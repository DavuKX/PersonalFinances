import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

const stubMatchMedia = (matches: boolean) => {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockReturnValue({ matches }),
  });
};

describe('ThemeService', () => {
  let service: ThemeService;

  beforeEach(() => {
    stubMatchMedia(false);
    localStorage.clear();
    document.documentElement.classList.remove('dark');
    TestBed.configureTestingModule({});
    service = TestBed.inject(ThemeService);
    TestBed.flushEffects();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('defaults to light when no stored theme and system prefers light', () => {
    expect(service.theme()).toBe('light');
    expect(service.isDark()).toBe(false);
  });

  it('defaults to dark when system prefers dark', () => {
    stubMatchMedia(true);
    localStorage.removeItem('theme');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    const freshService = TestBed.inject(ThemeService);
    expect(freshService.theme()).toBe('dark');
  });

  it('restores dark theme from localStorage', () => {
    localStorage.setItem('theme', 'dark');
    TestBed.resetTestingModule();
    stubMatchMedia(false);
    TestBed.configureTestingModule({});
    const freshService = TestBed.inject(ThemeService);
    expect(freshService.theme()).toBe('dark');
  });

  it('toggle() switches from light to dark', () => {
    service.setTheme('light');
    TestBed.flushEffects();
    service.toggle();
    TestBed.flushEffects();
    expect(service.theme()).toBe('dark');
    expect(service.isDark()).toBe(true);
  });

  it('toggle() switches from dark to light', () => {
    service.setTheme('dark');
    TestBed.flushEffects();
    service.toggle();
    TestBed.flushEffects();
    expect(service.theme()).toBe('light');
    expect(service.isDark()).toBe(false);
  });

  it('setTheme(dark) adds dark class to documentElement', () => {
    service.setTheme('dark');
    TestBed.flushEffects();
    expect(document.documentElement.classList.contains('dark')).toBe(true);
  });

  it('setTheme(light) removes dark class from documentElement', () => {
    service.setTheme('dark');
    TestBed.flushEffects();
    service.setTheme('light');
    TestBed.flushEffects();
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });

  it('persists theme to localStorage on change', () => {
    service.setTheme('dark');
    TestBed.flushEffects();
    expect(localStorage.getItem('theme')).toBe('dark');
  });
});
