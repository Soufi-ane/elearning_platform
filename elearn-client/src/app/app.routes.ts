import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Profile } from './components/profile/profile';
import { authGuard } from './components/login/auth.guard';
import { MainDashboard } from './components/dashboard/main/main';
import { Calendar } from './components/dashboard/calendar/calendar';
import { Attendance } from './components/dashboard/attendance/attendance';
import { AbsenceView } from './components/dashboard/absence/absence';

export const routes: Routes = [
  { path : '', component : Home },
  { path : 'login', component : Login },
  {
    path : 'dashboard',
    component : Dashboard,
    canActivate : [authGuard],
    children : [
      { path : '', component : MainDashboard },
      { path : 'profile', component : Profile },
      { path : 'calendar', component : Calendar },
      { path : 'attendance', component : Attendance },
      { path : 'attendance/:id', component : AbsenceView },
    ]
  },
  { path : '**', redirectTo : "/" },
];
