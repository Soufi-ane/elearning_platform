import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserRequest } from '../../../models/models';

@Injectable({ providedIn: 'root' })
export class RequestService {
  private http = inject(HttpClient);
  private apiUrl = '/api/requests';

  getRequests(studentId?: string): Observable<UserRequest[]> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId);
    }
    return this.http.get<UserRequest[]>(this.apiUrl, { params });
  }
}
