import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RequestService } from './requests.service';
import { UserRequest } from '../../../models/models';

@Component({
  selector: 'user-requests',
  templateUrl: './requests.html',
  standalone: true,
  imports: [DatePipe]
})
export class Requests implements OnInit {
  private requestService = inject(RequestService);

  requests = signal<UserRequest[]>([]);
  isLoading = signal(true);

  ngOnInit() {
    this.requestService.getRequests().subscribe({
      next: (data) => {
        this.requests.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  getStateClasses(state: string): string {
    const base = "px-2 py-0.5 text-[10px] rounded uppercase font-medium tracking-wider border ";

    switch (state) {
      case 'PENDING':
        return base + "bg-[#C8A96E]/10 text-[#C8A96E] border-[#C8A96E]/20";
      case 'IN_PROGRESS':
        return base + "bg-[#8FA68C]/10 text-[#8FA68C] border-[#8FA68C]/20";
      case 'COMPLETED':
        return base + "bg-[#1A302F] text-[#8FA68C] border-[#1A302F]";
      default:
        return base + "bg-white/5 text-white/40 border-white/10";
    }
  }

  formatState(state: string): string {
    if (!state) return '';
    return state
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}
