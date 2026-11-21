import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ModuleService } from './modules.service';
import {
  ElementWorkspace,
  FileType,
  MaterialFolder,
  MaterialItem
} from '../../../models/models';

@Component({
  selector: 'app-element-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './element-workspace.html'
})
export class ElementWorkspaceView implements OnInit {
  private route = inject(ActivatedRoute);
  private svc = inject(ModuleService);

  moduleId = signal<string>('');
  elementId = signal<string>('');
  workspace = signal<ElementWorkspace | null>(null);
  currentFolderId = signal<string | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  showFolderModal = signal(false);
  folderName = signal('');

  showUploadModal = signal(false);
  uploadTitle = signal('');
  uploadDescription = signal('');
  uploadIsAssignment = signal(false);
  uploadDeadline = signal('');
  selectedFile = signal<File | null>(null);
  uploading = signal(false);

  previewImage = signal<MaterialItem | null>(null);

  currentFolders = computed(() => {
    const ws = this.workspace();
    const cur = this.currentFolderId();
    if (!ws) return [];
    return ws.folders.filter(f => (f.parentId || null) === cur);
  });

  currentMaterials = computed(() => {
    const ws = this.workspace();
    const cur = this.currentFolderId();
    if (!ws) return [];
    return ws.materials.filter(m => (m.folderId || null) === cur);
  });

  breadcrumbs = computed(() => {
    const ws = this.workspace();
    if (!ws) return [];
    const byId = new Map(ws.folders.map(f => [f.id, f]));
    const trail: MaterialFolder[] = [];
    let cur = this.currentFolderId();
    while (cur && byId.has(cur)) {
      const f = byId.get(cur)!;
      trail.unshift(f);
      cur = f.parentId;
    }
    return trail;
  });

  ngOnInit(): void {
    this.moduleId.set(this.route.snapshot.paramMap.get('id') || '');
    this.elementId.set(this.route.snapshot.paramMap.get('elementId') || '');
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getElementWorkspace(this.elementId()).subscribe({
      next: (ws) => {
        this.workspace.set(ws);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load course materials.');
        this.loading.set(false);
      }
    });
  }

  openFolder(folderId: string | null): void {
    this.currentFolderId.set(folderId);
  }

  openFolderModal(): void {
    this.folderName.set('');
    this.showFolderModal.set(true);
  }

  createFolder(): void {
    const name = this.folderName().trim();
    if (!name) return;
    this.svc.createFolder(
      this.elementId(),
      name,
      this.currentFolderId()
    ).subscribe({
      next: () => {
        this.showFolderModal.set(false);
        this.load();
      }
    });
  }

  deleteFolder(event: Event, folder: MaterialFolder): void {
    event.stopPropagation();
    this.svc.deleteFolder(this.elementId(), folder.id).subscribe({
      next: () => {
        if (this.currentFolderId() === folder.id) {
          this.currentFolderId.set(folder.parentId);
        }
        this.load();
      }
    });
  }

  openUploadModal(asAssignment: boolean): void {
    this.uploadTitle.set('');
    this.uploadDescription.set('');
    this.uploadIsAssignment.set(asAssignment);
    this.uploadDeadline.set('');
    this.selectedFile.set(null);
    this.showUploadModal.set(true);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;
    this.selectedFile.set(file);
    if (file && !this.uploadTitle().trim()) {
      this.uploadTitle.set(file.name);
    }
  }

  submitUpload(): void {
    const file = this.selectedFile();
    const isAssign = this.uploadIsAssignment();
    if (!file && !isAssign) return;
    if (isAssign && !this.uploadTitle().trim()) return;

    const fd = new FormData();
    if (file) {
      fd.append('file', file);
    }
    if (this.uploadTitle().trim()) {
      fd.append('title', this.uploadTitle().trim());
    }
    if (this.uploadDescription().trim()) {
      fd.append('description', this.uploadDescription().trim());
    }
    if (this.currentFolderId()) {
      fd.append('folderId', this.currentFolderId()!);
    }
    fd.append('assignment', String(isAssign));
    if (isAssign && this.uploadDeadline()) {
      fd.append('deadline', this.uploadDeadline());
    }

    this.uploading.set(true);
    this.svc.uploadMaterial(this.elementId(), fd).subscribe({
      next: () => {
        this.uploading.set(false);
        this.showUploadModal.set(false);
        this.load();
      },
      error: () => {
        this.uploading.set(false);
      }
    });
  }

  deleteMaterial(m: MaterialItem): void {
    this.svc.deleteMaterial(this.elementId(), m.id).subscribe({
      next: () => this.load()
    });
  }

  downloadUrl(m: MaterialItem, inline = false): string {
    return this.svc.getDownloadUrl(m.id, inline);
  }

  isImage(m: MaterialItem): boolean {
    return m.fileType === FileType.IMAGE;
  }

  isOverdue(m: MaterialItem): boolean {
    if (!m.deadline) return false;
    return new Date(m.deadline).getTime() < Date.now();
  }

  formatSize(bytes: number): string {
    if (!bytes) return '0 B';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
}
