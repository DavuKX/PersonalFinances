import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { CategoryFormDialogComponent } from './category-form-dialog.component';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';

const mockCategory: CategoryResponse = {
  id: 'cat-uuid-1',
  name: 'Groceries',
  transactionType: TransactionType.EXPENSE,
  parentId: null,
  isDefault: false,
  createdAt: '2025-01-01T00:00:00Z',
};

describe('CategoryFormDialogComponent', () => {
  let fixture: ComponentFixture<CategoryFormDialogComponent>;
  let component: CategoryFormDialogComponent;

  const categoryApiMock = { create: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: CategoryApiService, useValue: categoryApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    fixture = TestBed.createComponent(CategoryFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('form is invalid when name is empty', () => {
    expect(component['form'].invalid).toBe(true);
  });

  it('create calls categoryApi.create and emits saved', () => {
    categoryApiMock.create.mockReturnValue(of(mockCategory));
    const savedValues: CategoryResponse[] = [];
    component.saved.subscribe((c) => savedValues.push(c));

    component['form'].setValue({
      name: 'Groceries',
      transactionType: TransactionType.EXPENSE,
      parentId: '',
    });
    component['submit']();

    expect(categoryApiMock.create).toHaveBeenCalledWith({
      name: 'Groceries',
      transactionType: TransactionType.EXPENSE,
      parentId: null,
    });
    expect(savedValues).toHaveLength(1);
    expect(toastMock.success).toHaveBeenCalledWith('Category created');
  });

  it('passes parentId as string when a parent is selected', () => {
    categoryApiMock.create.mockReturnValue(of(mockCategory));
    component['form'].setValue({
      name: 'Groceries',
      transactionType: TransactionType.EXPENSE,
      parentId: 'parent-uuid-1',
    });
    component['submit']();
    expect(categoryApiMock.create).toHaveBeenCalledWith(
      expect.objectContaining({ parentId: 'parent-uuid-1' }),
    );
  });

  it('fixedType input pre-selects the transaction type', () => {
    fixture.componentRef.setInput('fixedType', TransactionType.INCOME);
    fixture.detectChanges();
    expect(component['form'].controls.transactionType.value).toBe(TransactionType.INCOME);
  });

  it('shows error toast when create fails', () => {
    categoryApiMock.create.mockReturnValue(throwError(() => new Error()));
    component['form'].setValue({
      name: 'Groceries',
      transactionType: TransactionType.EXPENSE,
      parentId: '',
    });
    component['submit']();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to create category');
  });
});

