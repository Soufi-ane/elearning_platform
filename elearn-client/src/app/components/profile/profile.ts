import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../login/login.service';
import { User } from '../../models/models';

@Component({
  selector: 'user-profile',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './profile.html'
})
export class Profile {
  private loginService = inject(LoginService);

  currentUser = this.loginService.userProfile;
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  users = signal<User[]>([]);
  selectedUser = signal<User | null>(null);
  roleFilter = signal<string>('');
  searchQuery = signal<string>('');

  activeUser = computed(() => this.selectedUser() ?? this.currentUser());
  isViewingSelf = computed(() => {
    const cur = this.currentUser();
    const act = this.activeUser();
    return !!cur && !!act && cur.id === act.id;
  });

  showCreateModal = signal<boolean>(false);
  newFirstName = signal<string>('');
  newLastName = signal<string>('');
  newUsername = signal<string>('');
  newEmail = signal<string>('');
  newPasswordInput = signal<string>('');
  newDob = signal<string>('');
  newRole = signal<string>('STUDENT');
  newDepartment = signal<string>('');
  newStudyMode = signal<string>('');
  newYear = signal<number>(1);
  newSemester = signal<number>(1);
  newGroupName = signal<string>('A');

  openCreateModal(): void {
    this.newFirstName.set('');
    this.newLastName.set('');
    this.newUsername.set('');
    this.newEmail.set('');
    this.newPasswordInput.set('');
    this.newDob.set('');
    this.newRole.set('STUDENT');
    this.newDepartment.set(this.departments()[0] ?? '');
    this.newStudyMode.set(this.studyModes()[0] ?? '');
    this.newYear.set(1);
    this.newSemester.set(1);
    this.newGroupName.set('A');
    this.clearMessages();
    this.showCreateModal.set(true);
  }

  createAccount(): void {
    this.clearMessages();
    const role = this.newRole();
    const isStudent = role === 'STUDENT';
    const isAdminRole = role === 'ADMIN';

    const payload: Record<string, unknown> = {
      firstName: this.newFirstName(),
      lastName: this.newLastName(),
      username: this.newUsername(),
      email: this.newEmail(),
      password: this.newPasswordInput(),
      dateOfBirth: this.newDob() || null,
      role,
      department: isAdminRole ? null : (this.newDepartment() || null),
      studyMode: isStudent ? (this.newStudyMode() || null) : null,
      year: isStudent ? Number(this.newYear()) : 0,
      semester: isStudent ? Number(this.newSemester()) : 0,
      groupName: isStudent ? (this.newGroupName() || 'A') : null
    };

    this.loginService.createUser(payload).subscribe({
      next: (created) => {
        this.showCreateModal.set(false);
        this.selectedUser.set(created);
        this.isEditing.set(false);
        this.feedback.set('Account created.');
        this.loadUsers();
      },
      error: (err) => {
        this.errorMsg.set(this.extractError(err, 'Failed to create account.'));
      }
    });
  }

  deleteActiveAccount(): void {
    const u = this.activeUser() as any;
    if (!u || !this.isAdmin() || u.rootAdmin || this.isViewingSelf()) return;
    if (!confirm(`Delete account for ${u.firstName} ${u.lastName}?`)) return;

    this.clearMessages();
    this.loginService.deleteUser(u.id).subscribe({
      next: () => {
        this.selectedUser.set(this.currentUser());
        this.isEditing.set(false);
        this.feedback.set('Account deleted.');
        this.loadUsers();
      },
      error: (err) => {
        this.errorMsg.set(this.extractError(err, 'Failed to delete account.'));
      }
    });
  }

  pictureTick = signal<number>(Date.now());
  pictureUrl = computed(() => {
    const u = this.activeUser() as any;
    if (!u?.id || !u.hasProfilePicture) return null;
    return `/api/users/${u.id}/picture?t=${this.pictureTick()}`;
  });

  isEditing = signal<boolean>(false);
  showPasswordModal = signal<boolean>(false);
  feedback = signal<string>('');
  errorMsg = signal<string>('');

  roles = signal<string[]>(['STUDENT', 'TEACHER', 'ADMIN']);
  departments = signal<string[]>([]);
  studyModes = signal<string[]>([]);
  groups = ['A', 'B', 'C'];

  formFirstName = signal<string>('');
  formLastName = signal<string>('');
  formUsername = signal<string>('');
  formEmail = signal<string>('');
  formDob = signal<string>('');
  formRole = signal<string>('STUDENT');
  formDepartment = signal<string>('');
  formStudyMode = signal<string>('');
  formYear = signal<number>(1);
  formSemester = signal<number>(1);
  formGroupName = signal<string>('A');

  currentPassword = signal<string>('');
  newPassword = signal<string>('');
  confirmPassword = signal<string>('');

  private initialized = false;

  constructor() {
    effect(() => {
      const user = this.currentUser();
      if (!user || this.initialized) return;
      this.initialized = true;
      this.loadOptions();
      if (user.role === 'ADMIN') {
        this.loadUsers();
      }
    }, { allowSignalWrites: true });
  }

