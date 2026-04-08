import { HttpClient } from "@angular/common/http";
import { inject, Injectable, signal } from "@angular/core";
import { Observable } from "rxjs";

type LoginRequest = {
  usernameOrEmail : string,
  password : string
}

type LoginResponse = {
  user : User,
  token : string
}

type User = {
  firstName : string,
  lastName : string,
  username : string,
  email : string,
  dateOfBirth : Date,
  role : Role,
  departmentId : string
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

  userProfile = signal<LoginResponse|null>(null);

  post<LoginRequest>(payload:LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/users/login`,payload);
  }
}
