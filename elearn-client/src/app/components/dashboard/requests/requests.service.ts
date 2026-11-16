import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  RequestCreatePayload,
  RequestUpdatePayload,
  User,
  UserRequest
} from '../../../models/models';

@Injectable({ providedIn: 'root' })
export class RequestService {
  private http = inject(HttpClient);
  private apiUrl = '/api/requests';

  getRequests(filters?: {
    studentId?: string;
    state?: string;
    role?: string;
    search?: string;
  }): Observable<UserRequest[]> {
    let params = new HttpParams();
    if (filters?.studentId) {
      params = params.set('studentId', filters.studentId);
    }
    if (filters?.state && filters.state !== 'ALL') {
      params = params.set('state', filters.state);
    }
    if (filters?.role && filters.role !== 'ALL') {
      params = params.set('role', filters.role);
    }
    if (filters?.search && filters.search.trim() !== '') {
      params = params.set('search', filters.search.trim());
    }
    return this.http.get<UserRequest[]>(this.apiUrl, {
      params,
      withCredentials: true
    });
  }

  createRequest(payload: RequestCreatePayload): Observable<UserRequest> {
    return this.http.post<UserRequest>(this.apiUrl, payload, {
      withCredentials: true
    });
  }

  updateRequest(
    id: string,
    payload: RequestUpdatePayload
  ): Observable<UserRequest> {
    return this.http.put<UserRequest>(`${this.apiUrl}/${id}`, payload, {
      withCredentials: true
    });
  }

  deleteRequest(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      withCredentials: true
    });
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users', {
      withCredentials: true
    });
  }
}
