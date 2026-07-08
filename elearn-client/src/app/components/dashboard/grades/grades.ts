import { Component, inject, effect, signal, computed } from '@angular/core';
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

  semester = signal<number | null>(null);
  results = signal<Result[]>([]);
  isLoading = signal<boolean>(false);

  availableSemesters = computed(() => {
    const user = this.currentUser() as any;
    if (!user) return [];

    const currentSemester = Number(user.semester ?? 1);
    const numPreviousSemesters = currentSemester - 1;
    if (numPreviousSemesters < 1) return [];
    return Array.from({ length: numPreviousSemesters }, (_, i) => i + 1);
  });

  constructor() {
    effect(() => {
      const user = this.currentUser();
      if (!user || !user.id) return;
      if (this.semester() === null) {
        const current = Number((user as any).semester ?? user.year ?? 1);
        this.semester.set(current > 1 ? current - 1 : 1);
        return;
      }

      this.loadGrades(user.id, this.semester()!);
    }, { allowSignalWrites: true });
  }

  loadGrades(studentId: string, semester: number): void {
    this.isLoading.set(true);
    this.gradeService.listStudentResultsBySemester(studentId, semester).subscribe({
      next: (data) => {
        this.results.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load grades:', err);
        this.isLoading.set(false);
      }
    });
  }

  onSemesterChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    this.semester.set(Number(selectElement.value));
  }

  formatGrade(grade: number): string {
    return grade ? grade.toFixed(2) : '0.00';
  }
}
