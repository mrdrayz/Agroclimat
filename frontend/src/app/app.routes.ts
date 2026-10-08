import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadComponent: () => import('./pages/auth-page.component').then((m) => m.AuthPageComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/main-layout.component').then((m) => m.MainLayoutComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard-page.component').then((m) => m.DashboardPageComponent)
      },
      {
        path: 'alerts',
        redirectTo: 'warnings',
        pathMatch: 'full'
      },
      {
        path: 'warnings',
        canActivate: [roleGuard(['ADMIN', 'AGRONOMIST', 'OPERATOR'])],
        loadComponent: () =>
          import('./pages/alerts-page.component').then((m) => m.AlertsPageComponent)
      },
      {
        path: 'zone-workbench',
        canActivate: [roleGuard(['ADMIN', 'AGRONOMIST'])],
        loadComponent: () =>
          import('./pages/zone-workbench-page.component').then((m) => m.ZoneWorkbenchPageComponent)
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./pages/reports-page.component').then((m) => m.ReportsPageComponent)
      },
      {
        path: 'admin/users',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin-users-page.component').then((m) => m.AdminUsersPageComponent)
      },
      {
        path: 'admin/greenhouse-assignments',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () =>
          import('./pages/admin-greenhouse-assignments-page.component').then(
            (m) => m.AdminGreenhouseAssignmentsPageComponent
          )
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
