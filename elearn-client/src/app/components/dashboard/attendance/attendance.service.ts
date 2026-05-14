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

export type AbsenceByElement = {
  element : Element,
  count : number,
  status : AbsenceStatus
}

export enum AbsenceStatus {
  NORMAL,
  VERBAL_WARNING,
  WRITTEN_WARNING,
  DISCIPLINARY_HEARING
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
    return this.http.get<AbsenceByElement[]>(
      `${this.apiUrl}/absence?userId=${studentId}`
    );
  }

  getByElementId(elementId : string, userId : string|null) {
    return this.http.get<Absence[]>(
      `${this.apiUrl}/absence/${elementId}?userId=${userId}`
    );
  }

}
