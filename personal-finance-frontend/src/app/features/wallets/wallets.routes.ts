import { Routes } from '@angular/router';

export const walletsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./wallet-list/wallet-list.component').then((m) => m.WalletListComponent),
    data: { title: 'Wallets' },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./wallet-detail/wallet-detail.component').then((m) => m.WalletDetailComponent),
    data: { title: 'Wallet Detail' },
  },
];

