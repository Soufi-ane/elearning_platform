import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LoginService } from '../../login/login.service';
import { Role } from '../../../models/models';

@Component({
  selector: 'app-dashboard-main',
  imports: [RouterLink],
  templateUrl: './dashboard-main.html',
})
export class MainDashboard {
  private loginService = inject(LoginService);
  user = this.loginService.userProfile;
  Role = Role;
}
