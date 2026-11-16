import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../login/login.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html'
})
export class Header {
  private loginService = inject(LoginService);
  public router = inject(Router);
  profile = this.loginService.userProfile;
  showLogoutModal = signal(false);

  openLogoutModal() {
    this.showLogoutModal.set(true);
  }

  closeLogoutModal() {
    this.showLogoutModal.set(false);
  }

  confirmLogout() {
    this.closeLogoutModal();
    this.loginService.logout().subscribe({
      next: () => {},
      error: () => {}
    });
    this.profile.set(null);
    this.router.navigate(['/']);
  }
}
