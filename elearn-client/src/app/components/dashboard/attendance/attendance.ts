import { Component, inject, OnInit, signal } from "@angular/core";
import { AbsenceByElement, AttendaceService } from "./attendance.service";
import { RouterLink } from "@angular/router";

@Component({
  selector: 'user-attendance',
  imports: [ RouterLink ],
  templateUrl: './attendance.html',
})
export class Attendance implements OnInit {
  private attendaceService = inject(AttendaceService);

  userAbsences = signal<AbsenceByElement[]>([]);

  ngOnInit(): void {
    this.attendaceService.getByStudent(null)
      .subscribe({
        next: (res) => {
          this.userAbsences.set(res);
        },
        error: (err) => console.log("Error:", err)
      });
  }

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2,'0');
    const day = String(date.getDate()).padStart(2,'0');
    const hours = date.getHours()
    const minutes = date.getMinutes();
    const result = `${hours}:${minutes} | ${day}/${month}/${year}`;
    return result;
  }
}
