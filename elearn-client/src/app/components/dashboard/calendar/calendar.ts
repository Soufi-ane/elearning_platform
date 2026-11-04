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
  templateUrl: './calendar.html'
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
  viewYear = signal<number>(1);
  viewSemester = signal<number>(1);
  viewGroup = signal<string>('A');
  availableGroups = signal<string[]>(['A', 'B']);
  formGroups = signal<string[]>(['A', 'B']);

  rooms = signal<Room[]>([]);
  modules = signal<Module[]>([]);
  filteredModules = signal<Module[]>([]);
  allElements = signal<Element[]>([]);
  filteredElements = signal<Element[]>([]);

  form = this.fb.group({
    department: ['', Validators.required],
    year: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(2)]],
    groupName: ['', Validators.required],
    moduleId: [''],
    elementId: ['', Validators.required],
    roomId: ['', Validators.required],
    date: ['', Validators.required],
    startsAt: ['', Validators.required],
    endsAt: ['', Validators.required],
    weeklyRepeats: [1, [Validators.required, Validators.min(1)]],
    type: ['', Validators.required]
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
    if (user) {
      if (user.department) {
        this.viewDepartment.set(String(user.department));
      }
      if (user.year && user.year > 0) {
        this.viewYear.set(user.year);
      }
      if (user.semester && user.semester > 0) {
        this.viewSemester.set(user.semester);
      }
      if (user.groupName) {
        this.viewGroup.set(user.groupName);
      } else if (this.isAdminOrTeacher) {
        this.viewGroup.set('ALL');
      }
    }
    this.refreshAvailableGroups();
    const prevMonday = this.getPreviousMonday(new Date());
    const urlDate = this.route.snapshot.queryParamMap.get('d');
    this.init(urlDate ? this.urlStrToDate(urlDate) : prevMonday);
    this.loadFormDependencies();

    this.form.get('department')?.valueChanges.subscribe(() => {
      this.onFormClassChange();
    });
    this.form.get('year')?.valueChanges.subscribe(() => {
      this.onFormClassChange();
    });
    this.form.get('semester')?.valueChanges.subscribe(() => {
      this.filterFormModulesAndElements();
    });
    this.form.get('moduleId')?.valueChanges.subscribe(() => {
      this.filterFormElementsByModule();
    });
  }

  private onFormClassChange() {
    const dept = this.form.get('department')?.value;
    const yr = Number(this.form.get('year')?.value || 1);
    if (dept && yr > 0) {
      this.calendarService.getGroups(dept, yr).subscribe({
        next: (grps) => this.formGroups.set(grps.length ? grps : ['A', 'B']),
        error: () => this.formGroups.set(['A', 'B'])
      });
    }
    this.filterFormModulesAndElements();
  }

  private filterFormModulesAndElements() {
    const dept = this.form.get('department')?.value;
    const yr = Number(this.form.get('year')?.value || 0);
    const sem = Number(this.form.get('semester')?.value || 0);
    const mods = this.modules().filter(m => {
      const matchDept = !dept || !m.department || String(m.department) === dept;
      const matchYear = !yr || !m.year || m.year === yr;
      const matchSem = !sem || !m.semester || m.semester === sem;
      return matchDept && matchYear && matchSem;
    });
    this.filteredModules.set(mods);
    this.filterFormElementsByModule();
  }

  private filterFormElementsByModule() {
    const modId = this.form.get('moduleId')?.value;
    const allowedModIds = new Set(this.filteredModules().map(m => m.id));
    if (modId) {
      this.filteredElements.set(
        this.allElements().filter(e => e.module?.id === modId)
      );
    } else if (allowedModIds.size > 0) {
      this.filteredElements.set(
        this.allElements().filter(e => allowedModIds.has(e.module?.id))
      );
    } else {
      this.filteredElements.set(this.allElements());
    }
  }

  refreshAvailableGroups() {
    this.calendarService.getGroups(
      this.viewDepartment(),
      this.viewYear()
    ).subscribe({
      next: (groups) => {
        this.availableGroups.set(groups.length ? groups : ['A', 'B']);
      },
      error: () => this.availableGroups.set(['A', 'B'])
    });
  }

  changeDepartment(dept: string) {
    this.viewDepartment.set(dept);
    this.refreshAvailableGroups();
    this.init(this.currentWeekStart());
  }

  changeYear(yearVal: string | number) {
    this.viewYear.set(Number(yearVal));
    this.refreshAvailableGroups();
    this.init(this.currentWeekStart());
  }

  changeSemester(semVal: string | number) {
    this.viewSemester.set(Number(semVal));
    this.init(this.currentWeekStart());
  }

  changeGroup(groupVal: string) {
    this.viewGroup.set(groupVal);
    this.init(this.currentWeekStart());
  }

  loadFormDependencies() {
    if (this.isAdminOrTeacher) {
      this.http.get<Room[]>('/api/v1/rooms', { withCredentials: true })
        .subscribe(res => this.rooms.set(res));
      this.http.get<Module[]>('/api/v1/modules', { withCredentials: true })
        .subscribe(res => {
          this.modules.set(res);
          this.filteredModules.set(res);
        });
      this.http.get<Element[]>(
        '/api/v1/modules/elements',
        { withCredentials: true }
      ).subscribe(res => {
        this.allElements.set(res);
        this.filteredElements.set(res);
      });
    }
  }

  init(startDate: Date) {
    const monday = this.getPreviousMonday(startDate);
    this.currentWeekStart.set(monday);
    this.currentWeekStr = this.getCurrentWeekStr();
    this.endOfWeekStr = this.getEndOfWeekStr();
    this.buildWeekColumns(monday, {});

    this.calendarService.getByWeek(
      monday,
      this.viewDepartment(),
      this.viewYear(),
      this.viewSemester(),
      this.viewGroup()
    ).subscribe({
      next: (res) => {
        const mappedPlans = this.generatePlanWithSpan(res);
        this.buildWeekColumns(monday, mappedPlans);
      },
      error: () => this.buildWeekColumns(monday, {})
    });
    this.updateStartDate();
  }

  buildWeekColumns(startDate: Date, plansMap: Record<string, PlanWithSpan[]>) {
    const cols: DayColumn[] = [];
    for (let i = 0; i < 7; i++) {
      const current = new Date(startDate.getTime());
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
    prevWeek.setDate(prevWeek.getDate() - 7);
    this.init(prevWeek);
  }

  onNext() {
    const nextWeek = new Date(this.currentWeekStart().getTime());
    nextWeek.setDate(nextWeek.getDate() + 7);
    this.init(nextWeek);
  }

  openCreateModal() {
    this.editPlanId.set(null);
    const defaultGroup = this.viewGroup() === 'ALL' ? '' : this.viewGroup();
    this.form.reset({
      department: this.viewDepartment(),
      year: this.viewYear(),
      semester: this.viewSemester(),
      groupName: defaultGroup,
      moduleId: '',
      elementId: '',
      roomId: '',
      date: this.formatToISODate(new Date()),
      startsAt: '09:00',
      endsAt: '11:00',
      weeklyRepeats: 1,
      type: 'LECTURE'
    });
    this.onFormClassChange();
    this.showForm.set(true);
  }

  openEditModal(plan: PlanWithSpan, dateStr: string) {
    this.editPlanId.set(plan.id);
    const modId = plan.element?.module?.id || '';
    const dept = plan.department ||
      plan.element?.module?.department ||
      this.viewDepartment();
    const yr = plan.year || plan.element?.module?.year || this.viewYear();
    const sem = plan.semester ||
      plan.element?.module?.semester ||
      this.viewSemester();
    const grp = plan.groupName || 'A';

    this.form.patchValue({
      department: String(dept),
      year: yr,
      semester: sem,
      groupName: grp,
      moduleId: modId,
      elementId: plan.element.id,
      roomId: plan.room.id,
      date: plan.date || dateStr,
      startsAt: plan.startsAt.substring(0, 5),
      endsAt: plan.endsAt.substring(0, 5),
      weeklyRepeats: plan.weeklyRepeats || 1,
      type: String(plan.type)
    });
    this.onFormClassChange();
    this.showForm.set(true);
  }

  closeModal() {
    this.showForm.set(false);
    this.editPlanId.set(null);
  }

  onSubmit() {
    if (this.form.invalid) return;
    const id = this.editPlanId();
    const raw = this.form.getRawValue();
    const payload = {
      date: raw.date,
      startsAt: raw.startsAt,
      endsAt: raw.endsAt,
      weeklyRepeats: Number(raw.weeklyRepeats),
      type: raw.type,
      department: raw.department,
      year: Number(raw.year),
      semester: Number(raw.semester),
      groupName: raw.groupName?.trim().toUpperCase(),
      elementId: raw.elementId,
      roomId: raw.roomId
    };

    const targetWeek = raw.date
      ? this.getPreviousMonday(this.isoStrToDate(raw.date))
      : this.currentWeekStart();

    const afterSave = () => {
      if (payload.department) {
        this.viewDepartment.set(payload.department);
      }
      if (payload.year) {
        this.viewYear.set(payload.year);
      }
      if (payload.semester) {
        this.viewSemester.set(payload.semester);
      }
      if (
        payload.groupName &&
        this.viewGroup() !== 'ALL' &&
        this.viewGroup() !== payload.groupName
      ) {
        this.viewGroup.set(payload.groupName);
      }
      this.refreshAvailableGroups();
      this.closeModal();
      this.init(targetWeek);
    };

    if (id) {
      this.calendarService.updatePlan(id, payload).subscribe({
        next: () => afterSave(),
        error: () => alert('Failed to update seance. Verify element ownership.')
      });
    } else {
      this.calendarService.createPlan(payload).subscribe({
        next: () => afterSave(),
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
    const nextWeek = new Date(this.currentWeekStart().getTime());
    nextWeek.setDate(nextWeek.getDate() + 6);
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
    '08:00', '09:00', '10:00', '11:00',
    '12:00', '13:00', '14:00', '15:00',
    '16:00', '17:00', '18:00'
  ];

  days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  urlStrToDate(str: string): Date {
    const [day, month, year] = str.split('-');
    return new Date(+year, +month - 1, +day);
  }

  isoStrToDate(str: string): Date {
    const [year, month, day] = str.split('-');
    return new Date(+year, +month - 1, +day);
  }

  formatDate(date: Date, seperator: string): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${day}${seperator}${month}${seperator}${year}`;
  }

  formatShortDayDate(date: Date): string {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${day}/${month}`;
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

  private timeToMinutes(t: string): number {
    const parts = t.substring(0, 5).split(':');
    return (+parts[0]) * 60 + (+parts[1]);
  }

  addSpan(p: Plan): PlanWithSpan {
    const newPlan: PlanWithSpan = {
      ...p,
      rowSpan: [0, 1]
    };
    const baseMin = this.timeToMinutes(this.timeSlots[0]);
    const startMin = this.timeToMinutes(newPlan.startsAt);
    const endMin = this.timeToMinutes(newPlan.endsAt);
    const startIdx = Math.max(
      0,
      Math.min(
        this.timeSlots.length - 2,
        Math.floor((startMin - baseMin) / 60)
      )
    );
    const endIdx = Math.max(
      startIdx + 1,
      Math.min(
        this.timeSlots.length,
        Math.ceil((endMin - baseMin) / 60)
      )
    );
    newPlan.rowSpan = [startIdx, endIdx];
    return newPlan;
  }
}
