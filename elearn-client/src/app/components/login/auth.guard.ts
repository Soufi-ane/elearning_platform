import { inject } from "@angular/core";
import { CanActivateFn, Router, ActivatedRouteSnapshot } from "@angular/router";
import { LoginService } from "./login.service";

export const roleGuard = (allowedRoles: string[] = []): CanActivateFn => {
  return (route: ActivatedRouteSnapshot) => {
    const loginService = inject(LoginService);
    const router = inject(Router);
    const user = loginService.userProfile();

    if (!user) {
      router.navigate(['/login']);
      return false;
    }
    if (allowedRoles.length === 0) {
      return true;
    }
    if (allowedRoles.includes(user.role.toString())) {
      return true;
    }
    router.navigate(['/unauthorized']);
    return false;
  };
};
