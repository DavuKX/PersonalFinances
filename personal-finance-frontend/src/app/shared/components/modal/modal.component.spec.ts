import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { ModalComponent } from './modal.component';

@Component({
  imports: [ModalComponent],
  template: `
    <app-modal [isOpen]="isOpen" [title]="title" (closed)="onClosed()">
      <p id="content">Modal body</p>
    </app-modal>
  `,
})
class TestHostComponent {
  isOpen = false;
  title = 'Test Modal';
  closed = false;
  onClosed() {
    this.closed = true;
  }
}

describe('ModalComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('does not render when isOpen is false', () => {
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('[role="dialog"]');
    expect(dialog).toBeNull();
  });

  it('renders when isOpen is true', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('[role="dialog"]');
    expect(dialog).not.toBeNull();
  });

  it('shows the title', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const h2 = fixture.nativeElement.querySelector('h2') as HTMLElement;
    expect(h2.textContent).toContain('Test Modal');
  });

  it('emits closed when close button is clicked', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const closeBtn = fixture.nativeElement.querySelector('[aria-label="Close"]') as HTMLElement;
    closeBtn.click();
    expect(host.closed).toBe(true);
  });

  it('projects content inside the modal', () => {
    host.isOpen = true;
    fixture.detectChanges();
    const content = fixture.nativeElement.querySelector('#content') as HTMLElement;
    expect(content.textContent).toBe('Modal body');
  });
});
