import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

@Component({
  imports: [ConfirmationDialogComponent],
  template: `
    <app-confirmation-dialog
      [isOpen]="isOpen"
      [title]="title"
      [message]="message"
      (confirmed)="onConfirm()"
      (cancelled)="onCancel()"
    />
  `,
})
class TestHostComponent {
  isOpen = false;
  title = 'Delete item?';
  message = 'This cannot be undone.';
  confirmed = false;
  cancelled = false;
  onConfirm() { this.confirmed = true; }
  onCancel() { this.cancelled = true; }
}

describe('ConfirmationDialogComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('does not render when closed', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeNull();
  });

  it('renders dialog when open', () => {
    host.isOpen = true;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).not.toBeNull();
  });

  it('shows the message', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('This cannot be undone.');
  });

  it('emits confirmed when confirm button clicked', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>;
    const confirmBtn = Array.from(buttons).find(b => b.textContent?.includes('Confirm'));
    confirmBtn?.click();
    expect(host.confirmed).toBe(true);
  });

  it('emits cancelled when cancel button clicked', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>;
    const cancelBtn = Array.from(buttons).find(b => b.textContent?.includes('Cancel'));
    cancelBtn?.click();
    expect(host.cancelled).toBe(true);
  });
});
