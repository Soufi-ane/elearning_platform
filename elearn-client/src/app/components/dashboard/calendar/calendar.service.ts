import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Department, Plan } from '../../../models/models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CalendarService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/timeTable';

  private toLocalISODate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  getEvents(
    start: string,
    end: string,
    department?: Department | string,
    year?: number,
    semester?: number,
    group?: string
  ): Observable<Plan[]> {
    let params = new HttpParams()
      .set('start', start)
      .set('end', end);

    if (department && department !== 'ALL') {
      params = params.set('department', department);
    }
    if (year && year > 0) {
      params = params.set('year', String(year));
    }
    if (semester && semester > 0) {
      params = params.set('semester', String(semester));
    }
    if (group && group.trim() !== '' && group !== 'ALL') {
      params = params.set('group', group.trim());
    }

    return this.http.get<Plan[]>(`${this.apiUrl}/events`, {
      params,
      withCredentials: true
    });
  }

  getByWeek(
    date: Date,
    department: Department | string,
    year?: number,
    semester?: number,
    group?: string
  ): Observable<Record<string, Plan[]>> {
    const dateStr = this.toLocalISODate(date);
    let params = new HttpParams();
    if (department && department !== 'ALL') {
      params = params.set('department', department);
    }
    if (year && year > 0) {
      params = params.set('year', String(year));
    }
    if (semester && semester > 0) {
      params = params.set('semester', String(semester));
    }
    if (group && group.trim() !== '') {
      params = params.set('group', group.trim());
    }
    return this.http.get<Record<string, Plan[]>>(
      `${this.apiUrl}/${dateStr}`,
      { params, withCredentials: true }
    );
  }

  getGroups(
    department?: Department | string,
    year?: number
  ): Observable<string[]> {
    let params = new HttpParams();
    if (department && department !== 'ALL') {
      params = params.set('department', department);
    }
    if (year && year > 0) {
      params = params.set('year', String(year));
    }
    return this.http.get<string[]>(`${this.apiUrl}/groups`, {
      params,
      withCredentials: true
    });
  }

  createPlan(data: any): Observable<Plan> {
    return this.http.post<Plan>(this.apiUrl, data, {
      withCredentials: true
    });
  }

  updatePlan(id: string, data: any): Observable<Plan> {
    return this.http.put<Plan>(`${this.apiUrl}/${id}`, data, {
      withCredentials: true
    });
  }

  reschedulePlan(
    id: string,
    data: { date: string; startsAt: string; endsAt: string; roomId?: string }
  ): Observable<Plan> {
    return this.http.patch<Plan>(`${this.apiUrl}/${id}/schedule`, data, {
      withCredentials: true
    });
  }

  deletePlan(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      withCredentials: true
    });
  }
}
