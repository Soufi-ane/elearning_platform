import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ModuleService } from './modules.service';
import { Module } from '../../../models/models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-module-list',
  templateUrl: './module-list.html'
})
export class ModuleList implements OnInit {
  private svc = inject(ModuleService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);
  modules: Module[] = [];

  ngOnInit(): void {
    this.svc.getModules().subscribe({
      next: (data) => {
        this.modules = data;
        this.cdr.markForCheck();
        console.log('Modules loaded:', this.modules);
      }
    });
  }
  onSelect(id: string): void {
    this.router.navigate(['/dashboard/modules', id, 'elements']);
  }
}
