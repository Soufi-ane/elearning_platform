import {KeyValuePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { CalendarService, Plan, PlanWithSpan } from './calendar.service';
import { LoginService } from '../login/login.service';
import { ActivatedRoute, Router } from '@angular/router';
import { start } from 'repl';

@Component({
  selector: 'app-calendar',
  imports: [KeyValuePipe],
  templateUrl: './calendar.html',
})
export class Calendar implements OnInit {
  private calendarService = inject(CalendarService);
  private loginService = inject(LoginService);

  constructor(private router : Router,private route : ActivatedRoute){}

  currentWeekStart = signal<Date>(new Date());
  currentWeekStr = '';
  endOfWeekStr = '';

  planningWithSplan = signal<Record<string, PlanWithSpan[]>>({});

  updateStartDate(){
    this.router.navigate(
      ["/dashboard/calendar"],
      { queryParams : { d : this.formatDate(this.currentWeekStart(),"-")}}
    );
  }

  ngOnInit(): void {
    const prevMonday = this.getPreviousMonday(new Date());
    const urlDate = this.route.snapshot.queryParamMap.get("d");
    this.init(urlDate ? this.urlStrToDate(urlDate) : prevMonday);
  }

  init(startDate : Date) {
    this.currentWeekStart.set(startDate);
    this.currentWeekStr = this.getCurrentWeekStr();
    this.endOfWeekStr = this.getEndOfWeekStr();
    this.calendarService.getByWeek(
      startDate,
      this.loginService.userProfile()!.department.id
    ).subscribe({
      next: (res) => {
        this.planningWithSplan.set(this.generatePlanWithSpan(res));
      },
      error: (err) => console.log("Error:", err)
    });

    this.updateStartDate();
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

  getCurrentWeekStr() : string {
    return this.formatDate(this.currentWeekStart(),"/");
  }

  getEndOfWeekStr() : string {
    const nextWeek = new Date(this.currentWeekStart());
    nextWeek.setDate(nextWeek.getDate() + 7);
    return this.formatDate(nextWeek, "/");
  }

  getPreviousMonday(startDate : Date): Date {
    const day = startDate.getDay();
    const daysToshiftBy = day == 0 ? 6 : day - 1;
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

  asDate(dateString: string): Date {
    return new Date(dateString);
  }

  urlStrToDate(str : string) : Date {
    const [day, month, year] = str.split("-");
    return new Date(+year, +month - 1, +day);
  }

  formatDate(date: Date, seperator : string): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2,'0');
    const day = String(date.getDate()).padStart(2,'0');
    const result = `${day}${seperator}${month}${seperator}${year}`;
    return result;
  }

  datePlus(date: Date, days : number): Date {
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

}
