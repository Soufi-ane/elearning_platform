import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AttendaceService } from './attendance.service';
import { LoginService } from '../../login/login.service';
import { Absence, AbsenceType, Role } from '../../../models/models';

@Component({
  selector: 'user-absence-view',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './absence-view.html'
})
export class AbsenceView implements OnInit {
  private attendanceService = inject(AttendaceService);
  private loginService = inject(LoginService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private location = inject(Location);
  private fb = inject(FormBuilder);

  absences = signal<Absence[]>([]);
  targetUserId: string | null = null;
  elementId: string | null = null;

  editingAbsence = signal<Absence | null>(null);
  deleteTarget = signal<Absence | null>(null);

  form = this.fb.group({
    date: ['', Validators.required],
    time: ['09:00', Validators.required],
    type: [AbsenceType.CLASS as string, Validators.required],
    isJustified: [false]
  });

  get isAdminOrTeacher(): boolean {
    const roleStr = String(this.loginService.userProfile()?.role || '');
    return (
      roleStr === Role.ADMIN ||
      roleStr === Role.TEACHER ||
      roleStr === 'ADMIN' ||
      roleStr === 'TEACHER'
    );
  }

  ngOnInit(): void {
    this.elementId = this.route.snapshot.paramMap.get('id');
    this.targetUserId = this.route.snapshot.queryParamMap.get('userId');
    if (this.elementId) {
      this.loadAbsences();
    }
  }

  goBack(): void {
    this.location.back();
  }

  loadAbsences(): void {
    if (!this.elementId) return;
    this.attendanceService
      .getByElementId(this.elementId, this.targetUserId)
      .subscribe({
        next: (res) => this.absences.set(res),
        error: () => this.router.navigate(['/dashboard/attendance'])
      });
  }

  toggleJustification(absence: Absence): void {
    if (!this.isAdminOrTeacher) return;
    this.attendanceService
      .updateAbsence(absence.id, {
        isJustified: !absence.isJustified,
        type: absence.type
      })
      .subscribe({
        next: (updated) => {
          this.absences.update((list) =>
            list.map((a) => (a.id === updated.id ? updated : a))
          );
        }
      });
  }

  openEditModal(absence: Absence): void {
    this.editingAbsence.set(absence);
    const dt = new Date(absence.dateTime);
    const dateStr = `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(
      2,
      '0'
    )}-${String(dt.getDate()).padStart(2, '0')}`;
    const timeStr = `${String(dt.getHours()).padStart(2, '0')}:${String(
      dt.getMinutes()
    ).padStart(2, '0')}`;
    this.form.patchValue({
      date: dateStr,
      time: timeStr,
      type: String(absence.type),
      isJustified: absence.isJustified
    });
  }

  saveEdit(): void {
    const current = this.editingAbsence();
    if (!current || this.form.invalid) return;
    const raw = this.form.getRawValue();
    const dateTime = `${raw.date}T${raw.time}:00`;
    this.attendanceService
      .updateAbsence(current.id, {
        dateTime,
        type: raw.type!,
        isJustified: !!raw.isJustified
      })
      .subscribe((updated) => {
        this.absences.update((list) =>
          list.map((a) => (a.id === updated.id ? updated : a))
        );
        this.editingAbsence.set(null);
      });
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.attendanceService.deleteAbsence(target.id).subscribe(() => {
      this.absences.update((list) => list.filter((a) => a.id !== target.id));
      this.deleteTarget.set(null);
    });
  }

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  }
}
