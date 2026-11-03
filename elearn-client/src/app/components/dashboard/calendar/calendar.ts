import { Component, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CalendarService } from './calendar.service';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginService } from '../../login/login.service';
import {
  Department,
  Element,
  Module,
  Plan,
  PlanWithSpan,
  Role,
  Room
} from '../../../models/models';

export type DayColumn = {
  date: Date;
  dateStr: string;
  plans: PlanWithSpan[];
};

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './calendar.html',
})
export class Calendar implements OnInit {
  private calendarService = inject(CalendarService);
  private loginService = inject(LoginService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

  currentWeekStart = signal<Date>(new Date());
  currentWeekStr = '';
  endOfWeekStr = '';
  weekColumns = signal<DayColumn[]>([]);
  showForm = signal(false);
  editPlanId = signal<string | null>(null);
  deleteTarget = signal<PlanWithSpan | null>(null);
  viewDepartment = signal<string>(Department.GENIE_INFORMATIQUE);

  rooms = signal<Room[]>([]);
  modules = signal<Module[]>([]);
  allElements = signal<Element[]>([]);
  filteredElements = signal<Element[]>([]);

  form = this.fb.group({
    date: ['', Validators.required],
    startsAt: ['', Validators.required],
    endsAt: ['', Validators.required],
    weeklyRepeats: [1, [Validators.required, Validators.min(1)]],
    type: ['LECTURE', Validators.required],
    moduleId: [''],
    elementId: ['', Validators.required],
    roomId: ['', Validators.required]
  });

  get isAdminOrTeacher(): boolean {
    const roleStr = String(this.loginService.userProfile()?.role);
    return roleStr === Role.ADMIN || roleStr === Role.TEACHER ||
      roleStr === 'ADMIN' || roleStr === 'TEACHER';
  }

  get isAdmin(): boolean {
    const roleStr = String(this.loginService.userProfile()?.role);
    return roleStr === Role.ADMIN || roleStr === 'ADMIN';
  }

  updateStartDate() {
    this.router.navigate(
      ['/dashboard/calendar'],
      { queryParams: { d: this.formatDate(this.currentWeekStart(), '-') } }
    );
  }

  ngOnInit(): void {
    const user = this.loginService.userProfile();
    if (user && user.department) {
      this.viewDepartment.set(user.department as string);
    }

    const prevMonday = this.getPreviousMonday(new Date());
    const urlDate = this.route.snapshot.queryParamMap.get('d');
    this.init(urlDate ? this.urlStrToDate(urlDate) : prevMonday);
    this.loadFormDependencies();

    this.form.get('moduleId')?.valueChanges.subscribe(modId => {
      if (!modId) {
        this.filteredElements.set(this.allElements());
      } else {
        const filtered = this.allElements().filter(e => e.module.id === modId);
        this.filteredElements.set(filtered);
      }
      if (!this.editPlanId()) {
        this.form.patchValue({ elementId: '' }, { emitEvent: false });
      }
    });
  }

  changeDepartment(dept: string) {
    this.viewDepartment.set(dept);
    this.init(this.currentWeekStart());
  }

  loadFormDependencies() {
    if (this.isAdminOrTeacher) {
      this.http.get<Room[]>('/api/rooms', { withCredentials: true })
        .subscribe(res => this.rooms.set(res));
      this.http.get<Module[]>('/api/modules', { withCredentials: true })
        .subscribe(res => this.modules.set(res));
      this.http.get<Element[]>(
        '/api/modules/elements',
        { withCredentials: true }
      ).subscribe(res => {
        this.allElements.set(res);
        this.filteredElements.set(res);
      });
    }
  }

  init(startDate: Date) {
    this.currentWeekStart.set(startDate);
    this.currentWeekStr = this.getCurrentWeekStr();
    this.endOfWeekStr = this.getEndOfWeekStr();
    this.buildWeekColumns(startDate, {});

    this.calendarService.getByWeek(
      startDate,
      this.viewDepartment() as any
    ).subscribe({
      next: (res) => {
        const mappedPlans = this.generatePlanWithSpan(res);
        this.buildWeekColumns(startDate, mappedPlans);
      },
      error: () => this.buildWeekColumns(startDate, {})
    });
    this.updateStartDate();
  }

  buildWeekColumns(startDate: Date, plansMap: Record<string, PlanWithSpan[]>) {
    const cols: DayColumn[] = [];
    for (let i = 0; i < 7; i++) {
      const current = new Date(startDate);
      current.setDate(current.getDate() + i);
      const dateStr = this.formatToISODate(current);
      cols.push({
        date: current,
        dateStr: dateStr,
        plans: plansMap[dateStr] || []
      });
    }
    this.weekColumns.set(cols);
  }

  onPrevious() {
    const prevWeek = new Date(this.currentWeekStart().getTime());
    prevWeek.setDate(prevWeek.getDate() - 1);
    this.init(this.getPreviousMonday(prevWeek));
  }

  onNext() {
    const nextWeek = new Date(this.currentWeekStart().getTime());
    nextWeek.setDate(nextWeek.getDate() + 7);
    this.init(nextWeek);
  }

  openCreateModal() {
    this.editPlanId.set(null);
    this.filteredElements.set(this.allElements());
    this.form.reset({
      date: this.formatToISODate(this.currentWeekStart()),
      startsAt: '09:00',
      endsAt: '11:00',
      weeklyRepeats: 1,
      type: 'LECTURE',
      moduleId: '',
      elementId: '',
      roomId: ''
    });
    this.showForm.set(true);
  }

  openEditModal(plan: PlanWithSpan, dateStr: string) {
    this.editPlanId.set(plan.id);
    const modId = plan.element?.module?.id || '';
    if (modId) {
      this.filteredElements.set(
        this.allElements().filter(e => e.module.id === modId)
      );
    } else {
      this.filteredElements.set(this.allElements());
    }
    this.form.patchValue({
      date: dateStr,
      startsAt: plan.startsAt.substring(0, 5),
      endsAt: plan.endsAt.substring(0, 5),
      weeklyRepeats: 1,
      type: String(plan.type),
      moduleId: modId,
      elementId: plan.element.id,
      roomId: plan.room.id
    });
    this.showForm.set(true);
  }

  closeModal() {
    this.showForm.set(false);
    this.editPlanId.set(null);
  }

  onSubmit() {
    if (this.form.invalid) return;
    const id = this.editPlanId();
    const payload = this.form.getRawValue();

    if (id) {
      this.calendarService.updatePlan(id, payload).subscribe({
        next: () => {
          this.closeModal();
          this.init(this.currentWeekStart());
        },
        error: () => alert('Failed to update seance. Verify element ownership.')
      });
    } else {
      this.calendarService.createPlan(payload).subscribe({
        next: () => {
          this.closeModal();
          this.init(this.currentWeekStart());
        },
        error: () => alert('Failed to create seance. Verify element ownership.')
      });
    }
  }

  openDeleteModal(plan: PlanWithSpan) {
    this.deleteTarget.set(plan);
  }

  closeDeleteModal() {
    this.deleteTarget.set(null);
  }

  confirmDelete() {
    const target = this.deleteTarget();
    if (!target) return;
    this.calendarService.deletePlan(target.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.init(this.currentWeekStart());
      },
      error: () => alert('Failed to delete seance. Verify element ownership.')
    });
  }

  findPlanAtSlot(plans: PlanWithSpan[], slotIndex: number): PlanWithSpan | null {
    for (const p of plans) {
      if (slotIndex >= p.rowSpan[0] && slotIndex < p.rowSpan[1]) {
        return p;
      }
    }
    return null;
  }

  getCurrentWeekStr(): string {
    return this.formatDate(this.currentWeekStart(), '/');
  }

  getEndOfWeekStr(): string {
    const nextWeek = new Date(this.currentWeekStart());
    nextWeek.setDate(nextWeek.getDate() + 7);
    return this.formatDate(nextWeek, '/');
  }

  getPreviousMonday(startDate: Date): Date {
    const day = startDate.getDay();
    const daysToshiftBy = day === 0 ? 6 : day - 1;
    const newDate = new Date(startDate.getTime());
    newDate.setDate(newDate.getDate() - daysToshiftBy);
    newDate.setHours(0, 0, 0, 0);
    return newDate;
  }

  timeSlots = [
    '07:30', '08:00', '08:30', '09:00',
    '09:30', '10:00', '10:30', '11:00',
    '11:30', '12:00', '12:30', '13:00',
    '13:30', '14:00', '14:30', '15:00',
    '15:30', '16:00', '16:30', '17:00',
    '17:30', '18:00',
  ];

  days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  urlStrToDate(str: string): Date {
    const [day, month, year] = str.split('-');
    return new Date(+year, +month - 1, +day);
  }

  formatDate(date: Date, seperator: string): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${day}${seperator}${month}${seperator}${year}`;
  }

  formatToISODate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  generatePlanWithSpan(
    plannings: Record<string, Plan[]>
  ): Record<string, PlanWithSpan[]> {
    const planningsWithSpan: Record<string, PlanWithSpan[]> = {};
    Object.entries(plannings).forEach(([date, plans]) => {
      planningsWithSpan[date] = plans.map((m: Plan) => this.addSpan(m));
    });
    return planningsWithSpan;
  }

  addSpan(p: Plan): PlanWithSpan {
    const newPlan: PlanWithSpan = {
      id: p.id,
      room: p.room,
      type: p.type,
      element: p.element,
      startsAt: p.startsAt,
      endsAt: p.endsAt,
      rowSpan: []
    };
    const start = newPlan.startsAt.substring(0, 5);
    const end = newPlan.endsAt.substring(0, 5);

    for (let i = 0; i < this.timeSlots.length; i++) {
      if (this.timeSlots[i] === start) newPlan.rowSpan[0] = i;
      if (this.timeSlots[i] === end) newPlan.rowSpan[1] = i;
    }

    return newPlan;
  }
}
