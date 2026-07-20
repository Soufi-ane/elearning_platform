import { Component, inject, OnInit, signal } from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { AttendaceService } from "./attendance.service";
import { CommonModule, Location } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { Absence, Role } from "../../../models/models";

@Component({
  selector: 'user-absence-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './absence-view.html',
})
export class AbsenceView implements OnInit {
  private attendanceService = inject(AttendaceService);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);

  absences = signal<Absence[]>([]);
  isAdminOrTeacher = false;
  targetUserId: string | null = null;
  elementId: string | null = null;

  ngOnInit(): void {
    this.elementId = this.route.snapshot.paramMap.get("id");
    this.targetUserId = this.route.snapshot.queryParamMap.get("userId");

    this.http.get<any>(`${this.attendanceService['apiUrl']}/users/auth`).subscribe({
      next: (user) => {
        if (user && (user.role === Role.ADMIN || user.role === Role.TEACHER)) {
          this.isAdminOrTeacher = true;
        }
      }
    });

    if (this.elementId) {
      this.loadAbsences();
    }
  }

  goBack(): void {
    this.location.back();
  }

  loadAbsences() {
    if (this.elementId) {
      this.attendanceService.getByElementId(this.elementId, this.targetUserId).subscribe({
        next: (res) => this.absences.set(res),
        error: () => this.router.navigate(["/dashboard/attendance"])
      });
    }
  }

  toggleJustification(absence: Absence) {
    if (!this.isAdminOrTeacher) return;
    const newStatus = !absence.isJustified;
    this.attendanceService.updateAbsence(absence.id, {
      isJustified: newStatus,
      type: absence.type
    }).subscribe({
      next: (updated) => {
        this.absences.update(list =>
          list.map(a => a.id === updated.id ? { ...a, isJustified: updated.isJustified } : a)
        );
      },
      error: (err) => console.log("Failed to update justification", err)
    });
  }

  deleteAbsence(absenceId: string) {
    if (!this.isAdminOrTeacher) return;
    if (confirm("Are you sure you want to delete this absence record?")) {
      this.attendanceService.deleteAbsence(absenceId).subscribe({
        next: () => {
          this.absences.update(list => list.filter(a => a.id !== absenceId));
        },
        error: (err) => console.log("Failed to delete absence", err)
      });
    }
  }

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = date.getHours();
    const minutes = date.getMinutes();
    return `${hours}:${minutes} | ${day}/${month}/${year}`;
  }
}
