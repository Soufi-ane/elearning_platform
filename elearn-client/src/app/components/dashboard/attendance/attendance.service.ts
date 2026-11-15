import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Absence,
  AbsenceByElement,
  AbsenceCreateRequest,
  AbsenceUpdateRequest,
  User
} from '../../../models/models';

@Injectable({ providedIn: 'root' })
export class AttendaceService {
  private http = inject(HttpClient);
  public apiUrl = '/api';

  getByStudent(studentId: string | null): Observable<AbsenceByElement[]> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('userId', studentId);
    }
    return this.http.get<AbsenceByElement[]>(`${this.apiUrl}/absence`, {
      params,
      withCredentials: true
    });
  }

  getByElementId(
    elementId: string,
    userId: string | null
  ): Observable<Absence[]> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId);
    }
    return this.http.get<Absence[]>(`${this.apiUrl}/absence/${elementId}`, {
      params,
      withCredentials: true
    });
  }

  getManagedAbsences(filters?: {
    department?: string;
    year?: number;
    semester?: number;
    group?: string;
    elementId?: string;
    studentId?: string;
    searchName?: string;
  }): Observable<Absence[]> {
    let params = new HttpParams();
    if (filters?.department) {
      params = params.set('department', filters.department);
    }
    if (filters?.year && filters.year > 0) {
      params = params.set('year', String(filters.year));
    }
    if (filters?.semester && filters.semester > 0) {
      params = params.set('semester', String(filters.semester));
    }
    if (filters?.group && filters.group !== 'ALL') {
      params = params.set('group', filters.group);
    }
    if (filters?.elementId) {
      params = params.set('elementId', filters.elementId);
    }
    if (filters?.studentId) {
      params = params.set('studentId', filters.studentId);
    }
    if (filters?.searchName && filters.searchName.trim() !== '') {
      params = params.set('searchName', filters.searchName.trim());
    }
    return this.http.get<Absence[]>(`${this.apiUrl}/absence/manage`, {
      params,
      withCredentials: true
    });
  }

  createAbsence(request: AbsenceCreateRequest): Observable<Absence> {
    return this.http.post<Absence>(`${this.apiUrl}/absence`, request, {
      withCredentials: true
    });
  }

  updateAbsence(
    id: string,
    request: AbsenceUpdateRequest
  ): Observable<Absence> {
    return this.http.put<Absence>(`${this.apiUrl}/absence/${id}`, request, {
      withCredentials: true
    });
  }

  deleteAbsence(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/absence/${id}`, {
      withCredentials: true
    });
  }

  getStudents(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users?role=STUDENT`, {
      withCredentials: true
    });
  }
}
