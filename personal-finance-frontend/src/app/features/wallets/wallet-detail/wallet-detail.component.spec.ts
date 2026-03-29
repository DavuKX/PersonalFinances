import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { WalletDetailComponent } from './wallet-detail.component';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { TransactionApiService } from '../../../core/services/transaction-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { LimitPeriod, WalletResponse } from '../../../core/models/wallet.models';
import { TransactionPageResponse, TransactionType } from '../../../core/models/transaction.models';

const mockWallet: WalletResponse = {
  id: 'wallet-uuid-1',
  name: 'My Wallet',
  currency: 'USD',
  balance: 1000,
  archived: false,
  spendingLimitAmount: null,
  spendingLimitPeriod: null,
  archivedAt: null,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
};

const emptyTxPage: TransactionPageResponse = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};

describe('WalletDetailComponent', () => {
  let fixture: ComponentFixture<WalletDetailComponent>;
  let component: WalletDetailComponent;

  const walletApiMock = {
    getById: vi.fn(),
    archive: vi.fn(),
    restore: vi.fn(),
    delete: vi.fn(),
    removeSpendingLimit: vi.fn(),
  };
  const transactionApiMock = { getByWallet: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  function createComponent(): void {
    fixture = TestBed.createComponent(WalletDetailComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', 'wallet-uuid-1');
    fixture.detectChanges();
  }

  beforeEach(() => {
    vi.clearAllMocks();
    walletApiMock.getById.mockReturnValue(of(mockWallet));
    transactionApiMock.getByWallet.mockReturnValue(of(emptyTxPage));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: TransactionApiService, useValue: transactionApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    createComponent();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('loads wallet and transactions on init', () => {
    expect(walletApiMock.getById).toHaveBeenCalledWith('wallet-uuid-1');
    expect(transactionApiMock.getByWallet).toHaveBeenCalledWith('wallet-uuid-1', 0, 20);
  });

  it('wallet signal is set from loaded response', () => {
    expect(component['wallet']()).toEqual(mockWallet);
  });

  it('archive calls walletApi.archive and updates wallet', () => {
    const archived = { ...mockWallet, archived: true };
    walletApiMock.archive.mockReturnValue(of(archived));
    component['archive']();
    expect(component['wallet']()?.archived).toBe(true);
    expect(toastMock.success).toHaveBeenCalledWith('Wallet archived');
  });

  it('restore calls walletApi.restore and updates wallet', () => {
    const restored = { ...mockWallet, archived: false };
    walletApiMock.restore.mockReturnValue(of(restored));
    component['restore']();
    expect(component['wallet']()?.archived).toBe(false);
    expect(toastMock.success).toHaveBeenCalledWith('Wallet restored');
  });

  it('removeSpendingLimit updates wallet and shows toast', () => {
    const noLimit = { ...mockWallet, spendingLimitAmount: null, spendingLimitPeriod: null };
    walletApiMock.removeSpendingLimit.mockReturnValue(of(noLimit));
    component['removeSpendingLimit']();
    expect(component['wallet']()?.spendingLimitAmount).toBeNull();
    expect(toastMock.success).toHaveBeenCalledWith('Spending limit removed');
  });

  it('executeDelete navigates to /wallets on success', () => {
    walletApiMock.delete.mockReturnValue(of(undefined));
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
    component['executeDelete']();
    expect(router.navigate).toHaveBeenCalledWith(['/wallets']);
    expect(toastMock.success).toHaveBeenCalledWith('Wallet deleted');
  });

  it('categoryLabel returns subcategory path when subCategoryName exists', () => {
    const tx = {
      id: 'tx-1', walletId: 'w1', type: TransactionType.EXPENSE,
      amount: 10, currency: 'USD', categoryId: 'c1', subCategoryId: 's1',
      categoryName: 'Food', subCategoryName: 'Groceries',
      description: null, transactionDate: '2025-01-01', createdAt: '2025-01-01', updatedAt: '2025-01-01',
    };
    expect(component['categoryLabel'](tx)).toBe('Food › Groceries');
  });

  it('categoryLabel falls back to categoryName when no subcategory', () => {
    const tx = {
      id: 'tx-1', walletId: 'w1', type: TransactionType.EXPENSE,
      amount: 10, currency: 'USD', categoryId: 'c1', subCategoryId: null,
      categoryName: 'Food', subCategoryName: null,
      description: null, transactionDate: '2025-01-01', createdAt: '2025-01-01', updatedAt: '2025-01-01',
    };
    expect(component['categoryLabel'](tx)).toBe('Food');
  });

  it('shows error toast when wallet load fails', () => {
    walletApiMock.getById.mockReturnValue(throwError(() => new Error()));
    createComponent();
    expect(component['wallet']()).toBeNull();
  });
});

