import { Routes } from '@angular/router';

export const analyticsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./analytics-overview/analytics-overview.component').then((m) => m.AnalyticsOverviewComponent),
    data: { title: 'Analytics' },
  },
];

