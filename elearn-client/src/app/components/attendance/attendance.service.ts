import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Element } from "../calendar/calendar.service";

export type Absence = {
  id : string,
  dateTime: string,
  type : AbsenceType,
  isJustified : boolean,
  student : UserInfo,
  element : Element
}

export type UserInfo = {
  id: string,
  firstName: string,
  lastName: string
}

export enum AbsenceType {
  CLASS,
  EXAM,
  OTHER
}

@Injectable({providedIn : "root"})
export class AttendaceService {
  private http = inject(HttpClient);
  private apiUrl = "/api";

  getByStudent(studentId : string|null) {
    return this.http.get<Absence[]>(
      `${this.apiUrl}/absence?userId=${studentId}`
    );
  }

}
