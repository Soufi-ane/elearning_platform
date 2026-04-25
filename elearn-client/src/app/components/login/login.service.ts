import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { catchError, Observable, of, tap } from "rxjs";

type LoginRequest = {
  usernameOrEmail : string,
  password : string
}

type User = {
  firstName : string,
  lastName : string,
  username : string,
  email : string,
  dateOfBirth : Date,
  role : Role,
  department : Department,
  studyMode : StudyMode,
  year : number
}

enum StudyMode {
  HYBRID,
  ON_SITE,
  REMOTE
}

type Department = {
  id : string,
  name : string
}

enum Role {
  STUDENT,
  TEACHER,
  ADMIN
}

@Injectable({providedIn : "root"})
export class LoginService {
  private http = inject(HttpClient);
  private apiUrl = "/api";

  userProfile = signal<User|null>(null);

  post<LoginRequest>(payload:LoginRequest): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/users/login`,payload);
  }

  logout() {
    return this.http.post(
      `${this.apiUrl}/users/logout`
    ,{});
  }

  checkAuth() {
    return this.http.get<User>(`${this.apiUrl}/users/auth`,{withCredentials : true})
    .pipe(
      tap({
        next : (user) => this.userProfile.set(user),
        error : () => this.userProfile.set(null)
      }),
      catchError(()=> {
        this.userProfile.set(null);
        return of(null);
      })
    );
  }
}
