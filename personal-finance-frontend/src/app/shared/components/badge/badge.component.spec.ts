import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { BadgeComponent } from './badge.component';

@Component({
  imports: [BadgeComponent],
  template: `<app-badge [variant]="variant">Active</app-badge>`,
})
class TestHostComponent {
  variant: 'success' | 'danger' | 'warning' | 'info' | 'default' = 'default';
}

describe('BadgeComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('renders projected content', () => {
    fixture.detectChanges();
    const span = fixture.nativeElement.querySelector('span') as HTMLElement;
    expect(span.textContent).toContain('Active');
  });

  it('applies success variant classes', () => {
    host.variant = 'success';
    fixture.detectChanges();
    const span = fixture.nativeElement.querySelector('span') as HTMLElement;
    expect(span.className).toContain('bg-emerald-100');
  });

  it('applies danger variant classes', () => {
    host.variant = 'danger';
    fixture.detectChanges();
    const span = fixture.nativeElement.querySelector('span') as HTMLElement;
    expect(span.className).toContain('bg-rose-100');
  });

  it('applies default variant classes', () => {
    fixture.detectChanges();
    const span = fixture.nativeElement.querySelector('span') as HTMLElement;
    expect(span.className).toContain('bg-gray-100');
  });
});
