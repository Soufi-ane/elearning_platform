import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ModuleService } from '../../modules/modules.service';
import { CalendarService } from '../../calendar/calendar.service';
import { Module, Element, Department, Room, User } from '../../../../models/models';

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

  activeModal = signal<'modules' | 'elements' | 'seances' | null>(null);

  moduleForm = this.fb.group({
    name: ['', Validators.required],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(6)]],
    department: ['', Validators.required]
  });

  elementForm = this.fb.group({
    name: ['', Validators.required],
    moduleId: ['', Validators.required],
    teacherId: ['', Validators.required]
  });

  seanceForm = this.fb.group({
    date: ['', Validators.required],
    startsAt: ['', Validators.required],
    endsAt: ['', Validators.required],
    weeklyRepeats: [1, [Validators.required, Validators.min(1)]],
    type: ['', Validators.required],
    elementId: ['', Validators.required],
    roomId: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.moduleService.getModules().subscribe(res => this.modules.set(res));
    this.http.get<Element[]>('/api/modules/elements', {
      withCredentials: true
    }).subscribe(res => this.elements.set(res));
    this.http.get<User[]>('/api/users?role=TEACHER', {
      withCredentials: true
    }).subscribe(res => this.teachers.set(res));
    this.http.get<Room[]>('/api/rooms', {
      withCredentials: true
    }).subscribe({
      next: (res) => this.rooms.set(res),
      error: () => this.rooms.set([])
    });
  }

  openModal(type: 'modules' | 'elements' | 'seances') {
    this.activeModal.set(type);
  }

  closeModal() {
    this.activeModal.set(null);
  }

  onModuleSubmit() {
    if (this.moduleForm.invalid) return;
    this.moduleService.createModule(
      this.moduleForm.getRawValue() as any
    ).subscribe(() => {
      this.moduleForm.reset({
        name: '',
        semester: 1,
        department: ''
      });
      this.closeModal();
      this.loadData();
    });
  }

  onElementSubmit() {
    if (this.elementForm.invalid) return;
    this.moduleService.createElement(
      this.elementForm.getRawValue() as any
    ).subscribe(() => {
      this.elementForm.reset({ name: '', moduleId: '', teacherId: '' });
      this.closeModal();
      this.loadData();
    });
  }

  onSeanceSubmit() {
    if (this.seanceForm.invalid) return;
    this.calendarService.createPlan(this.seanceForm.getRawValue()).subscribe(() => {
      this.seanceForm.reset({
        date: '',
        startsAt: '',
        endsAt: '',
        weeklyRepeats: 1,
        type: '',
        elementId: '',
        roomId: ''
      });
      this.closeModal();
    });
  }
}
