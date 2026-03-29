import { TestBed } from '@angular/core/testing';
import { TokenStorageService } from './token-storage.service';

describe('TokenStorageService', () => {
  let service: TokenStorageService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenStorageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('accessToken() is null initially', () => {
    expect(service.accessToken()).toBeNull();
  });

  it('saveTokens() stores tokens in localStorage and signals', () => {
    service.saveTokens('at', 'rt');
    expect(service.accessToken()).toBe('at');
    expect(service.refreshToken()).toBe('rt');
    expect(localStorage.getItem('access_token')).toBe('at');
    expect(localStorage.getItem('refresh_token')).toBe('rt');
  });

  it('clearTokens() removes tokens from localStorage and signals', () => {
    service.saveTokens('at', 'rt');
    service.clearTokens();
    expect(service.accessToken()).toBeNull();
    expect(service.refreshToken()).toBeNull();
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(localStorage.getItem('refresh_token')).toBeNull();
  });

  it('initializes from localStorage if tokens were previously stored', () => {
    localStorage.setItem('access_token', 'stored-at');
    localStorage.setItem('refresh_token', 'stored-rt');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    const freshService = TestBed.inject(TokenStorageService);
    expect(freshService.accessToken()).toBe('stored-at');
    expect(freshService.refreshToken()).toBe('stored-rt');
  });
});

