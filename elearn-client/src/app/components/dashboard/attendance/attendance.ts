import { Component, computed, inject, OnInit, signal } from '@angular/core';
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
import { ModuleService } from '../modules/modules.service';
import { LoginService } from '../../login/login.service';
import {
  Absence,
  AbsenceByElement,
  AbsenceType,
  Department,
  Element,
  Module,
  Role,
  User
} from '../../../models/models';

@Component({
  selector: 'user-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './attendance.html'
})
export class Attendance implements OnInit {
  private attendanceService = inject(AttendaceService);
  private calendarService = inject(CalendarService);
  private moduleService = inject(ModuleService);
  private loginService = inject(LoginService);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  user = this.loginService.userProfile;

  activeTab = signal<'records' | 'students'>('records');

  private getCurrentAcademicSemester(): number {
    const month = new Date().getMonth() + 1;
    return month >= 2 && month <= 8 ? 2 : 1;
  }

  viewDepartment = signal<string>(Department.GENIE_INFORMATIQUE);
  viewYear = signal<number>(1);
  viewSemester = signal<number>(this.getCurrentAcademicSemester());
  viewGroup = signal<string>('A');
  availableGroups = signal<string[]>(['A', 'B']);
  formGroups = signal<string[]>(['A', 'B']);
  searchQuery = signal<string>('');

  managedAbsences = signal<Absence[]>([]);
  students = signal<User[]>([]);
  modules = signal<Module[]>([]);
  allElements = signal<Element[]>([]);

  selectedStudent = signal<User | null>(null);
  userAbsences = signal<AbsenceByElement[]>([]);

  showForm = signal(false);
  editAbsenceId = signal<string | null>(null);
  deleteTarget = signal<Absence | null>(null);
  errorMessage = signal<string | null>(null);

  formFilteredModules = signal<Module[]>([]);
  formFilteredElements = signal<Element[]>([]);
  formFilteredStudents = signal<User[]>([]);

