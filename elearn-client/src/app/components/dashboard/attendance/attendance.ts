import { Component, inject, OnInit, signal } from "@angular/core";
import { AttendaceService } from "./attendance.service";
import { RouterLink } from "@angular/router";
import { AbsenceByElement, User } from "../../../models/models";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { LoginService } from "../../login/login.service";

@Component({
  selector: 'user-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './attendance.html',
})
export class Attendance implements OnInit {
  private attendanceService = inject(AttendaceService);
  private loginService = inject(LoginService);

  isAdminOrTeacher = false;
  students = signal<User[]>([]);
  searchQuery = signal<string>('');
  selectedStudent = signal<User | null>(null);
  userAbsences = signal<AbsenceByElement[]>([]);

  ngOnInit(): void {
    const user = this.loginService.userProfile();

    if (user && (user.role.toString() === 'ADMIN' || user.role.toString() === 'TEACHER')) {
      this.isAdminOrTeacher = true;
      this.loadStudents();
    } else {
      this.isAdminOrTeacher = false;
      this.loadAbsences(null);
    }
  }

  loadAbsences(studentId: string | null) {
    this.attendanceService.getByStudent(studentId).subscribe({
      next: (res) => this.userAbsences.set(res),
      error: (err) => console.error("Failed to load absences:", err)
    });
  }

  loadStudents() {
    console.log("Fetching student list...");
    this.attendanceService.getStudents().subscribe({
      next: (res) => {
        console.log("Students loaded:", res);
        this.students.set(res);
      },
      error: (err) => console.error("Error loading students list:", err)
    });
  }

  filteredStudents() {
    const query = this.searchQuery().toLowerCase();
    if (!query) return this.students();
    return this.students().filter(s =>
      `${s.firstName} ${s.lastName}`.toLowerCase().includes(query) ||
      s.username.toLowerCase().includes(query)
    );
  }

  selectStudent(student: User) {
    this.selectedStudent.set(student);
    this.loadAbsences(student.id);
  }

  resetStudentSelection() {
    this.selectedStudent.set(null);
    this.userAbsences.set([]);
  }

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2,'0');
    const day = String(date.getDate()).padStart(2,'0');
    const hours = date.getHours();
    const minutes = date.getMinutes();
    return `${hours}:${minutes} | ${day}/${month}/${year}`;
  }
}
