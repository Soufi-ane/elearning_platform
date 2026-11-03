import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Department, Plan } from "../../../models/models";
import { Observable } from "rxjs";

@Injectable({ providedIn: "root" })
export class CalendarService {
  private http = inject(HttpClient);
  private apiUrl = "/api";

  getByWeek(date: Date, department: Department): Observable<Record<string, Plan[]>> {
    const dateStr = date.toISOString().split('T')[0];
    return this.http.get<Record<string, Plan[]>>(
      `${this.apiUrl}/timeTable/${dateStr}?department=${department}`
    );
  }

  createPlan(data: any): Observable<Plan> {
    return this.http.post<Plan>(`${this.apiUrl}/timeTable`, data);
  }

  updatePlan(id: string, data: any): Observable<Plan> {
    return this.http.put<Plan>(`${this.apiUrl}/timeTable/${id}`, data);
  }

  deletePlan(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/timeTable/${id}`);
  }
}
