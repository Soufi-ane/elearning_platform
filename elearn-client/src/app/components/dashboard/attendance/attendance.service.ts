import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Absence, AbsenceByElement, AbsenceType, User } from "../../../models/models";

@Injectable({ providedIn: "root" })
export class AttendaceService {
  private http = inject(HttpClient);
  public apiUrl = "/api";

getByStudent(studentId: string | null) {
  const url = studentId ? `${this.apiUrl}/absence?userId=${studentId}` : `${this.apiUrl}/absence`;
  return this.http.get<AbsenceByElement[]>(url, { withCredentials: true });
}

  getByElementId(elementId: string, userId: string | null) {
    const url = userId
      ? `${this.apiUrl}/absence/${elementId}?userId=${userId}`
      : `${this.apiUrl}/absence/${elementId}`;
    return this.http.get<Absence[]>(url);
  }

  updateAbsence(id: string, request: { isJustified: boolean; type: AbsenceType }) {
    return this.http.put<Absence>(`${this.apiUrl}/absence/${id}`, request);
  }

  deleteAbsence(id: string) {
    return this.http.delete<void>(`${this.apiUrl}/absence/${id}`);
  }

  getStudents() {
    return this.http.get<User[]>(`${this.apiUrl}/users?role=STUDENT`, { withCredentials: true });
  }
}

