import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ToastService);
  });

  it('adds a success toast', () => {
    service.success('Saved!');
    expect(service.toasts().length).toBe(1);
    expect(service.toasts()[0].type).toBe('success');
    expect(service.toasts()[0].message).toBe('Saved!');
  });

  it('adds an error toast', () => {
    service.error('Failed!');
    expect(service.toasts()[0].type).toBe('error');
  });

  it('adds a warning toast', () => {
    service.warning('Be careful!');
    expect(service.toasts()[0].type).toBe('warning');
  });

  it('adds an info toast', () => {
    service.info('FYI');
    expect(service.toasts()[0].type).toBe('info');
  });

  it('dismiss() removes the toast by id', () => {
    service.success('hello');
    const id = service.toasts()[0].id;
    service.dismiss(id);
    expect(service.toasts().length).toBe(0);
  });

  it('auto-dismisses after duration', async () => {
    vi.useFakeTimers();
    service.success('auto', 100);
    expect(service.toasts().length).toBe(1);
    vi.advanceTimersByTime(200);
    expect(service.toasts().length).toBe(0);
    vi.useRealTimers();
  });
});

