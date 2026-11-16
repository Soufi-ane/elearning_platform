import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { RequestService } from './requests.service';
import { LoginService } from '../../login/login.service';
import {
  RequestState,
  Role,
  User,
  UserRequest
} from '../../../models/models';

@Component({
  selector: 'user-requests',
  templateUrl: './requests.html',
  standalone: true,
  imports: [CommonModule, DatePipe, FormsModule, ReactiveFormsModule]
})
export class Requests implements OnInit {
  private requestService = inject(RequestService);
  private loginService = inject(LoginService);
  private fb = inject(FormBuilder);

  user = this.loginService.userProfile;
  requests = signal<UserRequest[]>([]);
  users = signal<User[]>([]);
  isLoading = signal(true);

  filterState = signal<string>('ALL');
  filterRole = signal<string>('ALL');
  searchQuery = signal<string>('');

  showForm = signal(false);
  editId = signal<string | null>(null);
  deleteTarget = signal<UserRequest | null>(null);
  errorMessage = signal<string | null>(null);

  readonly states = [
    RequestState.PENDING,
    RequestState.IN_REVIEW,
    RequestState.IN_PROGRESS,
    RequestState.READY_FOR_PICKUP,
    RequestState.APPROVED,
    RequestState.COMPLETED,
    RequestState.REJECTED
  ];

  readonly presetTitles = [
    'Attestation de Scolarite',
    'Official Transcript (Releve de Notes)',
    'Internship Convention (Convention de Stage)',
    'Absence Justification Review',
    'Course Group Transfer',
    'Hardware / Lab Equipment Loan',
    'Teaching / Faculty Attestation'
  ];

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(160)]],
    userId: [''],
    state: [RequestState.PENDING as string],
    forcastDate: ['']
  });

  get isAdmin(): boolean {
    const r = String(this.user()?.role || '');
    return r === Role.ADMIN || r === 'ADMIN';
  }

  ngOnInit(): void {
    if (this.isAdmin) {
      this.requestService.getAllUsers().subscribe({
        next: (list) =>
          this.users.set(
            list.filter((u) => String(u.role) !== 'ADMIN')
          ),
        error: () => this.users.set([])
      });
    }
    this.loadRequests();
  }

  loadRequests(): void {
    this.isLoading.set(true);
    this.requestService
      .getRequests({
        state: this.filterState(),
        role: this.isAdmin ? this.filterRole() : undefined,
        search: this.searchQuery()
      })
      .subscribe({
        next: (data) => {
          this.requests.set(data);
          this.isLoading.set(false);
        },
        error: () => this.isLoading.set(false)
      });
  }

  onFilterChange(): void {
    this.loadRequests();
  }

  canModify(req: UserRequest): boolean {
    if (this.isAdmin) return true;
    return String(req.state) === RequestState.PENDING;
  }

  openCreateModal(): void {
    this.errorMessage.set(null);
    this.editId.set(null);
    const defaultUser = this.isAdmin
      ? this.users()[0]?.id || this.user()?.id || ''
      : this.user()?.id || '';
    this.form.reset({
      title: '',
      userId: defaultUser,
      state: RequestState.PENDING,
      forcastDate: ''
    });
    this.showForm.set(true);
  }

  openEditModal(req: UserRequest): void {
    if (!this.canModify(req)) return;
    this.errorMessage.set(null);
    this.editId.set(req.id);
    const forecastStr = req.forcastDate
      ? this.toISODate(new Date(req.forcastDate))
      : '';
    this.form.patchValue({
      title: req.title,
      userId: req.student?.id || this.user()?.id || '',
      state: String(req.state || RequestState.PENDING),
      forcastDate: forecastStr
    });
    this.showForm.set(true);
  }

  applyPresetTitle(preset: string): void {
    this.form.patchValue({ title: preset });
  }

  closeModal(): void {
    this.showForm.set(false);
    this.editId.set(null);
    this.errorMessage.set(null);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage.set(null);
    const raw = this.form.getRawValue();
    const id = this.editId();

    const forecastIso =
      this.isAdmin && raw.forcastDate
        ? new Date(`${raw.forcastDate}T12:00:00`).toISOString()
        : null;

    const req$ = id
      ? this.requestService.updateRequest(id, {
          title: raw.title!,
          state: this.isAdmin ? raw.state! : undefined,
          forcastDate: this.isAdmin ? forecastIso : undefined,
          userId: this.isAdmin ? raw.userId || undefined : undefined
        })
      : this.requestService.createRequest({
          title: raw.title!,
          userId: this.isAdmin ? raw.userId || undefined : undefined,
          state: this.isAdmin ? raw.state! : undefined,
          forcastDate: this.isAdmin ? forecastIso : undefined
        });

    req$.subscribe({
      next: () => {
        this.closeModal();
        this.loadRequests();
      },
      error: (err) => {
        this.errorMessage.set(
          err?.error?.message || 'Failed to save request.'
        );
      }
    });
  }

  onQuickStateChange(req: UserRequest, newState: string): void {
    if (!this.isAdmin) return;
    this.requestService
      .updateRequest(req.id, {
        title: req.title,
        state: newState,
        forcastDate: req.forcastDate
          ? new Date(req.forcastDate).toISOString()
          : null,
        userId: req.student?.id
      })
      .subscribe({
        next: (updated) => {
          this.requests.update((list) =>
            list.map((item) => (item.id === updated.id ? updated : item))
          );
        },
        error: (err) => {
          this.errorMessage.set(
            err?.error?.message || 'Failed to update request state.'
          );
        }
      });
  }

  openDeleteModal(req: UserRequest): void {
    if (!this.canModify(req)) return;
    this.deleteTarget.set(req);
  }

  closeDeleteModal(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.requestService.deleteRequest(target.id).subscribe({
      next: () => {
        this.closeDeleteModal();
        this.requests.update((list) =>
          list.filter((r) => r.id !== target.id)
        );
      },
      error: (err) => {
        this.closeDeleteModal();
        this.errorMessage.set(
          err?.error?.message || 'Unable to delete this request.'
        );
      }
    });
  }

  getStateClasses(state: string): string {
    const base =
      'px-2.5 py-0.5 text-[10px] rounded-sm uppercase font-medium ' +
      'tracking-wider border inline-block ';
    switch (state) {
      case RequestState.PENDING:
        return base + 'bg-[#C8A96E]/10 text-[#C8A96E] border-[#C8A96E]/30';
      case RequestState.IN_REVIEW:
        return base + 'bg-sky-950/40 text-sky-300 border-sky-500/30';
      case RequestState.IN_PROGRESS:
        return base + 'bg-amber-950/40 text-amber-300 border-amber-500/30';
      case RequestState.READY_FOR_PICKUP:
        return base + 'bg-teal-950/50 text-teal-300 border-teal-500/40';
      case RequestState.APPROVED:
      case RequestState.COMPLETED:
        return base + 'bg-[#14261C] text-[#8FA68C] border-[#2A3D30]';
      case RequestState.REJECTED:
        return base + 'bg-[#221518] text-[#C48B93] border-[#4A252C]';
      default:
        return base + 'bg-white/5 text-white/40 border-white/10';
    }
  }

  formatState(state: string): string {
    if (!state) return '';
    return state
      .toLowerCase()
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ');
  }

  private toISODate(date: Date): string {
    const yr = date.getFullYear();
    const mo = String(date.getMonth() + 1).padStart(2, '0');
    const da = String(date.getDate()).padStart(2, '0');
    return `${yr}-${mo}-${da}`;
  }
}
