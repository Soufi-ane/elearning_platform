import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Department, Plan } from "../../../models/models";

@Injectable({providedIn : "root"})
export class CalendarService {
  private http = inject(HttpClient);
  private apiUrl = "/api";

  getByWeek(date:Date, department: Department) {
    return this.http.get<Record<string,Plan[]>>(
      `${this.apiUrl}/timeTable/${date.toISOString().split('T')[0]}?department=${department}`
    );
  }

}
