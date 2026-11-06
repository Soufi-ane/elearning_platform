import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ModuleService } from '../../modules/modules.service';
import { CalendarService } from '../../calendar/calendar.service';
import {
  Department,
  Module,
  Element,
  Room,
  User
} from '../../../../models/models';

@Component({
  selector: 'app-admin-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-management.html'
})
export class AdminManagement implements OnInit {
  private moduleService = inject(ModuleService);
  private calendarService = inject(CalendarService);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  modules = signal<Module[]>([]);
  elements = signal<Element[]>([]);
  teachers = signal<User[]>([]);
  rooms = signal<Room[]>([]);
  availableGroups = signal<string[]>(['A', 'B']);

  activeModal = signal<'modules' | 'elements' | 'seances' | null>(null);

  moduleForm = this.fb.group({
    name: ['', Validators.required],
    year: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(2)]],
    department: ['', Validators.required]
  });

  elementForm = this.fb.group({
    name: ['', Validators.required],
    moduleId: ['', Validators.required],
    teacherId: ['', Validators.required]
  });

  seanceForm = this.fb.group({
    department: [Department.GENIE_INFORMATIQUE as string],
    year: [1],
    semester: [1],
    groupName: ['A'],
    date: ['', Validators.required],
    startsAt: ['09:00', Validators.required],
    endsAt: ['11:00', Validators.required],
    weeklyRepeats: [1, [Validators.required, Validators.min(1)]],
    type: ['LECTURE', Validators.required],
    elementId: ['', Validators.required],
    roomId: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.moduleService.getModules().subscribe((res) => this.modules.set(res));
    this.http
      .get<Element[]>('/api/modules/elements', { withCredentials: true })
      .subscribe((res) => this.elements.set(res));
    this.http
      .get<User[]>('/api/users?role=TEACHER', { withCredentials: true })
      .subscribe((res) => this.teachers.set(res));
    this.http
      .get<Room[]>('/api/rooms', { withCredentials: true })
      .subscribe({
        next: (res) => this.rooms.set(res),
        error: () => this.rooms.set([])
      });
  }

  openModal(type: 'modules' | 'elements' | 'seances') {
    this.loadData();
    this.activeModal.set(type);
  }

  closeModal() {
    this.activeModal.set(null);
  }

  onModuleSubmit() {
    if (this.moduleForm.invalid) return;
    const raw = this.moduleForm.getRawValue();
    this.moduleService
      .createModule({
        name: raw.name!,
        year: Number(raw.year),
        semester: Number(raw.semester),
        department: raw.department!
      })
      .subscribe(() => {
        this.moduleForm.reset({
          name: '',
          year: 1,
          semester: 1,
          department: ''
        });
        this.closeModal();
        this.loadData();
      });
  }

  onElementSubmit() {
    if (this.elementForm.invalid) return;
    this.moduleService
      .createElement(this.elementForm.getRawValue() as any)
      .subscribe(() => {
        this.elementForm.reset({ name: '', moduleId: '', teacherId: '' });
        this.closeModal();
        this.loadData();
      });
  }

  onSeanceSubmit() {
    if (this.seanceForm.invalid) return;
    const raw = this.seanceForm.getRawValue();
    const selectedEl = this.elements().find((e) => e.id === raw.elementId);

    const payload = {
      ...raw,
      department:
        selectedEl?.module?.department ||
        raw.department ||
        Department.GENIE_INFORMATIQUE,
      year: selectedEl?.module?.year || Number(raw.year) || 1,
      semester: selectedEl?.module?.semester || Number(raw.semester) || 1,
      groupName: raw.groupName || 'A',
      weeklyRepeats: Number(raw.weeklyRepeats) || 1
    };

    this.calendarService.createPlan(payload).subscribe(() => {
      this.seanceForm.reset({
        department: Department.GENIE_INFORMATIQUE,
        year: 1,
        semester: 1,
        groupName: 'A',
        date: '',
        startsAt: '09:00',
        endsAt: '11:00',
        weeklyRepeats: 1,
        type: 'LECTURE',
        elementId: '',
        roomId: ''
      });
      this.closeModal();
    });
  }
}
