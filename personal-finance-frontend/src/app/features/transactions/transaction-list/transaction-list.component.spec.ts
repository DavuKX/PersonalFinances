import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { TransactionListComponent } from './transaction-list.component';
import { TransactionApiService } from '../../../core/services/transaction-api.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import {
  TransactionPageResponse,
  TransactionResponse,
  TransactionType,
} from '../../../core/models/transaction.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { CategoryResponse } from '../../../core/models/category.models';

const makeTx = (overrides: Partial<TransactionResponse> = {}): TransactionResponse => ({
  id: 'tx-uuid-1',
  walletId: 'wallet-uuid-1',
  type: TransactionType.EXPENSE,
  amount: 50,
  currency: 'USD',
  categoryId: null,
  subCategoryId: null,
  categoryName: null,
  subCategoryName: null,
  description: 'Groceries',
  transactionDate: '2025-03-01T00:00:00Z',
  createdAt: '2025-03-01T00:00:00Z',
  updatedAt: '2025-03-01T00:00:00Z',
  ...overrides,
});

const emptyPage: TransactionPageResponse = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};

const makeWallet = (): WalletResponse => ({
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
});

describe('TransactionListComponent', () => {
  let fixture: ComponentFixture<TransactionListComponent>;
  let component: TransactionListComponent;

  const txApiMock = { getAll: vi.fn(), getByWallet: vi.fn(), delete: vi.fn() };
  const walletApiMock = { getAll: vi.fn() };
  const categoryApiMock = { getAll: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  function createComponent(): void {
    fixture = TestBed.createComponent(TransactionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    vi.clearAllMocks();
    txApiMock.getAll.mockReturnValue(of(emptyPage));
    walletApiMock.getAll.mockReturnValue(of([]));
    categoryApiMock.getAll.mockReturnValue(of([]));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: TransactionApiService, useValue: txApiMock },
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: CategoryApiService, useValue: categoryApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    createComponent();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('loads transactions on init', () => {
    expect(txApiMock.getAll).toHaveBeenCalledWith(0, 20, {
      type: undefined,
      categoryId: undefined,
      from: undefined,
      to: undefined,
    });
  });

  it('uses getByWallet when wallet filter is set', () => {
    txApiMock.getByWallet.mockReturnValue(of(emptyPage));
    component['setWalletFilter']('wallet-uuid-1');
    expect(txApiMock.getByWallet).toHaveBeenCalledWith('wallet-uuid-1', 0, 20);
  });

  it('shows error toast when loadTransactions fails', () => {
    txApiMock.getAll.mockReturnValue(throwError(() => new Error()));
    createComponent();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to load transactions');
  });

  it('hasActiveFilters is false when no filters are set', () => {
    expect(component['hasActiveFilters']()).toBe(false);
  });

  it('hasActiveFilters is true when type filter is set', () => {
    component['filterType'].set(TransactionType.INCOME);
    expect(component['hasActiveFilters']()).toBe(true);
  });

  it('clearFilters resets all filter signals', () => {
    txApiMock.getAll.mockReturnValue(of(emptyPage));
    component['filterType'].set(TransactionType.INCOME);
    component['filterWalletId'].set('wallet-uuid-1');
    component['clearFilters']();
    expect(component['filterType']()).toBe('');
    expect(component['filterWalletId']()).toBe('');
  });

  it('categoryLabel returns subcategory path when present', () => {
    const tx = makeTx({ categoryName: 'Food', subCategoryName: 'Groceries' });
    expect(component['categoryLabel'](tx)).toBe('Food › Groceries');
  });

  it('categoryLabel returns dash when no category', () => {
    const tx = makeTx({ categoryName: null, subCategoryName: null });
    expect(component['categoryLabel'](tx)).toBe('—');
  });

  it('walletName resolves wallet id to name', () => {
    component['wallets'].set([makeWallet()]);
    expect(component['walletName']('wallet-uuid-1')).toBe('My Wallet');
  });

  it('executeDelete removes transaction and shows toast', () => {
    const tx = makeTx();
    txApiMock.delete.mockReturnValue(of(undefined));
    component['transactions'].set([tx]);
    component['totalElements'].set(1);
    component['txToDelete'].set(tx);
    component['executeDelete']();
    expect(component['transactions']()).toHaveLength(0);
    expect(component['totalElements']()).toBe(0);
    expect(toastMock.success).toHaveBeenCalledWith('Transaction deleted');
  });

  it('executeDelete shows error toast on failure', () => {
    txApiMock.delete.mockReturnValue(throwError(() => new Error()));
    component['txToDelete'].set(makeTx());
    component['executeDelete']();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to delete transaction');
  });

  it('topLevelCategories filters parentId=null', () => {
    const parent: CategoryResponse = {
      id: 'p1', name: 'Food', transactionType: TransactionType.EXPENSE,
      parentId: null, isDefault: false, createdAt: '2025-01-01',
    };
    const child: CategoryResponse = {
      id: 'c1', name: 'Groceries', transactionType: TransactionType.EXPENSE,
      parentId: 'p1', isDefault: false, createdAt: '2025-01-01',
    };
    component['categories'].set([parent, child]);
    expect(component['topLevelCategories']()).toEqual([parent]);
  });
});

