import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { SpinnerComponent } from './spinner.component';

@Component({
  imports: [SpinnerComponent],
  template: `<app-spinner [size]="size" />`,
})
class TestHostComponent {
  size: 'sm' | 'md' | 'lg' | 'xl' = 'md';
}

describe('SpinnerComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('has role="status"', () => {
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('[role="status"]') as HTMLElement;
    expect(el).not.toBeNull();
  });

  it('applies sm size classes', () => {
    host.size = 'sm';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('[role="status"]') as HTMLElement;
    expect(el.className).toContain('w-4');
  });

  it('applies xl size classes', () => {
    host.size = 'xl';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('[role="status"]') as HTMLElement;
    expect(el.className).toContain('w-12');
  });
});
