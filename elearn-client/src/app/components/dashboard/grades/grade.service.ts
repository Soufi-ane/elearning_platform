import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Result } from '../../../models/models';

@Injectable({
  providedIn: 'root'
})
export class GradeService {
  private http = inject(HttpClient);
  private baseUrl = "/api/reuslts";

  listStudentResultsBySemester(studentId: string, semester: number): Observable<Result[]> {
    return this.http.get<Result[]>(`${this.baseUrl}/${studentId}/${semester}`);
  }
}


