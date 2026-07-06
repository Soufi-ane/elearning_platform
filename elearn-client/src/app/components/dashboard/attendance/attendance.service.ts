import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Absence, AbsenceByElement } from "../../../models/models";

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
