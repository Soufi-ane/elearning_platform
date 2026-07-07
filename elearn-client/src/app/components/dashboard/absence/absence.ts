import { Component, inject, OnInit, signal } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { AttendaceService } from "../attendance/attendance.service";
import { Absence } from "../../../models/models";


@Component({
  selector: 'user-absence',
  imports: [],
  templateUrl: './absence_view.html',
})
export class AbsenceView implements OnInit {
  attendanceService = inject(AttendaceService);

  absences = signal<Absence[]>([]);

  constructor(private router: Router, private route : ActivatedRoute){}

  ngOnInit(): void {
    const absenceId = this.route.snapshot.paramMap.get("id");
    if(absenceId) {
      this.attendanceService.getByElementId(absenceId,null)
        .subscribe({
          next: (res) => {
            this.absences.set(res);
          },
          error: (_) => this.router.navigate(["/dashboard/attendance"])
      });
    }
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
