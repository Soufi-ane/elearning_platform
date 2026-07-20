import { Component, inject, OnInit, signal } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { HttpClient } from "@angular/common/http";
import { RouterLink } from "@angular/router";
import { Absence } from "../../../models/models";

@Component({
  selector: 'admin-absence',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-absence.html',
})
export class AdminAbsence implements OnInit {
  private http = inject(HttpClient);
  private apiUrl = "/api/v1/absence/manage";

  absences = signal<Absence[]>([]);
  searchName = signal<string>('');
  department = signal<string>('');

  ngOnInit(): void {
    this.loadManagedAbsences();
  }

  loadManagedAbsences() {
    let url = this.apiUrl;
    const params: string[] = [];

    if (this.searchName()) {
      params.push(`searchName=${encodeURIComponent(this.searchName())}`);
    }
    if (this.department()) {
      params.push(`department=${encodeURIComponent(this.department())}`);
    }

    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }

    this.http.get<Absence[]>(url).subscribe({
      next: (res) => this.absences.set(res),
      error: (err) => console.log("Error loading managed absences:", err)
    });
  }

  onSearchChange(event: any) {
    this.searchName.set(event);
    this.loadManagedAbsences();
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
