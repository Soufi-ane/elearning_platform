import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../login/login.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
})
export class Header {
  private loginService = inject(LoginService);

  profile = this.loginService.userProfile;

  constructor(private router : Router){}

  logout() {
    this.loginService.logout()
    .subscribe({
      next: (_) => {
      },
      error: (_) => {}
    })
    this.profile.set(null);
    this.router.navigate(["/"]);
  }
}