  loadOptions(): void {
    this.loginService.getProfileOptions().subscribe({
      next: (opts) => {
        this.roles.set(opts.roles);
        this.departments.set(opts.departments);
        this.studyModes.set(opts.studyModes);
      }
    });
  }

  loadUsers(): void {
    this.loginService
      .listUsers(this.roleFilter() || undefined, this.searchQuery() || undefined)
      .subscribe({
        next: (list) => this.users.set(list)
      });
  }

  selectUser(user: User): void {
    this.selectedUser.set(user);
    this.isEditing.set(false);
    this.clearMessages();
  }

  selectSelf(): void {
    this.selectedUser.set(this.currentUser());
    this.isEditing.set(false);
    this.clearMessages();
  }

  startEdit(): void {
    const u = this.activeUser() as any;
    if (!u || !this.isAdmin()) return;
    this.formFirstName.set(u.firstName ?? '');
    this.formLastName.set(u.lastName ?? '');
    this.formUsername.set(u.username ?? '');
    this.formEmail.set(u.email ?? '');
    this.formDob.set(u.dateOfBirth ?? '');
    this.formRole.set(u.role ?? 'STUDENT');
    this.formDepartment.set(u.department ?? '');
    this.formStudyMode.set(u.studyMode ?? '');
    this.formYear.set(u.year || 1);
    this.formSemester.set(u.semester || 1);
    this.formGroupName.set(u.groupName ?? 'A');
    this.isEditing.set(true);
    this.clearMessages();
  }

  saveProfile(): void {
    const u = this.activeUser();
    if (!u || !this.isAdmin()) return;
    this.clearMessages();

    const isStudent = this.formRole() === 'STUDENT';
    const isAdminRole = this.formRole() === 'ADMIN';

    const payload: Record<string, unknown> = {
      firstName: this.formFirstName(),
      lastName: this.formLastName(),
      username: this.formUsername(),
      email: this.formEmail(),
      dateOfBirth: this.formDob() || null,
      role: this.formRole(),
      department: isAdminRole ? null : (this.formDepartment() || null),
      studyMode: isStudent ? (this.formStudyMode() || null) : null,
      year: isStudent ? Number(this.formYear()) : 0,
      semester: isStudent ? Number(this.formSemester()) : 0,
      groupName: isStudent ? (this.formGroupName() || 'A') : null
    };

    this.loginService.updateUserProfile(u.id, payload).subscribe({
      next: (updated) => {
        this.selectedUser.set(updated);
        this.isEditing.set(false);
        this.feedback.set('Profile updated.');
        this.loadUsers();
      },
      error: (err) => {
        this.errorMsg.set(this.extractError(err, 'Failed to update profile.'));
      }
    });
  }

  onPictureSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const u = this.activeUser();
    if (!file || !u) return;

    this.clearMessages();
    this.loginService.uploadProfilePicture(u.id, file).subscribe({
      next: (updated) => {
        this.selectedUser.set(updated);
        this.pictureTick.set(Date.now());
        this.feedback.set('Profile picture updated.');
        if (this.isAdmin()) this.loadUsers();
      },
      error: (err) => {
        this.errorMsg.set(this.extractError(err, 'Failed to upload picture.'));
      }
    });
    input.value = '';
  }

  removePicture(): void {
    const u = this.activeUser();
    if (!u) return;
    this.clearMessages();
    this.loginService.deleteProfilePicture(u.id).subscribe({
      next: (updated) => {
        this.selectedUser.set(updated);
        this.pictureTick.set(Date.now());
        this.feedback.set('Profile picture removed.');
        if (this.isAdmin()) this.loadUsers();
      },
      error: (err) => {
        this.errorMsg.set(this.extractError(err, 'Failed to remove picture.'));
      }
    });
  }

  openPasswordModal(): void {
    this.currentPassword.set('');
    this.newPassword.set('');
    this.confirmPassword.set('');
    this.clearMessages();
    this.showPasswordModal.set(true);
  }

  submitPasswordChange(): void {
    this.clearMessages();
    if (this.newPassword() !== this.confirmPassword()) {
      this.errorMsg.set('Passwords do not match.');
      return;
    }
    this.loginService
      .changePassword(this.currentPassword(), this.newPassword())
      .subscribe({
        next: () => {
          this.showPasswordModal.set(false);
          this.feedback.set('Password updated.');
        },
        error: (err) => {
          this.errorMsg.set(this.extractError(err, 'Failed to update password.'));
        }
      });
  }

  formatEnum(value?: string | null): string {
    return value ? value.replace(/_/g, ' ') : '';
  }

  initials(u: User): string {
    return `${u.lastName.charAt(0)}${u.firstName.charAt(0)}`.toUpperCase();
  }

  private extractError(err: any, fallback: string): string {
    if (typeof err?.error === 'string' && err.error.trim()) return err.error;
    return err?.error?.message || err?.error?.error || fallback;
  }

  private clearMessages(): void {
    this.feedback.set('');
    this.errorMsg.set('');
  }
}
