import { Component, inject } from '@angular/core';
import { LoginService } from '../login/login.service';

@Component({
  selector: 'user-profile',
  imports: [],
  templateUrl: './profile.html',
})
export class Profile {
  private loginService = inject(LoginService);
  user = this.loginService.userProfile()!;
}
