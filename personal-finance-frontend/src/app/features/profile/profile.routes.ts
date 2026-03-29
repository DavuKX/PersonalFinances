import { Routes } from '@angular/router';

export const profileRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./profile/profile.component').then((m) => m.ProfileComponent),
    data: { title: 'Profile' },
  },
  {
    path: 'password',
    loadComponent: () =>
      import('./change-password/change-password.component').then((m) => m.ChangePasswordComponent),
    data: { title: 'Change Password' },
  },
];

