import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { WalletFormDialogComponent } from './wallet-form-dialog.component';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { WalletResponse } from '../../../core/models/wallet.models';

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

describe('WalletFormDialogComponent', () => {
  let fixture: ComponentFixture<WalletFormDialogComponent>;
  let component: WalletFormDialogComponent;

  const walletApiMock = { create: vi.fn(), update: vi.fn() };
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
    fixture = TestBed.createComponent(WalletFormDialogComponent);
    component = fixture.componentInstance;
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('form is invalid when name is empty', () => {
    fixture.componentRef.setInput('wallet', null);
    fixture.detectChanges();
    expect(component['form'].invalid).toBe(true);
  });

  it('create calls walletApi.create and emits saved', () => {
    walletApiMock.create.mockReturnValue(of(mockWallet));
    const savedValues: WalletResponse[] = [];
    component.saved.subscribe((w) => savedValues.push(w));

    fixture.componentRef.setInput('wallet', null);
    fixture.detectChanges();

    component['form'].setValue({ name: 'New Wallet', currency: 'USD', balance: 500 });
    component['submit']();

    expect(walletApiMock.create).toHaveBeenCalledWith({
      name: 'New Wallet',
      currency: 'USD',
      balance: 500,
    });
    expect(savedValues).toHaveLength(1);
    expect(toastMock.success).toHaveBeenCalledWith('Wallet created');
  });

  it('update calls walletApi.update and emits saved', () => {
    walletApiMock.update.mockReturnValue(of(mockWallet));
    const savedValues: WalletResponse[] = [];
    component.saved.subscribe((w) => savedValues.push(w));

    fixture.componentRef.setInput('wallet', mockWallet);
    fixture.detectChanges();

    component['form'].controls.name.setValue('Updated Name');
    component['submit']();

    expect(walletApiMock.update).toHaveBeenCalledWith('wallet-uuid-1', { name: 'Updated Name' });
    expect(savedValues).toHaveLength(1);
    expect(toastMock.success).toHaveBeenCalledWith('Wallet updated');
  });

  it('shows error toast when create fails', () => {
    walletApiMock.create.mockReturnValue(throwError(() => new Error('server error')));
    fixture.componentRef.setInput('wallet', null);
    fixture.detectChanges();

    component['form'].setValue({ name: 'Wallet', currency: 'USD', balance: 0 });
    component['submit']();

    expect(toastMock.error).toHaveBeenCalledWith('Failed to create wallet');
  });
});

