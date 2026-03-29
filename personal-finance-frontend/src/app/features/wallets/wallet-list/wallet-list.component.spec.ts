import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ComponentFixture } from '@angular/core/testing';
import { WalletListComponent } from './wallet-list.component';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { WalletResponse } from '../../../core/models/wallet.models';

const makeWallet = (overrides: Partial<WalletResponse> = {}): WalletResponse => ({
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
  ...overrides,
});

describe('WalletListComponent', () => {
  let fixture: ComponentFixture<WalletListComponent>;
  let component: WalletListComponent;

  const walletApiMock = {
    getAll: vi.fn(),
    archive: vi.fn(),
    restore: vi.fn(),
    delete: vi.fn(),
  };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    walletApiMock.getAll.mockReturnValue(of([]));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    fixture = TestBed.createComponent(WalletListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('loads wallets on init', () => {
    expect(walletApiMock.getAll).toHaveBeenCalledOnce();
  });

  it('shows error toast when getAll fails', () => {
    walletApiMock.getAll.mockReturnValue(throwError(() => new Error()));
    fixture = TestBed.createComponent(WalletListComponent);
    fixture.detectChanges();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to load wallets');
  });

  it('visibleWallets excludes archived by default', () => {
    const active = makeWallet({ id: 'w1' });
    const archived = makeWallet({ id: 'w2', archived: true });
    walletApiMock.getAll.mockReturnValue(of([active, archived]));
    fixture = TestBed.createComponent(WalletListComponent);
    fixture.detectChanges();
    component = fixture.componentInstance;
    expect(component['visibleWallets']()).toEqual([active]);
  });

  it('visibleWallets includes archived when showArchived is true', () => {
    const active = makeWallet({ id: 'w1' });
    const archived = makeWallet({ id: 'w2', archived: true });
    walletApiMock.getAll.mockReturnValue(of([active, archived]));
    fixture = TestBed.createComponent(WalletListComponent);
    fixture.detectChanges();
    component = fixture.componentInstance;
    component['showArchived'].set(true);
    expect(component['visibleWallets']()).toEqual([active, archived]);
  });

  it('onWalletSaved adds new wallet to list', () => {
    const wallet = makeWallet();
    component['onWalletSaved'](wallet);
    expect(component['wallets']()).toContain(wallet);
  });

  it('onWalletSaved updates existing wallet in list', () => {
    const original = makeWallet({ name: 'Old Name' });
    component['wallets'].set([original]);
    const updated = makeWallet({ name: 'New Name' });
    component['onWalletSaved'](updated);
    expect(component['wallets']()[0].name).toBe('New Name');
  });

  it('archive calls walletApi.archive and updates list', () => {
    const wallet = makeWallet();
    const archived = makeWallet({ archived: true });
    walletApiMock.archive.mockReturnValue(of(archived));
    component['wallets'].set([wallet]);
    component['archive'](wallet);
    expect(walletApiMock.archive).toHaveBeenCalledWith('wallet-uuid-1');
    expect(component['wallets']()[0].archived).toBe(true);
    expect(toastMock.success).toHaveBeenCalled();
  });

  it('restore calls walletApi.restore and updates list', () => {
    const wallet = makeWallet({ archived: true });
    const restored = makeWallet({ archived: false });
    walletApiMock.restore.mockReturnValue(of(restored));
    component['wallets'].set([wallet]);
    component['restore'](wallet);
    expect(walletApiMock.restore).toHaveBeenCalledWith('wallet-uuid-1');
    expect(component['wallets']()[0].archived).toBe(false);
  });

  it('executeDelete removes wallet and shows toast', () => {
    const wallet = makeWallet();
    walletApiMock.delete.mockReturnValue(of(undefined));
    component['wallets'].set([wallet]);
    component['walletToDelete'].set(wallet);
    component['executeDelete']();
    expect(walletApiMock.delete).toHaveBeenCalledWith('wallet-uuid-1');
    expect(component['wallets']()).toHaveLength(0);
    expect(toastMock.success).toHaveBeenCalledWith('Wallet deleted');
  });

  it('navigateToDetail navigates to /wallets/:id', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate');
    component['navigateToDetail']('wallet-uuid-1');
    expect(router.navigate).toHaveBeenCalledWith(['/wallets', 'wallet-uuid-1']);
  });
});

