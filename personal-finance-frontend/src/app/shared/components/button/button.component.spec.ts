import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { ButtonComponent } from './button.component';

@Component({
  imports: [ButtonComponent],
  template: `<app-button [variant]="variant" [disabled]="disabled" [loading]="loading">Click me</app-button>`,
})
class TestHostComponent {
  variant: 'primary' | 'secondary' | 'danger' | 'ghost' = 'primary';
  disabled = false;
  loading = false;
}

describe('ButtonComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('renders button with content', () => {
    fixture.detectChanges();
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(btn.textContent).toContain('Click me');
  });

  it('is disabled when disabled input is true', () => {
    host.disabled = true;
    fixture.detectChanges();
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(btn.disabled).toBe(true);
  });

  it('is disabled when loading', () => {
    host.loading = true;
    fixture.detectChanges();
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(btn.disabled).toBe(true);
  });

  it('shows spinner when loading', () => {
    host.loading = true;
    fixture.detectChanges();
    const spinner = fixture.nativeElement.querySelector('.animate-spin');
    expect(spinner).not.toBeNull();
  });

  it('applies danger variant classes', () => {
    host.variant = 'danger';
    fixture.detectChanges();
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(btn.className).toContain('bg-rose-600');
  });
});
