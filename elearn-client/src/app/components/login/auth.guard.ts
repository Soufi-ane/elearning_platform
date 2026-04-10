import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { LoginService } from "./login.service";

export const authGuard : CanActivateFn = () => {
  const loginService = inject(LoginService);
  const router = inject(Router);

  if(!loginService.userProfile()) {
    router.navigate(['/login']);
    return false;
  }
  return true;
}
