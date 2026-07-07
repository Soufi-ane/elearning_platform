import { Component, inject, effect, signal } from '@angular/core';
import { GradeService } from './grade.service';
import { LoginService } from '../../login/login.service';
import { Result } from '../../../models/models';

@Component({
  selector: 'student-grades',
  standalone: true,
  templateUrl: './grades.html'
})
export class Grades {
  private gradeService = inject(GradeService);
  private loginService = inject(LoginService);

  protected currentUser = this.loginService.userProfile;

  semester = 1;
  results = signal<Result[]>([]);
  isLoading = signal<boolean>(false);

  constructor() {
    effect(() => {
      const user = this.currentUser();
      if (user && (user as any).id) {
        this.loadGrades((user as any).id, this.semester);
      }
    });
  }

  loadGrades(studentId: string, semester: number): void {
    this.isLoading.set(true);

    this.gradeService.listStudentResultsBySemester(studentId, semester).subscribe({
      next: (data) => {
        this.results.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to sync backend grades engine:', err);
        this.isLoading.set(false);
      }
    });
  }

  formatGrade(grade: number): string {
    return grade.toFixed(2);
  }

}
