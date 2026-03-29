import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { TransactionFormComponent } from './transaction-form.component';
import { TransactionApiService } from '../../../core/services/transaction-api.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { TransactionResponse, TransactionType } from '../../../core/models/transaction.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { CategoryResponse } from '../../../core/models/category.models';

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

const mockTx: TransactionResponse = {
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
};

describe('TransactionFormComponent', () => {
  let fixture: ComponentFixture<TransactionFormComponent>;
  let component: TransactionFormComponent;

  const txApiMock = { create: vi.fn(), getById: vi.fn(), update: vi.fn() };
  const walletApiMock = { getAll: vi.fn() };
  const categoryApiMock = { getAll: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    walletApiMock.getAll.mockReturnValue(of([mockWallet]));
    categoryApiMock.getAll.mockReturnValue(of([]));
    txApiMock.getById.mockReturnValue(of(mockTx));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: TransactionApiService, useValue: txApiMock },
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: CategoryApiService, useValue: categoryApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    fixture = TestBed.createComponent(TransactionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('defaults to EXPENSE type and today date', () => {
    expect(component['form'].value.type).toBe(TransactionType.EXPENSE);
    expect(component['form'].value.transactionDate).toBeTruthy();
  });

  it('isEditMode is false when no id input', () => {
    expect(component['isEditMode']()).toBe(false);
  });

  it('isEditMode is true when id input is set', () => {
    fixture.componentRef.setInput('id', 'tx-uuid-1');
    fixture.detectChanges();
    expect(component['isEditMode']()).toBe(true);
  });

  it('loadTransaction is called in edit mode', () => {
    txApiMock.getById.mockReturnValue(of(mockTx));
    fixture.componentRef.setInput('id', 'tx-uuid-1');
    fixture.detectChanges();
    expect(txApiMock.getById).toHaveBeenCalledWith('tx-uuid-1');
  });

  it('form is pre-populated after loading a transaction', () => {
    txApiMock.getById.mockReturnValue(of(mockTx));
    fixture.componentRef.setInput('id', 'tx-uuid-1');
    fixture.detectChanges();
    expect(component['form'].controls.amount.value).toBe(50);
    expect(component['form'].controls.description.value).toBe('Groceries');
    expect(component['form'].controls.transactionDate.value).toBe('2025-03-01');
  });

  it('changeType resets category and subcategory', () => {
    component['form'].patchValue({ categoryId: 'cat-1', subCategoryId: 'sub-1' });
    component['changeType'](TransactionType.INCOME);
    expect(component['form'].value.type).toBe(TransactionType.INCOME);
    expect(component['form'].value.categoryId).toBe('');
    expect(component['form'].value.subCategoryId).toBe('');
  });

  it('filteredCategories shows only matching type top-level categories', () => {
    const incomeCategory: CategoryResponse = {
      id: 'c1', name: 'Salary', transactionType: TransactionType.INCOME,
      parentId: null, isDefault: false, createdAt: '2025-01-01',
    };
    const expenseCategory: CategoryResponse = {
      id: 'c2', name: 'Food', transactionType: TransactionType.EXPENSE,
      parentId: null, isDefault: false, createdAt: '2025-01-01',
    };
    component['categories'].set([incomeCategory, expenseCategory]);
    component['form'].controls.type.setValue(TransactionType.EXPENSE);
    expect(component['filteredCategories']()).toEqual([expenseCategory]);
  });

  it('filteredSubcategories returns children of selected category', () => {
    const child: CategoryResponse = {
      id: 'c2', name: 'Groceries', transactionType: TransactionType.EXPENSE,
      parentId: 'c1', isDefault: false, createdAt: '2025-01-01',
    };
    component['categories'].set([child]);
    component['form'].controls.categoryId.setValue('c1');
    expect(component['filteredSubcategories']()).toEqual([child]);
  });

  it('create calls txApi.create and navigates on success', () => {
    txApiMock.create.mockReturnValue(of(mockTx));
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');

    component['form'].setValue({
      walletId: 'wallet-uuid-1',
      type: TransactionType.EXPENSE,
      amount: 50,
      categoryId: '',
      subCategoryId: '',
      description: 'Groceries',
      transactionDate: '2025-03-01',
    });
    component['submit']();

    expect(txApiMock.create).toHaveBeenCalled();
    expect(toastMock.success).toHaveBeenCalledWith('Transaction created');
    expect(router.navigate).toHaveBeenCalledWith(['/transactions']);
  });

  it('update calls txApi.update in edit mode', () => {
    txApiMock.getById.mockReturnValue(of(mockTx));
    txApiMock.update.mockReturnValue(of(mockTx));
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');

    fixture.componentRef.setInput('id', 'tx-uuid-1');
    fixture.detectChanges();

    component['form'].controls.amount.setValue(75);
    component['submit']();

    expect(txApiMock.update).toHaveBeenCalledWith('tx-uuid-1', expect.objectContaining({ amount: 75 }));
    expect(toastMock.success).toHaveBeenCalledWith('Transaction updated');
  });

  it('shows error toast when create fails', () => {
    txApiMock.create.mockReturnValue(throwError(() => new Error()));
    component['form'].setValue({
      walletId: 'wallet-uuid-1',
      type: TransactionType.EXPENSE,
      amount: 50,
      categoryId: '',
      subCategoryId: '',
      description: '',
      transactionDate: '2025-03-01',
    });
    component['submit']();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to create transaction');
  });
});


