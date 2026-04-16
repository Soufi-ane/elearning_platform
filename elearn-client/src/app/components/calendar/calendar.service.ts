import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";

export type Plan = {
  startsAt: string,
  endsAt : string,
  type : PlanType,
  element : Element,
  room : Room,
}

export type Room = {
  label : string,
  floor : number,
  campus : string
}

export type Element = {
  id : string,
  name : string,
  module : Module,
  teacher : BaseUser
}

export type BaseUser = {
  id : string,
  firstName : string,
  lastName : string
}

export type Module = {
  id : string,
  name : string
}

export enum PlanType {
  LECTURE,
  EXAM
}

export type PlanWithSpan = Plan & {
  rowSpan : number[]
};

@Injectable({providedIn : "root"})
export class CalendarService {
  private http = inject(HttpClient);
  private apiUrl = "/api";

  getByWeek(date:Date, departmentId : string): Observable<Record<string,Plan[]>> {
    return this.http.get<Record<string,Plan[]>>(
      `${this.apiUrl}/timeTable/${date.toISOString().split('T')[0]}?departmentId=${departmentId}`
    );
  }

}
