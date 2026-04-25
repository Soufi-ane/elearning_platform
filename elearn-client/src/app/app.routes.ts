import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Profile } from './components/profile/profile';
import { authGuard } from './components/login/auth.guard';
import { Calendar } from './components/calendar/calendar';
import { Attendance } from './components/attendance/attendance';

export const routes: Routes = [
  { path : '', component : Home },
  { path : 'login', component : Login },
  { path : 'dashboard', component : Dashboard, canActivate : [authGuard] },
  { path : 'dashboard/profile', component : Profile, canActivate : [authGuard] },
  { path : 'dashboard/calendar', component : Calendar, canActivate : [authGuard] },
  { path : 'dashboard/attendance', component : Attendance, canActivate : [authGuard] },
  { path : '**', redirectTo : "/" },
];
