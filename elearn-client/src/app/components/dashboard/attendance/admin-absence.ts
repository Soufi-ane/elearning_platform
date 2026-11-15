import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AttendaceService } from './attendance.service';
import { CalendarService } from '../calendar/calendar.service';
import {
  Absence,
  AbsenceType,
  Department,
  Element,
  User
} from '../../../models/models';

@Component({
  selector: 'admin-absence',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-absence.html'
})
export class AdminAbsence implements OnInit {
  private attendanceService = inject(AttendaceService);
  private calendarService = inject(CalendarService);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  absences = signal<Absence[]>([]);
  students = signal<User[]>([]);
  elements = signal<Element[]>([]);

  private getCurrentAcademicSemester(): number {
    const month = new Date().getMonth() + 1;
    return month >= 2 && month <= 8 ? 2 : 1;
  }

  searchName = signal<string>('');
  department = signal<string>(Department.GENIE_INFORMATIQUE);
  year = signal<number>(1);
  semester = signal<number>(this.getCurrentAcademicSemester());
  group = signal<string>('A');
  availableGroups = signal<string[]>(['A', 'B']);

  showForm = signal(false);
  editId = signal<string | null>(null);
  deleteTarget = signal<Absence | null>(null);

  form = this.fb.group({
    studentId: ['', Validators.required],
    elementId: ['', Validators.required],
    date: ['', Validators.required],
    time: ['09:00', Validators.required],
    type: [AbsenceType.CLASS as string, Validators.required],
    isJustified: [false]
  });

  ngOnInit(): void {
    this.refreshAvailableGroups();
    this.loadDependencies();
    this.loadManagedAbsences();
  }

  refreshAvailableGroups(): void {
    this.calendarService.getGroups(this.department(), this.year()).subscribe({
      next: (groups) => {
        const list = groups.length ? groups : ['A', 'B'];
        this.availableGroups.set(list);
        if (!list.includes(this.group())) {
          this.group.set(list[0]);
        }
      },
      error: () => this.availableGroups.set(['A', 'B'])
    });
  }

  loadDependencies(): void {
    forkJoin({
      students: this.attendanceService
        .getStudents()
        .pipe(catchError(() => of([] as User[]))),
      elements: this.http
        .get<Element[]>('/api/modules/elements', { withCredentials: true })
        .pipe(catchError(() => of([] as Element[])))
    }).subscribe(({ students, elements }) => {
      this.students.set(students);
      this.elements.set(elements);
    });
  }

  loadManagedAbsences(): void {
    this.attendanceService
      .getManagedAbsences({
        department: this.department(),
        year: this.year(),
        semester: this.semester(),
        group: this.group(),
        searchName: this.searchName() || undefined
      })
      .subscribe({
        next: (res) => this.absences.set(res),
        error: () => this.absences.set([])
      });
  }

  onFilterChange(): void {
    this.refreshAvailableGroups();
    this.loadManagedAbsences();
  }

  openCreateModal(): void {
    this.editId.set(null);
    const now = new Date();
    const dateStr = `${now.getFullYear()}-${String(
      now.getMonth() + 1
    ).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
    this.form.reset({
      studentId: this.students()[0]?.id || '',
      elementId: this.elements()[0]?.id || '',
      date: dateStr,
      time: '09:00',
      type: AbsenceType.CLASS,
      isJustified: false
    });
    this.showForm.set(true);
  }

  openEditModal(a: Absence): void {
    this.editId.set(a.id);
    const dt = new Date(a.dateTime);
    const dateStr = `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(
      2,
      '0'
    )}-${String(dt.getDate()).padStart(2, '0')}`;
    const timeStr = `${String(dt.getHours()).padStart(2, '0')}:${String(
      dt.getMinutes()
    ).padStart(2, '0')}`;
    this.form.patchValue({
      studentId: a.student.id,
      elementId: a.element.id,
      date: dateStr,
      time: timeStr,
      type: String(a.type),
      isJustified: a.isJustified
    });
    this.showForm.set(true);
  }

  closeModal(): void {
    this.showForm.set(false);
    this.editId.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const dateTime = `${raw.date}T${raw.time}:00`;
    const id = this.editId();

    const req$ = id
      ? this.attendanceService.updateAbsence(id, {
          studentId: raw.studentId!,
          elementId: raw.elementId!,
          dateTime,
          type: raw.type!,
          isJustified: !!raw.isJustified
        })
      : this.attendanceService.createAbsence({
          studentId: raw.studentId!,
          elementId: raw.elementId!,
          dateTime,
          type: raw.type!,
          isJustified: !!raw.isJustified
        });

    req$.subscribe(() => {
      this.closeModal();
      this.loadManagedAbsences();
    });
  }

  toggleJustify(a: Absence): void {
    this.attendanceService
      .updateAbsence(a.id, {
        isJustified: !a.isJustified,
        type: a.type
      })
      .subscribe((updated) => {
        this.absences.update((list) =>
          list.map((item) => (item.id === updated.id ? updated : item))
        );
      });
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.attendanceService.deleteAbsence(target.id).subscribe(() => {
      this.deleteTarget.set(null);
      this.loadManagedAbsences();
    });
  }

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }
}
