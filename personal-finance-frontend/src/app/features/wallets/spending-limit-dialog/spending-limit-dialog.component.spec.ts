import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { SpendingLimitDialogComponent } from './spending-limit-dialog.component';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { LimitPeriod, WalletResponse } from '../../../core/models/wallet.models';

const mockWallet: WalletResponse = {
  id: 'wallet-uuid-1',
  name: 'My Wallet',
  currency: 'USD',
  balance: 1000,
  archived: false,
  spendingLimitAmount: 500,
  spendingLimitPeriod: LimitPeriod.MONTHLY,
  archivedAt: null,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
};

describe('SpendingLimitDialogComponent', () => {
  let fixture: ComponentFixture<SpendingLimitDialogComponent>;
  let component: SpendingLimitDialogComponent;

  const walletApiMock = { setSpendingLimit: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    fixture = TestBed.createComponent(SpendingLimitDialogComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('walletId', 'wallet-uuid-1');
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('form is invalid when amount is empty', () => {
    fixture.detectChanges();
    expect(component['form'].invalid).toBe(true);
  });

  it('submit calls walletApi.setSpendingLimit and emits saved', () => {
    walletApiMock.setSpendingLimit.mockReturnValue(of(mockWallet));
    const savedValues: WalletResponse[] = [];
    component.saved.subscribe((w) => savedValues.push(w));

    fixture.detectChanges();
    component['form'].setValue({ amount: 500, period: LimitPeriod.MONTHLY });
    component['submit']();

    expect(walletApiMock.setSpendingLimit).toHaveBeenCalledWith('wallet-uuid-1', {
      amount: 500,
      period: LimitPeriod.MONTHLY,
    });
    expect(savedValues).toHaveLength(1);
    expect(toastMock.success).toHaveBeenCalledWith('Spending limit set');
  });

  it('shows error toast when setSpendingLimit fails', () => {
    walletApiMock.setSpendingLimit.mockReturnValue(throwError(() => new Error()));
    fixture.detectChanges();
    component['form'].setValue({ amount: 500, period: LimitPeriod.MONTHLY });
    component['submit']();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to set spending limit');
  });
});

