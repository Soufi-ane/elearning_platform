import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { LoginService } from '../login/login.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterOutlet, RouterLink],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private loginService = inject(LoginService);
  user = this.loginService.userProfile()!;
}