  form = this.fb.group({
    department: [Department.GENIE_INFORMATIQUE as string, Validators.required],
    year: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(2)]],
    groupName: ['A', Validators.required],
    studentId: ['', Validators.required],
    moduleId: [''],
    elementId: ['', Validators.required],
    date: ['', Validators.required],
    time: ['09:00', Validators.required],
    type: [AbsenceType.CLASS as string, Validators.required],
    isJustified: [false]
  });

  get isAdminOrTeacher(): boolean {
    const roleStr = String(this.user()?.role || '');
    return (
      roleStr === Role.ADMIN ||
      roleStr === Role.TEACHER ||
      roleStr === 'ADMIN' ||
      roleStr === 'TEACHER'
    );
  }

  get isAdmin(): boolean {
    const roleStr = String(this.user()?.role || '');
    return roleStr === Role.ADMIN || roleStr === 'ADMIN';
  }

  filteredStudentsList = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    const dept = this.viewDepartment();
    const yr = this.viewYear();
    const grp = this.viewGroup();

    return this.students().filter((s) => {
      if (dept && String(s.department) !== dept) return false;
      if (yr > 0 && s.year !== yr) return false;
      if (grp && (s.groupName || 'A').toUpperCase() !== grp.toUpperCase()) {
        return false;
      }
      if (!q) return true;
      const full = `${s.firstName} ${s.lastName}`.toLowerCase();
      return (
        full.includes(q) ||
        s.username.toLowerCase().includes(q) ||
        s.email.toLowerCase().includes(q)
      );
    });
  });

  ngOnInit(): void {
    const currentUser = this.user();
    if (currentUser && !this.isAdminOrTeacher) {
      if (currentUser.department) {
        this.viewDepartment.set(String(currentUser.department));
      }
      if (currentUser.year > 0) this.viewYear.set(currentUser.year);
      if (currentUser.semester > 0) {
        this.viewSemester.set(currentUser.semester);
      }
      if (currentUser.groupName) this.viewGroup.set(currentUser.groupName);
    } else if (currentUser && currentUser.department) {
      this.viewDepartment.set(String(currentUser.department));
    }

    this.setupFormListeners();
    this.refreshAvailableGroups();

    if (this.isAdminOrTeacher) {
      this.loadStaffDependencies();
      this.loadManagedAbsences();
    } else {
      this.loadAbsences(null);
    }
  }

  refreshAvailableGroups(): void {
    this.calendarService
      .getGroups(this.viewDepartment(), this.viewYear())
      .subscribe({
        next: (groups) => {
          const list = groups.length ? groups : ['A', 'B'];
          this.availableGroups.set(list);
          if (!list.includes(this.viewGroup())) {
            this.viewGroup.set(list[0]);
          }
        },
        error: () => this.availableGroups.set(['A', 'B'])
      });
  }

  private setupFormListeners(): void {
    this.form.get('department')?.valueChanges.subscribe(() => {
      this.onFormClassChange();
    });
    this.form.get('year')?.valueChanges.subscribe(() => {
      this.onFormClassChange();
    });
    this.form.get('semester')?.valueChanges.subscribe(() => {
      this.syncFormCascades();
    });
    this.form.get('groupName')?.valueChanges.subscribe(() => {
      this.syncFormStudents();
    });
    this.form.get('moduleId')?.valueChanges.subscribe(() => {
      this.syncFormElements();
    });
  }

  private onFormClassChange(): void {
    const dept = this.form.get('department')?.value;
    const yr = Number(this.form.get('year')?.value || 1);
    if (dept && yr > 0) {
      this.calendarService.getGroups(dept, yr).subscribe({
        next: (grps) => this.formGroups.set(grps.length ? grps : ['A', 'B']),
        error: () => this.formGroups.set(['A', 'B'])
      });
    }
    this.syncFormCascades();
  }

  loadStaffDependencies(onDone?: () => void): void {
    forkJoin({
      students: this.attendanceService
        .getStudents()
        .pipe(catchError(() => of([] as User[]))),
      modules: this.moduleService
        .getModules()
        .pipe(catchError(() => of([] as Module[]))),
      elements: this.http
        .get<Element[]>('/api/modules/elements', { withCredentials: true })
        .pipe(catchError(() => of([] as Element[])))
    }).subscribe(({ students, modules, elements }) => {
      this.students.set(students);
      this.allElements.set(elements);

      let resolvedMods = modules;
      if (resolvedMods.length === 0 && elements.length > 0) {
        const map = new Map<string, Module>();
        elements.forEach((e) => {
          if (e.module && !map.has(e.module.id)) {
            map.set(e.module.id, e.module);
          }
        });
        resolvedMods = Array.from(map.values());
      }
      this.modules.set(resolvedMods);
      this.syncFormCascades();
      if (onDone) onDone();
    });
  }

  private syncFormCascades(): void {
    this.syncFormStudents();
    this.syncFormModules();
  }

  private syncFormStudents(): void {
    const dept = this.form.get('department')?.value;
    const yr = Number(this.form.get('year')?.value || 0);
    const grp = (this.form.get('groupName')?.value || 'A').toUpperCase();

    const matching = this.students().filter((s) => {
      const matchDept = !dept || String(s.department) === dept;
      const matchYr = !yr || s.year === yr;
      const matchGrp =
        !grp || (s.groupName || 'A').toUpperCase() === grp;
      return matchDept && matchYr && matchGrp;
    });

    const list = matching.length > 0 ? matching : this.students();
    this.formFilteredStudents.set(list);

    const curStudentId = this.form.get('studentId')?.value;
    if (list.length > 0 && !list.some((s) => s.id === curStudentId)) {
      this.form.patchValue({ studentId: list[0].id }, { emitEvent: false });
    }
  }

  private syncFormModules(): void {
    const dept = this.form.get('department')?.value;
    const yr = Number(this.form.get('year')?.value || 0);
    const sem = Number(this.form.get('semester')?.value || 0);

    const teacherModIds = new Set(
      this.allElements()
        .map((e) => e.module?.id)
        .filter((id): id is string => !!id)
    );

    const matching = this.modules().filter((m) => {
      if (
        !this.isAdmin &&
        teacherModIds.size > 0 &&
        !teacherModIds.has(m.id)
      ) {
        return false;
      }
      const matchDept = !dept || !m.department || String(m.department) === dept;
      const matchYr = !yr || !m.year || m.year === yr;
      const matchSem = !sem || !m.semester || m.semester === sem;
      return matchDept && matchYr && matchSem;
    });

    const list = matching.length > 0 ? matching : this.modules();
    this.formFilteredModules.set(list);

    const curModId = this.form.get('moduleId')?.value;
    if (list.length > 0 && (!curModId || !list.some((m) => m.id === curModId))) {
      this.form.patchValue({ moduleId: list[0].id }, { emitEvent: false });
    }
    this.syncFormElements();
  }

  private syncFormElements(): void {
    const modId = this.form.get('moduleId')?.value;
    const all = this.allElements();
    const byMod = modId
      ? all.filter((e) => e.module?.id === modId)
      : all;
    const list = byMod.length > 0 ? byMod : all;
    this.formFilteredElements.set(list);

    const curElId = this.form.get('elementId')?.value;
    if (list.length > 0 && !list.some((e) => e.id === curElId)) {
      this.form.patchValue({ elementId: list[0].id }, { emitEvent: false });
    }
  }

  loadManagedAbsences(): void {
    const dept = this.viewDepartment();
    const yr = this.viewYear();
    const sem = this.viewSemester();
    const grp = this.viewGroup();

    if (!dept || yr <= 0 || sem <= 0 || !grp) {
      return;
    }

    this.attendanceService
      .getManagedAbsences({
        department: dept,
        year: yr,
        semester: sem,
        group: grp,
        searchName: this.searchQuery() || undefined
      })
      .subscribe({
        next: (res) => this.managedAbsences.set(res),
        error: () => this.managedAbsences.set([])
      });
  }

  loadAbsences(studentId: string | null): void {
    this.attendanceService.getByStudent(studentId).subscribe({
      next: (res) => this.userAbsences.set(res),
      error: () => this.userAbsences.set([])
    });
  }

  onFilterChange(): void {
    this.refreshAvailableGroups();
    if (this.isAdminOrTeacher) {
      this.loadManagedAbsences();
    }
  }

  selectStudent(student: User): void {
    this.selectedStudent.set(student);
    this.loadAbsences(student.id);
  }

  resetStudentSelection(): void {
    this.selectedStudent.set(null);
    this.userAbsences.set([]);
  }

  openCreateModal(presetStudent?: User): void {
    this.errorMessage.set(null);
    this.editAbsenceId.set(null);
    this.loadStaffDependencies(() => {
      const targetStudent = presetStudent || this.selectedStudent();

      const dept =
        (targetStudent?.department as unknown as string) ||
        this.viewDepartment();
      const yr = targetStudent?.year || this.viewYear();
      const sem = this.viewSemester();
      const grp = targetStudent?.groupName || this.viewGroup();

      const now = new Date();
      const dateStr = this.toISODate(now);
      const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(
        now.getMinutes()
      ).padStart(2, '0')}`;

      this.form.patchValue(
        {
          department: dept,
          year: yr,
          semester: sem,
          groupName: grp,
          date: dateStr,
          time: timeStr,
          type: AbsenceType.CLASS,
          isJustified: false
        },
        { emitEvent: false }
      );
      this.onFormClassChange();
      if (targetStudent) {
        this.form.patchValue(
          { studentId: targetStudent.id },
          { emitEvent: false }
        );
      }
      this.showForm.set(true);
    });
  }

  openEditModal(absence: Absence): void {
    this.errorMessage.set(null);
    this.editAbsenceId.set(absence.id);
    this.loadStaffDependencies(() => {
      const dt = new Date(absence.dateTime);
      const dateStr = this.toISODate(dt);
      const timeStr = `${String(dt.getHours()).padStart(2, '0')}:${String(
        dt.getMinutes()
      ).padStart(2, '0')}`;

      const dept =
        (absence.student?.department as unknown as string) ||
        (absence.element?.module?.department as unknown as string) ||
        this.viewDepartment();
      const yr =
        absence.student?.year ||
        absence.element?.module?.year ||
        this.viewYear();
      const sem =
        absence.element?.module?.semester ||
        absence.student?.semester ||
        this.viewSemester();
      const grp = absence.student?.groupName || this.viewGroup();

      this.form.patchValue(
        {
          department: dept,
          year: yr,
          semester: sem,
          groupName: grp,
          moduleId: absence.element?.module?.id || '',
          elementId: absence.element?.id || '',
          studentId: absence.student?.id || '',
          date: dateStr,
          time: timeStr,
          type: String(absence.type || AbsenceType.CLASS),
          isJustified: !!absence.isJustified
        },
        { emitEvent: false }
      );
      this.onFormClassChange();
      this.form.patchValue(
        {
          moduleId: absence.element?.module?.id || '',
          elementId: absence.element?.id || '',
          studentId: absence.student?.id || ''
        },
        { emitEvent: false }
      );
      this.showForm.set(true);
    });
  }

  closeModal(): void {
    this.showForm.set(false);
    this.editAbsenceId.set(null);
    this.errorMessage.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Please fill in all required fields.');
      return;
    }
    const raw = this.form.getRawValue();
    const dateTime = `${raw.date}T${raw.time}:00`;
    const id = this.editAbsenceId();

    const request$ = id
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

    request$.subscribe({
      next: (saved) => {
        if (saved.student?.department) {
          this.viewDepartment.set(String(saved.student.department));
        }
        if (saved.student?.year && saved.student.year > 0) {
          this.viewYear.set(saved.student.year);
        }
        if (saved.element?.module?.semester) {
          this.viewSemester.set(saved.element.module.semester);
        }
        if (saved.student?.groupName) {
          this.viewGroup.set(saved.student.groupName);
        }
        this.closeModal();
        this.refreshAvailableGroups();
        this.loadManagedAbsences();
        const sel = this.selectedStudent();
        if (sel) {
          this.loadAbsences(sel.id);
        }
      },
      error: (err) => {
        this.errorMessage.set(
          err?.error?.message || 'Failed to save absence record.'
        );
      }
    });
  }

  toggleJustification(absence: Absence): void {
    if (!this.isAdminOrTeacher) return;
    this.attendanceService
      .updateAbsence(absence.id, {
        isJustified: !absence.isJustified,
        type: absence.type
      })
      .subscribe({
        next: (updated) => {
          this.managedAbsences.update((list) =>
            list.map((a) => (a.id === updated.id ? updated : a))
          );
        }
      });
  }

  openDeleteModal(absence: Absence): void {
    this.deleteTarget.set(absence);
  }

  closeDeleteModal(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.attendanceService.deleteAbsence(target.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.managedAbsences.update((list) =>
          list.filter((a) => a.id !== target.id)
        );
        const sel = this.selectedStudent();
        if (sel) {
          this.loadAbsences(sel.id);
        }
      },
      error: (err) => {
        this.closeDeleteModal();
        this.errorMessage.set(
          err?.error?.message || 'Failed to delete absence record.'
        );
      }
    });
  }

  private toISODate(date: Date): string {
    const yr = date.getFullYear();
    const mo = String(date.getMonth() + 1).padStart(2, '0');
    const da = String(date.getDate()).padStart(2, '0');
    return `${yr}-${mo}-${da}`;
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

  formatStatus(status: string): string {
    if (!status) return 'Normal';
    return status
      .toLowerCase()
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ');
  }
}
