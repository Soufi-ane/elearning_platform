import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ModuleService } from './modules.service';
import { LoginService } from '../../login/login.service';
import { Element, Role, User } from '../../../models/models';

@Component({
  selector: 'app-element-detail',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './element-detail.html'
})
export class ElementDetail implements OnInit {
  private svc = inject(ModuleService);
  private route = inject(ActivatedRoute);
  private loginService = inject(LoginService);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  user = this.loginService.userProfile()!;
  moduleId = signal<string>('');
  elements = signal<Element[]>([]);
  teachers = signal<User[]>([]);
  showForm = signal(false);
  editId = signal<string | null>(null);
  deleteTarget = signal<Element | null>(null);

  form = this.fb.group({
    name: ['', Validators.required],
    moduleId: ['', Validators.required],
    teacherId: ['', Validators.required]
  });

  get isAdmin(): boolean {
    return this.user?.role === Role.ADMIN ||
      (this.user?.role as unknown as string) === 'ADMIN';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.moduleId.set(id);
      this.form.patchValue({ moduleId: id });
      this.load();
    }
    if (this.isAdmin) {
      this.http.get<User[]>('/api/users?role=TEACHER', {
        withCredentials: true
      }).subscribe(res => this.teachers.set(res));
    }
  }

  load(): void {
    this.svc.getElements(this.moduleId()).subscribe({
      next: (data) => this.elements.set(data)
    });
  }

  openCreateModal(): void {
    this.editId.set(null);
    this.form.reset({
      name: '',
      moduleId: this.moduleId(),
      teacherId: this.teachers()[0]?.id || ''
    });
    this.showForm.set(true);
  }

  closeModal(): void {
    this.showForm.set(false);
    this.editId.set(null);
  }

  onEdit(e: Element): void {
    this.editId.set(e.id);
    this.form.patchValue({
      name: e.name,
      moduleId: e.module.id,
      teacherId: e.teacher.id
    });
    this.showForm.set(true);
  }

  openDeleteModal(e: Element): void {
    this.deleteTarget.set(e);
  }

  closeDeleteModal(): void {
    this.deleteTarget.set(null);
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;
    this.svc.deleteElement(target.id).subscribe(() => {
      this.closeDeleteModal();
      this.load();
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const data = this.form.getRawValue() as {
      name: string;
      moduleId: string;
      teacherId: string;
    };
    const id = this.editId();

    if (id) {
      this.svc.updateElement(id, data).subscribe(() => {
        this.closeModal();
        this.load();
      });
    } else {
      this.svc.createElement(data).subscribe(() => {
        this.closeModal();
        this.load();
      });
    }
  }
}
