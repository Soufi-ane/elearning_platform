import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ModuleService } from '../../modules/modules.service';
import { CalendarService } from '../../calendar/calendar.service';
import { Module, Element, Department } from '../../../../models/models';

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
  teachers = signal<any[]>([]);
  rooms = signal<any[]>([]);

  activeTab = signal<'modules' | 'elements' | 'seances'>('modules');

  moduleForm = this.fb.group({
    name: ['', Validators.required],
    semester: [1, Validators.required],
    department: [Department.GENIE_INFORMATIQUE, Validators.required]
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
    weeklyRepeats: [1, Validators.required],
    type: ['LECTURE', Validators.required],
    elementId: ['', Validators.required],
    roomId: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.moduleService.getModules().subscribe(res => this.modules.set(res));
    this.http.get<any[]>('/api/v1/users?role=TEACHER', {
      withCredentials: true
    }).subscribe(res => this.teachers.set(res));
    this.http.get<any[]>('/api/v1/rooms', {
      withCredentials: true
    }).subscribe({
      next: (res) => this.rooms.set(res),
      error: () => this.rooms.set([])
    });
  }

  onModuleSubmit() {
    if (this.moduleForm.invalid) return;
    this.moduleService.createModule(
      this.moduleForm.value as any
    ).subscribe(() => {
      this.moduleForm.reset({
        semester: 1,
        department: Department.GENIE_INFORMATIQUE
      });
      this.loadData();
    });
  }

  onElementSubmit() {
    if (this.elementForm.invalid) return;
    this.moduleService.createElement(
      this.elementForm.value as any
    ).subscribe(() => {
      this.elementForm.reset();
      this.loadData();
    });
  }

  onSeanceSubmit() {
    if (this.seanceForm.invalid) return;
    this.calendarService.createPlan(this.seanceForm.value).subscribe(() => {
      this.seanceForm.reset({
        weeklyRepeats: 1,
        type: 'LECTURE'
      });
      alert('Seance added successfully');
    });
  }
}
