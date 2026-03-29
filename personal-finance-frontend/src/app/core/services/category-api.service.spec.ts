import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CategoryApiService } from './category-api.service';
import { CategoryResponse, CreateCategoryRequest } from '../models/category.models';
import { TransactionType } from '../models/transaction.models';

const mockCategory: CategoryResponse = {
  id: 'cat-uuid-1',
  name: 'Food',
  transactionType: TransactionType.EXPENSE,
  parentId: null,
  isDefault: false,
  createdAt: '2025-01-01T00:00:00Z',
};

describe('CategoryApiService', () => {
  let service: CategoryApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CategoryApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll sends GET to /api/v1/categories without params', () => {
    service.getAll().subscribe((cats) => expect(cats).toEqual([mockCategory]));
    httpMock.expectOne('/api/v1/categories').flush([mockCategory]);
  });

  it('getAll with transactionType includes query param', () => {
    service.getAll(TransactionType.EXPENSE).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/categories');
    expect(req.request.params.get('transactionType')).toBe('EXPENSE');
    req.flush([mockCategory]);
  });

  it('getById sends GET to /api/v1/categories/:id', () => {
    service.getById('cat-uuid-1').subscribe((c) => expect(c).toEqual(mockCategory));
    httpMock.expectOne('/api/v1/categories/cat-uuid-1').flush(mockCategory);
  });

  it('getSubcategories sends GET to /api/v1/categories/:id/subcategories', () => {
    service.getSubcategories('cat-uuid-1').subscribe((cats) => expect(cats).toEqual([mockCategory]));
    httpMock.expectOne('/api/v1/categories/cat-uuid-1/subcategories').flush([mockCategory]);
  });

  it('create sends POST to /api/v1/categories', () => {
    const req: CreateCategoryRequest = {
      name: 'Food',
      transactionType: TransactionType.EXPENSE,
      parentId: null,
    };
    service.create(req).subscribe((c) => expect(c).toEqual(mockCategory));
    const r = httpMock.expectOne('/api/v1/categories');
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush(mockCategory);
  });

  it('delete sends DELETE to /api/v1/categories/:id', () => {
    service.delete('cat-uuid-1').subscribe();
    const r = httpMock.expectOne('/api/v1/categories/cat-uuid-1');
    expect(r.request.method).toBe('DELETE');
    r.flush(null);
  });
});

