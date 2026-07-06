import { Component, inject, OnInit, effect } from '@angular/core';
import { GradeService } from './grade.service';
import { LoginService } from '../../login/login.service';
import { Result } from '../../../models/models';

@Component({
  selector: 'student-grades',
  standalone: true,
  templateUrl: './grades.html'
})
export class Grades implements OnInit {
  private gradeService = inject(GradeService);
  private loginService = inject(LoginService);

  protected currentUser = this.loginService.userProfile;

  semester = 1;
  results: Result[] = []; // Linked directly to your custom Result interface
  isLoading = false;

  constructor() {
    // Listens to your login service signal updates natively
    effect(() => {
      const user = this.currentUser();
      if (user && (user as any).id) {
        this.loadGrades((user as any).id, this.semester);
      }
    });
  }

  ngOnInit(): void {
    const user = this.currentUser();
    if (user && (user as any).id) {
      this.loadGrades((user as any).id, this.semester);
    }
  }

  loadGrades(studentId: string, semester: number): void {
    this.isLoading = true;
    this.gradeService.listStudentResultsBySemester(studentId, semester).subscribe({
      next: (data) => {
        this.results = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to sync backend grades engine:', err);
        this.isLoading = false;
      }
    });
  }

  formatGrade(grade: number): string {
    return grade.toFixed(2);
  }

}
