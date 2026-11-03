import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ModuleService } from './modules.service';
import { LoginService } from '../../login/login.service';
import { Department, Module, Role } from '../../../models/models';

@Component({
  selector: 'app-module-list',
  standalone: true,
  imports: [ReactiveFormsModule],
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

  form = this.fb.group({
    name: ['', Validators.required],
    semester: [1, [Validators.required, Validators.min(1), Validators.max(6)]],
    department: [Department.GENIE_INFORMATIQUE, Validators.required]
  });

  get isAdmin(): boolean {
    return this.user?.role === Role.ADMIN ||
      (this.user?.role as unknown as string) === 'ADMIN';
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.svc.getModules().subscribe({
      next: (data) => this.modules.set(data)
    });
  }

  onSelect(id: string): void {
    this.router.navigate(['/dashboard/modules', id, 'elements']);
  }

  openCreateModal(): void {
    this.editId.set(null);
    this.form.reset({
      name: '',
      semester: 1,
      department: Department.GENIE_INFORMATIQUE
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
      semester: m.semester,
      department: (m.department as any) || Department.GENIE_INFORMATIQUE
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
    const data = this.form.getRawValue() as {
      name: string;
      semester: number;
      department: string;
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
