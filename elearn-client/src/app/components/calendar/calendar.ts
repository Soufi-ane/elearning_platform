import { KeyValuePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { CalendarService, Plan, PlanType, PlanWithSpan } from './calendar.service';
import { LoginService } from '../login/login.service';

@Component({
  selector: 'app-calendar',
  imports: [KeyValuePipe],
  templateUrl: './calendar.html',
})
export class Calendar implements OnInit {
  private calendarService = inject(CalendarService);
  private loginService = inject(LoginService);

  currentWeekStart = signal<Date>(new Date());
  currentWeekStr = '';
  endOfWeekStr = '';

  planningWithSplan = signal<Record<string, PlanWithSpan[]>>({});

  ngOnInit(): void {
    const prevMonday = this.getPreviousMonday(new Date());
    this.init(prevMonday);
  }

  init(startDate: Date) {
    this.currentWeekStart.set(startDate);
    this.currentWeekStr = this.getCurrentWeekStr();
    this.endOfWeekStr = this.getEndOfWeekStr()
    this.calendarService.getByWeek(
      startDate,
      this.loginService.userProfile()!.department.id
    ).subscribe({
      next: (res) => {
        this.planningWithSplan.set(this.generatePlanWithSpan(res));
      },
      error: _ => { }
    });
  }

  onPrevious() {
    const prevWeek = new Date(this.currentWeekStart().getTime());
    prevWeek.setDate(prevWeek.getDate() - 1);
    this.init(this.getPreviousMonday(prevWeek));
  }

  onNext() {
    const nextWeek = this.currentWeekStart();
    nextWeek.setDate(nextWeek.getDate() + 7);
    this.init(nextWeek);
  }

  getCurrentWeekStr(): string {
    return this.formatDate(this.currentWeekStart());
  }

  getEndOfWeekStr(): string {
    const nextWeek = new Date(this.currentWeekStart());
    nextWeek.setDate(nextWeek.getDate() + 7);
    return this.formatDate(nextWeek);
  }

  getPreviousMonday(startDate: Date): Date {
    const day = startDate.getDay();
    const daysToshiftBy = day == 0 ? 6 : day - 1;
    const newDate = new Date(startDate.getTime());
    newDate.setDate(newDate.getDate() - daysToshiftBy);
    newDate.setHours(0, 0, 0, 0);
    console.log("previous monday : ", newDate.toDateString());
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

  asDate(dateString: string): Date {
    return new Date(dateString);
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const result = `${day}/${month}/${year}`;
    return result;
  }

  datePlus(date: Date, days: number): Date {
    const newDate = date;
    newDate.setDate(newDate.getDate() + days);
    return newDate;
  }

  generatePlanWithSpan(plannings: Record<string, Plan[]>): Record<string, PlanWithSpan[]> {
    let planningsWithSpan: Record<string, PlanWithSpan[]> = {};
    Object.entries(plannings).forEach(([date, plans]) => {
      planningsWithSpan[date] = plans.map((m: Plan) => this.addSpan(m));
    });
    return planningsWithSpan;
  }

  addSpan(p: Plan): PlanWithSpan {
    const newPlan: PlanWithSpan = {
      room: p.room,
      type: p.type,
      element: p.element,
      startsAt: p.startsAt,
      endsAt: p.endsAt,
      rowSpan: []
    }
    let start = newPlan.startsAt.substring(0, 5);
    let end = newPlan.endsAt.substring(0, 5);
    for (let i = 0; i < this.timeSlots.length; i++) {
      if (this.timeSlots[i] == start) newPlan.rowSpan[0] = i;
      if (this.timeSlots[i] == end) newPlan.rowSpan[1] = i;
    }
    return newPlan;
  }

  temp: Record<string, Plan[]> = {
    "2026-04-06": [],
    "2026-04-07": [
      {
        startsAt: "09:00:00",
        endsAt: "11:00:00",
        type: PlanType.LECTURE,
        element: {
          id: "ca1dc411-2e95-4d41-b442-f30c10901bdc",
          name: "Data Science",
          module: { id: "767bf8aa-05ff-4b20-a2b8-2ae19441e669", name: "Programmation Python" },
          teacher: { id: "4b8188b9-cee2-45bf-adaa-7be833b97634", firstName: "Rachid", lastName: "Saadane" }
        },
        room: { label: "TP2", floor: 3, campus: "Agdal 1" }
      },
      {
        startsAt: "14:00:00",
        endsAt: "16:00:00",
        type: PlanType.LECTURE,
        element: {
          id: "dfdcf5d0-9be5-4eb2-a66e-36f898f48baf",
          name: "Python POO",
          module: { id: "767bf8aa-05ff-4b20-a2b8-2ae19441e669", name: "Programmation Python" },
          teacher: { id: "4b8188b9-cee2-45bf-adaa-7be833b97634", firstName: "Rachid", lastName: "Saadane" }
        },
        room: { label: "AMPHI 7", floor: 3, campus: "Agdal 1" }
      }
    ],
    "2026-04-08": [],
    "2026-04-09": [
      {
        startsAt: "10:30:00",
        endsAt: "12:30:00",
        type: PlanType.LECTURE,
        element: {
          id: "a40d8939-fd6d-44a2-9e66-291fc9620e18",
          name: "MSSQL Server",
          module: { id: "23b0e1db-c76b-4298-8d80-499bbbea372b", name: "Base De donnee" },
          teacher: { id: "4b8188b9-cee2-45bf-adaa-7be833b97634", firstName: "Rachid", lastName: "Saadane" }
        },
        room: { label: "AMPHI 7", floor: 3, campus: "Agdal 1" }
      },
      {
        startsAt: "15:30:00",
        endsAt: "17:30:00",
        type: PlanType.LECTURE,
        element: {
          id: "d3d54458-e80c-42fc-b607-b9d391c680ef",
          name: "ENGLISH 1",
          module: { id: "935ad20c-600b-4691-8cba-6727189a024d", name: "Langues entrangères" },
          teacher: { id: "4b8188b9-cee2-45bf-adaa-7be833b97634", firstName: "Rachid", lastName: "Saadane" }
        },
        room: { label: "AMPHI 3", floor: 3, campus: "Medina 1" }
      }
    ],
    "2026-04-10": [
      {
        startsAt: "08:30:00",
        endsAt: "10:30:00",
        type: PlanType.LECTURE,
        element: {
          id: "15061634-ee90-4b68-8238-fb8e18b55e7b",
          name: "FRENCH 1",
          module: { id: "935ad20c-600b-4691-8cba-6727189a024d", name: "Langues entrangères" },
          teacher: { id: "4b8188b9-cee2-45bf-adaa-7be833b97634", firstName: "Rachid", lastName: "Saadane" }
        },
        room: { label: "AMPHI 3", floor: 3, campus: "Medina 1" }
      },
      {
        startsAt: "15:00:00",
        endsAt: "17:00:00",
        type: PlanType.LECTURE,
        element: {
          id: "35b06b2c-dcbd-4377-a5ec-af1679c6dcdf",
          name: "CMS",
          module: { id: "deff15be-114a-49b2-8c44-5e573794b7f0", name: "CMS" },
          teacher: { id: "4b8188b9-cee2-45bf-adaa-7be833b97634", firstName: "Rachid", lastName: "Saadane" }
        },
        room: { label: "TP2", floor: 3, campus: "Agdal 1" }
      }
    ],
    "2026-04-11": []
  };

}
