import { Component, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { ModuleService } from './modules.service';
import { LoginService } from '../../login/login.service';
import { Module, Role } from '../../../models/models';

@Component({
  selector: 'app-module-list',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './module-list.html'
})
export class ModuleList implements OnInit {
  private svc = inject(ModuleService);
  private router = inject(Router);
  private loginService = inject(LoginService);
  private fb = inject(FormBuilder);

  user = this.loginService.userProfile()!;
  modules = signal<Module[]>([]);
  showForm = signal(false);
  editId = signal<string | null>(null);
  deleteTarget = signal<Module | null>(null);

  filterDepartment = signal<string>('');
  filterYear = signal<number>(0);
  filterSemester = signal<number>(0);

  form = this.fb.group({
    name: ['', Validators.required],
    year: [1, [Validators.required, Validators.min(1), Validators.max(5)]],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(2)]],
    department: ['', Validators.required]
  });

  get isAdmin(): boolean {
    return this.user?.role === Role.ADMIN ||
      (this.user?.role as unknown as string) === 'ADMIN';
  }

  get isAdminOrTeacher(): boolean {
    const roleStr = String(this.user?.role);
    return roleStr === Role.ADMIN || roleStr === Role.TEACHER ||
      roleStr === 'ADMIN' || roleStr === 'TEACHER';
  }

  ngOnInit(): void {
    if (!this.isAdminOrTeacher && this.user) {
      this.filterDepartment.set(String(this.user.department || ''));
      this.filterYear.set(this.user.year || 0);
      this.filterSemester.set(this.user.semester || 0);
    }
    this.load();
  }

  load(): void {
    this.svc.getModules(
      this.filterDepartment() || undefined,
      this.filterYear() || undefined,
      this.filterSemester() || undefined
    ).subscribe({
      next: (data) => this.modules.set(data)
    });
  }

  onFilterChange(): void {
    this.load();
  }

  onSelect(id: string): void {
    this.router.navigate(['/dashboard/modules', id, 'elements']);
  }

  openCreateModal(): void {
    this.editId.set(null);
    this.form.reset({
      name: '',
      year: this.filterYear() || 1,
      semester: this.filterSemester() || 1,
      department: this.filterDepartment() || ''
    });
    this.showForm.set(true);
  }

  closeModal(): void {
    this.showForm.set(false);
    this.editId.set(null);
  }

  onEdit(event: Event, m: Module): void {
    event.stopPropagation();
    this.editId.set(m.id);
    this.form.patchValue({
      name: m.name,
      year: m.year || 1,
      semester: m.semester,
      department: (m.department as any) || ''
    });
    this.showForm.set(true);
  }

  openDeleteModal(event: Event, m: Module): void {
    event.stopPropagation();
    this.deleteTarget.set(m);
  }

  closeDeleteModal(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.svc.deleteModule(target.id).subscribe(() => {
      this.closeDeleteModal();
      this.load();
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const data = {
      name: raw.name!,
      year: Number(raw.year),
      semester: Number(raw.semester),
      department: raw.department!
    };
    const id = this.editId();

    if (id) {
      this.svc.updateModule(id, data).subscribe(() => {
        this.closeModal();
        this.load();
      });
    } else {
      this.svc.createModule(data).subscribe(() => {
        this.closeModal();
        this.load();
      });
    }
  }
}
