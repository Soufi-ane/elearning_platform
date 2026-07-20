import { Routes } from '@angular/router';
import { Home } from './home/home';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Profile } from './components/profile/profile';
import { MainDashboard } from './components/dashboard/main/main';
import { Calendar } from './components/dashboard/calendar/calendar';
import { Grades } from './components/dashboard/grades/grades';
import { Requests } from './components/dashboard/requests/requests';
import { ModuleList } from './components/dashboard/modules/module-list';
import { ElementDetail } from './components/dashboard/modules/element-detail';
import { roleGuard } from './components/login/auth.guard';
import { Attendance } from './components/dashboard/attendance/attendance';
import { AbsenceView } from './components/dashboard/attendance/absence-view';
import { AdminAbsence } from './components/dashboard/attendance/admin-absence';

export const routes: Routes = [
  { path : '', component : Home },
  { path : 'login', component : Login },
  {
    path : 'dashboard',
    component : Dashboard,
    canActivate : [roleGuard()],
    children : [
      { path : '', component : MainDashboard },
      { path : 'profile', component : Profile },
      { path : 'calendar', component : Calendar },
      { path : 'attendance', component : Attendance },
      {
        path : 'attendance/manage',
        component : AdminAbsence,
        canActivate : [roleGuard(['ADMIN', 'TEACHER'])]
      },
      { path : 'attendance/:id', component : AbsenceView },
      { path : 'grades', component : Grades },
      { path : 'requests', component : Requests },
      { path : 'modules', component : ModuleList },
      { path : 'modules/:id/elements', component : ElementDetail }
    ]
  },
  { path : '**', redirectTo : "/" },
];
