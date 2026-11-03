import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LoginService } from '../login/login.service';
import { Role } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private loginService = inject(LoginService);
  public router = inject(Router);
  user = this.loginService.userProfile()!;

  get isStudent(): boolean {
    return this.user?.role === Role.STUDENT ||
      (this.user?.role as unknown as string) === 'STUDENT';
  }

  get isAdminOrTeacher(): boolean {
    const roleStr = String(this.user?.role);
    return roleStr === Role.ADMIN || roleStr === Role.TEACHER ||
      roleStr === 'ADMIN' || roleStr === 'TEACHER';
  }
}
