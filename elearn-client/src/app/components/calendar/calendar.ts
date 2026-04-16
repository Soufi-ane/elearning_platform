import { KeyValuePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { CalendarService, Plan, PlanWithSpan } from './calendar.service';
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

  planningWithSplan = signal<Record<string, PlanWithSpan[]>>({});

  ngOnInit(): void {
    this.currentWeekStart.set(this.getPreviousMonday());
    this.calendarService.getByWeek(
      this.currentWeekStart(),
      this.loginService.userProfile()!.department.id
    ).subscribe({
      next: (res) => {
        this.planningWithSplan.set(this.generatePlanWithSpan(res));
      },
      error: (err) => console.log("Error:", err)
    });
  }


  getPreviousMonday(): Date {
    const today = new Date();
    const daysToshiftBy = 1 - today.getDay();
    const newDate = new Date();
    newDate.setDate(today.getDate() + daysToshiftBy);
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
    console.log(newPlan.rowSpan);
    return newPlan;
  }

}
