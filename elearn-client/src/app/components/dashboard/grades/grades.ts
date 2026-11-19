import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GradeService } from './grade.service';
import { LoginService } from '../../login/login.service';
import {
  Element,
  Result,
  SemesterSummary,
  UserBase
} from '../../../models/models';

@Component({
  selector: 'student-grades',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './grades.html'
})
export class Grades {
  private gradeService = inject(GradeService);
  private loginService = inject(LoginService);

  protected currentUser = this.loginService.userProfile;

  isStudent = computed(() => this.currentUser()?.role === 'STUDENT');
  isTeacher = computed(() => this.currentUser()?.role === 'TEACHER');
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  semester = signal<number | null>(null);
  summary = signal<SemesterSummary | null>(null);
  results = signal<Result[]>([]);
  elements = signal<Element[]>([]);
  students = signal<UserBase[]>([]);
  isLoading = signal<boolean>(false);
  errorMsg = signal<string>('');

  departments = ['GI', 'GC', 'GE', 'GM'];
  groups = ['A', 'B', 'C'];

  filterDepartment = signal<string>('');
  filterYear = signal<string>('');
  filterSemester = signal<string>('');
  filterGroup = signal<string>('');
  filterElementId = signal<string>('');
  searchQuery = signal<string>('');

  showModal = signal<boolean>(false);
  editingResult = signal<Result | null>(null);
  formElementId = signal<string>('');
  formStudentId = signal<string>('');
  formGrade = signal<number>(10);
  formRemarks = signal<string>('');

  availableSemesters = computed(() => {
    const user = this.currentUser() as any;
    if (!user) return [];
    const current = Number(user.semester ?? 1);
    const count = current - 1;
    if (count < 1) return [];
    return Array.from({ length: count }, (_, i) => i + 1);
  });

  private loadedStaffId = '';

  constructor() {
    effect(() => {
      const user = this.currentUser();
      if (!user || !user.id) return;

      if (user.role === 'STUDENT') {
        if (this.semester() === null) {
          const current = Number((user as any).semester ?? 1);
          this.semester.set(current > 1 ? current - 1 : 1);
          return;
        }
        this.loadStudentSummary(user.id, this.semester()!);
      } else if (this.loadedStaffId !== user.id) {
        this.loadedStaffId = user.id;
        this.loadStaffMetadata();
        this.loadManagedGrades();
      }
    }, { allowSignalWrites: true });
  }

  loadStudentSummary(studentId: string, semester: number): void {
    this.isLoading.set(true);
    this.gradeService.getSemesterSummary(studentId, semester).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.results.set(data.results);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  loadStaffMetadata(): void {
    this.gradeService.listControllableElements().subscribe({
      next: (els) => this.elements.set(els)
    });
    this.gradeService.listStudents().subscribe({
      next: (st) => this.students.set(st)
    });
  }

  loadManagedGrades(): void {
    this.isLoading.set(true);
    this.gradeService.searchGrades({
      department: this.filterDepartment() || undefined,
      year: this.filterYear() ? Number(this.filterYear()) : null,
      semester: this.filterSemester() ? Number(this.filterSemester()) : null,
      groupName: this.filterGroup() || undefined,
      elementId: this.filterElementId() || undefined,
      query: this.searchQuery() || undefined
    }).subscribe({
      next: (data) => {
        this.results.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  openCreateModal(): void {
    this.editingResult.set(null);
    this.errorMsg.set('');
    this.formElementId.set(this.elements()[0]?.id ?? '');
    this.formStudentId.set(this.students()[0]?.id ?? '');
    this.formGrade.set(12);
    this.formRemarks.set('');
    this.showModal.set(true);
  }

  openEditModal(result: Result): void {
    this.editingResult.set(result);
    this.errorMsg.set('');
    this.formElementId.set(result.element.id);
    this.formStudentId.set(result.student.id);
    this.formGrade.set(result.grade);
    this.formRemarks.set(result.remarks ?? '');
    this.showModal.set(true);
  }

  saveGrade(): void {
    this.errorMsg.set('');
    const editing = this.editingResult();
    const obs = editing
      ? this.gradeService.updateGrade(editing.id, {
          grade: Number(this.formGrade()),
          remarks: this.formRemarks()
        })
      : this.gradeService.createGrade({
          studentId: this.formStudentId(),
          elementId: this.formElementId(),
          grade: Number(this.formGrade()),
          remarks: this.formRemarks()
        });

    obs.subscribe({
      next: () => {
        this.showModal.set(false);
        this.loadManagedGrades();
      },
      error: (err) => {
        this.errorMsg.set(err?.error?.message ?? 'Failed to save grade');
      }
    });
  }

  removeGrade(result: Result): void {
    const name = `${result.student.firstName} ${result.student.lastName}`;
    if (!confirm(`Delete grade for ${name}?`)) return;
    this.gradeService.deleteGrade(result.id).subscribe({
      next: () => this.loadManagedGrades()
    });
  }

  onSemesterChange(event: Event): void {
    this.semester.set(Number((event.target as HTMLSelectElement).value));
  }

  formatGrade(grade: number): string {
    return Number(grade ?? 0).toFixed(2);
  }

  fullName(u?: UserBase | null): string {
    return u ? `${u.firstName} ${u.lastName}` : '—';
  }
}
