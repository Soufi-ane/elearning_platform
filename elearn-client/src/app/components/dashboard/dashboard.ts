import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LoginService } from '../login/login.service';
import { Role } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private loginService = inject(LoginService);
  user = this.loginService.userProfile()!;

  get isStudent(): boolean {
    return this.user?.role === Role.STUDENT || (this.user?.role as unknown as string) === 'STUDENT';
  }
}
