import { Routes } from '@angular/router';

export const transactionsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./transaction-list/transaction-list.component').then((m) => m.TransactionListComponent),
    data: { title: 'Transactions' },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./transaction-form/transaction-form.component').then((m) => m.TransactionFormComponent),
    data: { title: 'New Transaction' },
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./transaction-form/transaction-form.component').then((m) => m.TransactionFormComponent),
    data: { title: 'Edit Transaction' },
  },
];

