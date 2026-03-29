import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./admin-user-list/admin-user-list.component').then((m) => m.AdminUserListComponent),
    data: { title: 'User Management' },
  },
  {
    path: 'users/:id',
    loadComponent: () =>
      import('./admin-user-detail/admin-user-detail.component').then((m) => m.AdminUserDetailComponent),
    data: { title: 'User Detail' },
  },
];

