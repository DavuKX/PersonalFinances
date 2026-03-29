import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { CategoryListComponent } from './category-list.component';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';

const makeCategory = (overrides: Partial<CategoryResponse> = {}): CategoryResponse => ({
  id: 'cat-uuid-1',
  name: 'Food',
  transactionType: TransactionType.EXPENSE,
  parentId: null,
  isDefault: false,
  createdAt: '2025-01-01T00:00:00Z',
  ...overrides,
});

describe('CategoryListComponent', () => {
  let fixture: ComponentFixture<CategoryListComponent>;
  let component: CategoryListComponent;

  const categoryApiMock = { getAll: vi.fn(), delete: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  function createComponent(): void {
    fixture = TestBed.createComponent(CategoryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    vi.clearAllMocks();
    categoryApiMock.getAll.mockReturnValue(of([]));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: CategoryApiService, useValue: categoryApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    createComponent();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('loads categories on init', () => {
    expect(categoryApiMock.getAll).toHaveBeenCalledOnce();
  });

  it('shows error toast when getAll fails', () => {
    categoryApiMock.getAll.mockReturnValue(throwError(() => new Error()));
    createComponent();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to load categories');
  });

  it('treeFor returns only matching type as top-level nodes', () => {
    const income = makeCategory({ id: 'i1', transactionType: TransactionType.INCOME });
    const expense = makeCategory({ id: 'e1', transactionType: TransactionType.EXPENSE });
    categoryApiMock.getAll.mockReturnValue(of([income, expense]));
    createComponent();

    const incomeTree = component['treeFor'](TransactionType.INCOME)();
    expect(incomeTree).toHaveLength(1);
    expect(incomeTree[0].parent.id).toBe('i1');
  });

  it('treeFor nests children under their parent', () => {
    const parent = makeCategory({ id: 'p1', transactionType: TransactionType.EXPENSE });
    const child = makeCategory({ id: 'c1', transactionType: TransactionType.EXPENSE, parentId: 'p1' });
    categoryApiMock.getAll.mockReturnValue(of([parent, child]));
    createComponent();

    const tree = component['treeFor'](TransactionType.EXPENSE)();
    expect(tree[0].children).toHaveLength(1);
    expect(tree[0].children[0].id).toBe('c1');
  });

  it('onCategorySaved appends the new category', () => {
    const cat = makeCategory();
    component['onCategorySaved'](cat);
    expect(component['categories']()).toContain(cat);
  });

  it('executeDelete removes the category and its children', () => {
    const parent = makeCategory({ id: 'p1' });
    const child = makeCategory({ id: 'c1', parentId: 'p1' });
    categoryApiMock.delete.mockReturnValue(of(undefined));
    component['categories'].set([parent, child]);
    component['categoryToDelete'].set(parent);
    component['executeDelete']();

    const remaining = component['categories']();
    expect(remaining).not.toContainEqual(expect.objectContaining({ id: 'p1' }));
    expect(remaining).not.toContainEqual(expect.objectContaining({ id: 'c1' }));
    expect(toastMock.success).toHaveBeenCalledWith('Category deleted');
  });

  it('executeDelete shows error toast on failure', () => {
    categoryApiMock.delete.mockReturnValue(throwError(() => new Error()));
    const cat = makeCategory();
    component['categories'].set([cat]);
    component['categoryToDelete'].set(cat);
    component['executeDelete']();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to delete category');
  });

  it('openCreateDialog with a parent pre-populates available parents', () => {
    const parent = makeCategory({ id: 'p1' });
    component['categories'].set([parent]);
    component['openCreateDialog'](TransactionType.EXPENSE, parent);
    expect(component['dialogAvailableParents']()).toEqual([parent]);
    expect(component['dialogFixedType']()).toBe(TransactionType.EXPENSE);
    expect(component['formDialogOpen']()).toBe(true);
  });
});

