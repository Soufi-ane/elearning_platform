import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, Observable, of, tap } from 'rxjs';
import { User } from '../../models/models';

@Injectable({ providedIn: 'root' })
export class LoginService {
  private http = inject(HttpClient);
  private apiUrl = '/api';
  userProfile = signal<User | null>(null);

  post<LoginRequest>(payload: LoginRequest): Observable<User> {
    return this.http.post<User>(
      `${this.apiUrl}/users/login`,
      payload,
      { withCredentials: true }
    );
  }

  logout() {
    return this.http.post(
      `${this.apiUrl}/users/logout`,
      {},
      { withCredentials: true }
    );
  }

  getProfileOptions(): Observable<{
    roles: string[];
    departments: string[];
    studyModes: string[];
  }> {
    return this.http.get<{
      roles: string[];
      departments: string[];
      studyModes: string[];
    }>(`${this.apiUrl}/users/options`, { withCredentials: true });
  }

  checkAuth() {
    return this.http.get<User>(
      `${this.apiUrl}/users/auth`,
      { withCredentials: true }
    ).pipe(
      tap({
        next: (user) => this.userProfile.set(user),
        error: () => this.userProfile.set(null)
      }),
      catchError(() => {
        this.userProfile.set(null);
        return of(null);
      })
    );
  }

  listUsers(role?: string, query?: string): Observable<User[]> {
    let params = new HttpParams();
    if (role) params = params.set('role', role);
    if (query) params = params.set('query', query);
    return this.http.get<User[]>(`${this.apiUrl}/users`, {
      params,
      withCredentials: true
    });
  }

  updateUserProfile(id: string, payload: Record<string, unknown>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/users/${id}`, payload, {
      withCredentials: true
    }).pipe(
      tap((updated) => {
        if (this.userProfile()?.id === updated.id) {
          this.userProfile.set(updated);
        }
      })
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<string> {
    return this.http.put(
      `${this.apiUrl}/users/me/password`,
      { currentPassword, newPassword },
      { withCredentials: true, responseType: 'text' }
    );
  }

  uploadProfilePicture(id: string, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<User>(`${this.apiUrl}/users/${id}/picture`, formData, {
      withCredentials: true
    }).pipe(
      tap((updated) => {
        if (this.userProfile()?.id === updated.id) {
          this.userProfile.set(updated);
        }
      })
    );
  }

  deleteProfilePicture(id: string): Observable<User> {
    return this.http.delete<User>(`${this.apiUrl}/users/${id}/picture`, {
      withCredentials: true
    }).pipe(
      tap((updated) => {
        if (this.userProfile()?.id === updated.id) {
          this.userProfile.set(updated);
        }
      })
    );
  }

  createUser(payload: Record<string, unknown>): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/users/register`, payload, {
      withCredentials: true
    });
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${id}`, {
      withCredentials: true
    });
  }
}
