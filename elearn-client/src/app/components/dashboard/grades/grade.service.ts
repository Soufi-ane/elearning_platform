import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Element,
  Result,
  SemesterSummary,
  UserBase
} from '../../../models/models';

export interface GradeSearchFilters {
  department?: string;
  year?: number | null;
  semester?: number | null;
  groupName?: string;
  elementId?: string;
  teacherId?: string;
  query?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GradeService {
  private http = inject(HttpClient);
  private baseUrl = '/api/results';

  getSemesterSummary(
    studentId: string,
    semester: number
  ): Observable<SemesterSummary> {
    return this.http.get<SemesterSummary>(
      `${this.baseUrl}/summary/${studentId}/${semester}`
    );
  }

  searchGrades(filters: GradeSearchFilters): Observable<Result[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, val]) => {
      if (val !== undefined && val !== null && String(val).trim() !== '') {
        params = params.set(key, String(val));
      }
    });
    return this.http.get<Result[]>(`${this.baseUrl}/manage`, { params });
  }

  listControllableElements(): Observable<Element[]> {
    return this.http.get<Element[]>(`${this.baseUrl}/elements`);
  }

  listStudents(
    department?: string,
    year?: number | null,
    groupName?: string
  ): Observable<UserBase[]> {
    let params = new HttpParams();
    if (department) params = params.set('department', department);
    if (year) params = params.set('year', String(year));
    if (groupName) params = params.set('groupName', groupName);
    return this.http.get<UserBase[]>(`${this.baseUrl}/students`, { params });
  }

  createGrade(payload: {
    studentId: string;
    elementId: string;
    grade: number;
    remarks: string;
  }): Observable<Result> {
    return this.http.post<Result>(this.baseUrl, payload);
  }

  updateGrade(
    id: string,
    payload: { grade: number; remarks: string }
  ): Observable<Result> {
    return this.http.put<Result>(`${this.baseUrl}/${id}`, payload);
  }

  deleteGrade(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
