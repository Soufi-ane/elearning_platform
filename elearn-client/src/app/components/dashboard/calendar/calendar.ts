import {
  Component,
  effect,
  inject,
  OnInit,
  signal,
  ViewChild
} from '@angular/core';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CalendarService } from './calendar.service';
import { LoginService } from '../../login/login.service';
import {
  Department,
  Element,
  Module,
  Plan,
  Role,
  Room
} from '../../../models/models';
import {
  FullCalendarComponent,
  FullCalendarModule
} from '@fullcalendar/angular';
import {
  CalendarOptions,
  DateSelectArg,
  DatesSetArg,
  EventClickArg,
  EventDropArg,
  EventInput
} from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin, {
  EventResizeDoneArg
} from '@fullcalendar/interaction';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, FullCalendarModule],
  templateUrl: './calendar.html'
})
export class Calendar implements OnInit {
  @ViewChild(FullCalendarComponent) calendarComponent?: FullCalendarComponent;

  private calendarService = inject(CalendarService);
  private loginService = inject(LoginService);
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);

  visibleStart = signal<string>('');
  visibleEnd = signal<string>('');
  showForm = signal(false);
  editPlanId = signal<string | null>(null);
  selectedPlan = signal<Plan | null>(null);
  deleteTarget = signal<Plan | null>(null);
  errorMessage = signal<string | null>(null);

  viewDepartment = signal<string>('ALL');
  viewYear = signal<number>(0);
  viewSemester = signal<number>(0);
  viewGroup = signal<string>('ALL');

  availableGroups = signal<string[]>(['A', 'B']);
  formGroups = signal<string[]>(['A', 'B']);
  rooms = signal<Room[]>([]);
  modules = signal<Module[]>([]);
  filteredModules = signal<Module[]>([]);
  allElements = signal<Element[]>([]);
  filteredElements = signal<Element[]>([]);

  form = this.fb.group({
    department: [Department.GENIE_INFORMATIQUE as string, Validators.required],
    year: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(2)]],
    groupName: ['A', Validators.required],
    moduleId: [''],
    elementId: ['', Validators.required],
    roomId: ['', Validators.required],
    date: ['', Validators.required],
    startsAt: ['09:00', Validators.required],
    endsAt: ['11:00', Validators.required],
    weeklyRepeats: [1, [Validators.required, Validators.min(1)]],
    type: ['LECTURE', Validators.required]
  });

  calendarOptions = signal<CalendarOptions>({
    plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    firstDay: 1,
    allDaySlot: false,
    slotMinTime: '08:00:00',
    slotMaxTime: '19:00:00',
    slotDuration: '01:00:00',
    expandRows: true,
    height: 'auto',
    nowIndicator: true,
    selectable: false,
    editable: false,
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'timeGridWeek,timeGridDay,dayGridMonth'
    },
    events: [],
    datesSet: (arg: DatesSetArg) => this.onDatesSet(arg),
    select: (arg: DateSelectArg) => this.onDateSelect(arg),
    eventClick: (arg: EventClickArg) => this.onEventClick(arg),
    eventDrop: (arg: EventDropArg) => this.onEventDrop(arg),
    eventResize: (arg: EventResizeDoneArg) => this.onEventResize(arg)
  });

  constructor() {
    effect(() => {
      const user = this.loginService.userProfile();
      if (!user) return;
      const roleStr = String(user.role || '');
      const isStaff =
        roleStr === Role.ADMIN ||
        roleStr === Role.TEACHER ||
        roleStr === 'ADMIN' ||
        roleStr === 'TEACHER';

      if (isStaff) {
        this.calendarOptions.update((opts) => ({
          ...opts,
          selectable: true,
          editable: true,
          selectMirror: true
        }));
        if (this.allElements().length === 0 || this.rooms().length === 0) {
          this.loadFormDependencies();
        }
      } else {
        if (user.department) this.viewDepartment.set(String(user.department));
        if (user.year && user.year > 0) this.viewYear.set(user.year);
        if (user.semester && user.semester > 0) {
          this.viewSemester.set(user.semester);
        }
        if (user.groupName) this.viewGroup.set(user.groupName);
      }
    });
  }

  get isAdminOrTeacher(): boolean {
    const roleStr = String(this.loginService.userProfile()?.role || '');
    return (
      roleStr === Role.ADMIN ||
      roleStr === Role.TEACHER ||
      roleStr === 'ADMIN' ||
      roleStr === 'TEACHER'
    );
  }

  ngOnInit(): void {
    this.refreshAvailableGroups();
    this.setupFormListeners();
  }

  private setupFormListeners(): void {
    this.form.get('department')?.valueChanges.subscribe(() => {
      this.onFormClassChange();
    });
    this.form.get('year')?.valueChanges.subscribe(() => {
      this.onFormClassChange();
    });
    this.form.get('semester')?.valueChanges.subscribe(() => {
      this.filterFormModulesAndElements();
    });
    this.form.get('moduleId')?.valueChanges.subscribe((modId) => {
      const foundMod = this.modules().find((m) => m.id === modId);
      if (foundMod) {
        this.form.patchValue(
          {
            department: String(
              foundMod.department || Department.GENIE_INFORMATIQUE
            ),
            year: foundMod.year || 1,
            semester: foundMod.semester || 1
          },
          { emitEvent: false }
        );
      }
      this.filterFormElementsByModule();
    });
    this.form.get('elementId')?.valueChanges.subscribe((elId) => {
      if (!elId) return;
      const found = this.allElements().find((e) => e.id === elId);
      if (found && found.module) {
        this.form.patchValue(
          {
            department: String(
              found.module.department || Department.GENIE_INFORMATIQUE
            ),
            year: found.module.year || 1,
            semester: found.module.semester || 1,
            moduleId: found.module.id
          },
          { emitEvent: false }
        );
      }
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
    this.filterFormModulesAndElements();
  }

  private filterFormModulesAndElements(): void {
    const dept = this.form.get('department')?.value;
    const yr = Number(this.form.get('year')?.value || 0);
    const sem = Number(this.form.get('semester')?.value || 0);

    const mods = this.modules().filter((m) => {
      const matchDept = !dept || !m.department || String(m.department) === dept;
      const matchYear = !yr || !m.year || m.year === yr;
      const matchSem = !sem || !m.semester || m.semester === sem;
      return matchDept && matchYear && matchSem;
    });

    const nextMods = mods.length > 0 ? mods : this.modules();
    this.filteredModules.set(nextMods);

    const currentModId = this.form.get('moduleId')?.value;
    if (currentModId && !nextMods.some((m) => m.id === currentModId)) {
      this.form.patchValue({ moduleId: '' }, { emitEvent: false });
    }

    this.filterFormElementsByModule();
  }

  private filterFormElementsByModule(): void {
    const modId = this.form.get('moduleId')?.value;
    const allEls = this.allElements();

    let result: Element[] = [];
    if (modId) {
      const byMod = allEls.filter((e) => e.module?.id === modId);
      result = byMod.length > 0 ? byMod : allEls;
    } else {
      const allowedModIds = new Set(this.filteredModules().map((m) => m.id));
      const matching = allEls.filter(
        (e) => e.module && allowedModIds.has(e.module.id)
      );
      result = matching.length > 0 ? matching : allEls;
    }

    this.filteredElements.set(result);

    const currentElId = this.form.get('elementId')?.value;
    if (result.length > 0 && !result.some((e) => e.id === currentElId)) {
      this.form.patchValue({ elementId: result[0].id }, { emitEvent: false });
    }
  }

  loadFormDependencies(onLoaded?: () => void): void {
    forkJoin({
      rooms: this.http
        .get<Room[]>('/api/rooms', { withCredentials: true })
        .pipe(catchError(() => of([] as Room[]))),
      modules: this.http
        .get<Module[]>('/api/modules', { withCredentials: true })
        .pipe(catchError(() => of([] as Module[]))),
      elements: this.http
        .get<Element[]>('/api/modules/elements', { withCredentials: true })
        .pipe(catchError(() => of([] as Element[])))
    }).subscribe(({ rooms, modules, elements }) => {
      this.rooms.set(rooms);
      this.modules.set(modules);
      this.filteredModules.set(modules);
      this.allElements.set(elements);
      this.filteredElements.set(elements);

      if (!this.form.get('roomId')?.value && rooms.length > 0) {
        this.form.patchValue({ roomId: rooms[0].id }, { emitEvent: false });
      }
      if (!this.form.get('elementId')?.value && elements.length > 0) {
        this.form.patchValue(
          {
            moduleId: elements[0].module?.id || '',
            elementId: elements[0].id
          },
          { emitEvent: false }
        );
      }
      if (onLoaded) {
        onLoaded();
      }
    });
  }

  refreshAvailableGroups(): void {
    this.calendarService
      .getGroups(this.viewDepartment(), this.viewYear())
      .subscribe({
        next: (groups) => {
          this.availableGroups.set(groups.length ? groups : ['A', 'B']);
        },
        error: () => this.availableGroups.set(['A', 'B'])
      });
  }

  onFilterChange(): void {
    this.refreshAvailableGroups();
    this.loadEvents();
  }

  onDatesSet(arg: DatesSetArg): void {
    const startStr = this.formatToISODate(arg.start);
    const endInclusive = new Date(arg.end.getTime() - 86400000);
    const endStr = this.formatToISODate(endInclusive);
    this.visibleStart.set(startStr);
    this.visibleEnd.set(endStr);
    this.loadEvents();
  }

  loadEvents(): void {
    const start = this.visibleStart();
    const end = this.visibleEnd();
    if (!start || !end) return;

    this.calendarService
      .getEvents(
        start,
        end,
        this.viewDepartment(),
        this.viewYear(),
        this.viewSemester(),
        this.viewGroup()
      )
      .subscribe({
        next: (plans) => {
          const events = plans.map((p) => this.mapPlanToEvent(p));
          this.calendarOptions.update((opts) => ({
            ...opts,
            events: [...events]
          }));
          const api = this.calendarComponent?.getApi();
          if (api) {
            api.removeAllEvents();
            events.forEach((ev) => api.addEvent(ev));
          }
        },
        error: () => {
          this.calendarOptions.update((opts) => ({
            ...opts,
            events: []
          }));
        }
      });
  }

  private mapPlanToEvent(plan: Plan): EventInput {
    const startIso = `${plan.date}T${plan.startsAt}`;
    const endIso = `${plan.date}T${plan.endsAt}`;
    const isExam = String(plan.type) === 'EXAM';
    const badge = `Y${plan.year}S${plan.semester}-${plan.groupName || 'A'}`;

    return {
      id: plan.id,
      title: `${plan.element.name} (${plan.room.label}) [${badge}]`,
      start: startIso,
      end: endIso,
      backgroundColor: isExam ? '#4C1D24' : '#14261C',
      borderColor: isExam ? '#F43F5E' : '#C8A96E',
      textColor: '#EAE3D5',
      extendedProps: { plan }
    };
  }

  onDateSelect(selectInfo: DateSelectArg): void {
    if (!this.isAdminOrTeacher) return;
    selectInfo.view.calendar.unselect();

    const dateStr = this.formatToISODate(selectInfo.start);
    const startTime = this.formatToTime(selectInfo.start);
    const endTime = this.formatToTime(selectInfo.end);
    this.openCreateModal(dateStr, startTime, endTime);
  }

  onEventClick(clickInfo: EventClickArg): void {
    const plan = clickInfo.event.extendedProps['plan'] as Plan;
    if (!plan) return;
    if (this.isAdminOrTeacher) {
      this.openEditModal(plan);
    }
  }

  onEventDrop(dropInfo: EventDropArg): void {
    const plan = dropInfo.event.extendedProps['plan'] as Plan;
    if (!plan || !dropInfo.event.start || !dropInfo.event.end) {
      dropInfo.revert();
      return;
    }

    const payload = {
      date: this.formatToISODate(dropInfo.event.start),
      startsAt: this.formatToTime(dropInfo.event.start),
      endsAt: this.formatToTime(dropInfo.event.end)
    };

    this.calendarService.reschedulePlan(plan.id, payload).subscribe({
      next: () => this.loadEvents(),
      error: (err) => {
        dropInfo.revert();
        this.errorMessage.set(
          err?.error?.message || 'Unable to move course due to a conflict.'
        );
      }
    });
  }

  onEventResize(resizeInfo: EventResizeDoneArg): void {
    const plan = resizeInfo.event.extendedProps['plan'] as Plan;
    if (!plan || !resizeInfo.event.start || !resizeInfo.event.end) {
      resizeInfo.revert();
      return;
    }

    const payload = {
      date: this.formatToISODate(resizeInfo.event.start),
      startsAt: this.formatToTime(resizeInfo.event.start),
      endsAt: this.formatToTime(resizeInfo.event.end)
    };

    this.calendarService.reschedulePlan(plan.id, payload).subscribe({
      next: () => this.loadEvents(),
      error: (err) => {
        resizeInfo.revert();
        this.errorMessage.set(
          err?.error?.message || 'Unable to resize course due to a conflict.'
        );
      }
    });
  }

  openCreateModal(
    dateStr?: string,
    startsAt = '09:00',
    endsAt = '11:00'
  ): void {
    this.errorMessage.set(null);
    this.editPlanId.set(null);
    this.selectedPlan.set(null);

    const openWithData = () => {
      const firstEl = this.allElements()[0];
      const defaultDept =
        this.viewDepartment() !== 'ALL'
          ? this.viewDepartment()
          : String(
              firstEl?.module?.department || Department.GENIE_INFORMATIQUE
            );
      const defaultYear =
        this.viewYear() > 0 ? this.viewYear() : firstEl?.module?.year || 1;
      const defaultSem =
        this.viewSemester() > 0
          ? this.viewSemester()
          : firstEl?.module?.semester || 1;
      const defaultGroup = this.viewGroup() === 'ALL' ? 'A' : this.viewGroup();

      this.filteredModules.set(this.modules());
      this.filteredElements.set(this.allElements());

      this.form.reset(
        {
          department: defaultDept,
          year: defaultYear,
          semester: defaultSem,
          groupName: defaultGroup,
          moduleId: firstEl?.module?.id || '',
          elementId: firstEl?.id || '',
          roomId: this.rooms()[0]?.id || '',
          date: dateStr || this.formatToISODate(new Date()),
          startsAt,
          endsAt,
          weeklyRepeats: 1,
          type: 'LECTURE'
        },
        { emitEvent: false }
      );

      this.onFormClassChange();
      this.showForm.set(true);
    };

    if (
      this.allElements().length === 0 ||
      this.rooms().length === 0 ||
      this.modules().length === 0
    ) {
      this.loadFormDependencies(() => openWithData());
    } else {
      openWithData();
    }
  }

  openEditModal(plan: Plan): void {
    this.errorMessage.set(null);
    this.editPlanId.set(plan.id);
    this.selectedPlan.set(plan);

    const populateEdit = () => {
      const modId = plan.element?.module?.id || '';
      const dept =
        plan.department ||
        plan.element?.module?.department ||
        Department.GENIE_INFORMATIQUE;
      const yr = plan.year || plan.element?.module?.year || 1;
      const sem = plan.semester || plan.element?.module?.semester || 1;

      this.filteredModules.set(this.modules());
      this.filteredElements.set(this.allElements());

      this.form.patchValue(
        {
          department: String(dept),
          year: yr,
          semester: sem,
          groupName: plan.groupName || 'A',
          moduleId: modId,
          elementId: plan.element.id,
          roomId: plan.room.id,
          date: plan.date,
          startsAt: plan.startsAt.substring(0, 5),
          endsAt: plan.endsAt.substring(0, 5),
          weeklyRepeats: plan.weeklyRepeats || 1,
          type: String(plan.type)
        },
        { emitEvent: false }
      );

      this.onFormClassChange();
      this.showForm.set(true);
    };

    if (
      this.allElements().length === 0 ||
      this.rooms().length === 0 ||
      this.modules().length === 0
    ) {
      this.loadFormDependencies(() => populateEdit());
    } else {
      populateEdit();
    }
  }

  closeModal(): void {
    this.showForm.set(false);
    this.editPlanId.set(null);
    this.selectedPlan.set(null);
    this.errorMessage.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Please fill in all required fields.');
      return;
    }

    this.errorMessage.set(null);

    const id = this.editPlanId();
    const raw = this.form.getRawValue();
    const payload = {
      date: raw.date,
      startsAt: raw.startsAt,
      endsAt: raw.endsAt,
      weeklyRepeats: Number(raw.weeklyRepeats) || 1,
      type: raw.type,
      department: raw.department,
      year: Number(raw.year) || 1,
      semester: Number(raw.semester) || 1,
      groupName: raw.groupName?.trim().toUpperCase() || 'A',
      elementId: raw.elementId,
      roomId: raw.roomId
    };

    const request$ = id
      ? this.calendarService.updatePlan(id, payload)
      : this.calendarService.createPlan(payload);

    request$.subscribe({
      next: (savedPlan) => {
        if (
          this.viewDepartment() !== 'ALL' &&
          this.viewDepartment() !== String(savedPlan.department)
        ) {
          this.viewDepartment.set('ALL');
        }
        if (this.viewYear() !== 0 && this.viewYear() !== savedPlan.year) {
          this.viewYear.set(0);
        }
        if (
          this.viewSemester() !== 0 &&
          this.viewSemester() !== savedPlan.semester
        ) {
          this.viewSemester.set(0);
        }
        if (
          this.viewGroup() !== 'ALL' &&
          this.viewGroup() !== savedPlan.groupName
        ) {
          this.viewGroup.set('ALL');
        }

        this.closeModal();
        this.refreshAvailableGroups();

        const api = this.calendarComponent?.getApi();
        if (api && savedPlan.date) {
          api.gotoDate(savedPlan.date);
        }
        this.loadEvents();
      },
      error: (err) => {
        this.errorMessage.set(
          err?.error?.message || 'Failed to save course session.'
        );
      }
    });
  }

  promptDeleteFromModal(): void {
    const current = this.selectedPlan();
    if (!current) return;
    this.closeModal();
    this.deleteTarget.set(current);
  }

  closeDeleteModal(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;

    this.calendarService.deletePlan(target.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.loadEvents();
      },
      error: (err) => {
        this.closeDeleteModal();
        this.errorMessage.set(
          err?.error?.message || 'Failed to delete course session.'
        );
      }
    });
  }

  formatToISODate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  formatToTime(date: Date): string {
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }
}
