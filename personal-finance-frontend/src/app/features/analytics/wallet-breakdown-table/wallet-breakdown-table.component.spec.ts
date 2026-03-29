import { TestBed, ComponentFixture } from '@angular/core/testing';
import { WalletBreakdownTableComponent } from './wallet-breakdown-table.component';
import { WalletBreakdownResponse } from '../../../core/models/analytics.models';
import { WalletResponse } from '../../../core/models/wallet.models';

const makeBreakdown = (walletId: string): WalletBreakdownResponse => ({
  walletId,
  year: 2025,
  month: 3,
  totalIncome: 3000,
  totalExpenses: 1500,
  netSavings: 1500,
  savingsRate: 50,
  transactionCount: 10,
});

const makeWallet = (id: string, name: string): WalletResponse => ({
  id,
  name,
  currency: 'USD',
  balance: 1000,
  archived: false,
  spendingLimitAmount: null,
  spendingLimitPeriod: null,
  archivedAt: null,
  createdAt: '',
  updatedAt: '',
});

describe('WalletBreakdownTableComponent', () => {
  let fixture: ComponentFixture<WalletBreakdownTableComponent>;
  let component: WalletBreakdownTableComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    fixture = TestBed.createComponent(WalletBreakdownTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('shows spinner when loading', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-spinner')).toBeTruthy();
  });

  it('shows empty state when data is empty', () => {
    fixture.componentRef.setInput('data', []);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No wallet data');
  });

  it('renders table rows for each wallet breakdown', () => {
    fixture.componentRef.setInput('data', [makeBreakdown('w-1'), makeBreakdown('w-2')]);
    fixture.detectChanges();
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
  });

  it('displays wallet name resolved from wallets input', () => {
    fixture.componentRef.setInput('data', [makeBreakdown('w-1')]);
    fixture.componentRef.setInput('wallets', [makeWallet('w-1', 'Main Account')]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Main Account');
  });

  it('falls back to walletId when wallet is not found', () => {
    fixture.componentRef.setInput('data', [makeBreakdown('unknown-id')]);
    fixture.componentRef.setInput('wallets', []);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('unknown-id');
  });

  it('displays income, expenses and savings rate values', () => {
    fixture.componentRef.setInput('data', [makeBreakdown('w-1')]);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('3,000.00');
    expect(text).toContain('1,500.00');
    expect(text).toContain('50.0%');
  });

  it('walletName returns wallet name when found', () => {
    fixture.componentRef.setInput('wallets', [makeWallet('w-1', 'Savings')]);
    fixture.detectChanges();
    expect(component['walletName']('w-1')).toBe('Savings');
  });

  it('walletName returns walletId when wallet not found', () => {
    fixture.componentRef.setInput('wallets', []);
    fixture.detectChanges();
    expect(component['walletName']('xyz')).toBe('xyz');
  });
});

