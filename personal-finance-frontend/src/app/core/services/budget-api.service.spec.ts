import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BudgetApiService } from './budget-api.service';
import {
  BudgetPeriod,
  BudgetResponse,
  BudgetSummaryResponse,
  BudgetType,
  CreateBudgetRequest,
  UpdateBudgetRequest,
  BulkBudgetRequest,
} from '../models/budget.models';

describe('BudgetApiService', () => {
  let service: BudgetApiService;
  let http: HttpTestingController;

  const walletId = 'wallet-1';
  const budgetId = 'budget-1';
  const categoryId = 'cat-1';

  const mockBudget: BudgetResponse = {
    id: budgetId,
    walletId,
    userId: 'user-1',
    categoryId,
    budgetType: BudgetType.FIXED,
    amount: 500,
    period: BudgetPeriod.MONTHLY,
    createdAt: '2026-04-01T00:00:00Z',
    updatedAt: '2026-04-01T00:00:00Z',
  };

  const mockSummary: BudgetSummaryResponse = {
    ...mockBudget,
    resolvedAmount: 500,
    spentAmount: 200,
    remainingAmount: 300,
    percentUsed: 40,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(BudgetApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('create() should POST and return BudgetResponse', () => {
    const req: CreateBudgetRequest = { categoryId, budgetType: BudgetType.FIXED, amount: 500 };
    service.create(walletId, req).subscribe(res => expect(res).toEqual(mockBudget));
    const r = http.expectOne(`/api/v1/wallets/${walletId}/budgets`);
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush(mockBudget);
  });

  it('listByWallet() should GET and return array', () => {
    service.listByWallet(walletId).subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].id).toBe(budgetId);
    });
    const r = http.expectOne(`/api/v1/wallets/${walletId}/budgets`);
    expect(r.request.method).toBe('GET');
    r.flush([mockSummary]);
  });

  it('getById() should GET single budget summary', () => {
    service.getById(walletId, budgetId).subscribe(res => expect(res).toEqual(mockSummary));
    const r = http.expectOne(`/api/v1/wallets/${walletId}/budgets/${budgetId}`);
    expect(r.request.method).toBe('GET');
    r.flush(mockSummary);
  });

  it('update() should PUT and return BudgetResponse', () => {
    const req: UpdateBudgetRequest = { budgetType: BudgetType.FIXED, amount: 800 };
    service.update(walletId, budgetId, req).subscribe(res => expect(res.amount).toBe(800));
    const r = http.expectOne(`/api/v1/wallets/${walletId}/budgets/${budgetId}`);
    expect(r.request.method).toBe('PUT');
    r.flush({ ...mockBudget, amount: 800 });
  });

  it('delete() should send DELETE request', () => {
    service.delete(walletId, budgetId).subscribe();
    const r = http.expectOne(`/api/v1/wallets/${walletId}/budgets/${budgetId}`);
    expect(r.request.method).toBe('DELETE');
    r.flush(null);
  });

  it('setBulk() should PUT to bulk endpoint and return array', () => {
    const req: BulkBudgetRequest = {
      monthlyIncome: 10000,
      allocations: [{ categoryId, budgetType: BudgetType.PERCENTAGE, amount: 50 }],
    };
    service.setBulk(walletId, req).subscribe(res => expect(res.length).toBe(1));
    const r = http.expectOne(`/api/v1/wallets/${walletId}/budgets/bulk`);
    expect(r.request.method).toBe('PUT');
    expect(r.request.body).toEqual(req);
    r.flush([mockBudget]);
  });
});

