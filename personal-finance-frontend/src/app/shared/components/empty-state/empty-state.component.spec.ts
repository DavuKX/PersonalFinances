import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { EmptyStateComponent } from './empty-state.component';

@Component({
  imports: [EmptyStateComponent],
  template: `<app-empty-state [title]="title" [description]="description" [icon]="icon" />`,
})
class TestHostComponent {
  title = 'No data';
  description = '';
  icon = '';
}

describe('EmptyStateComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('renders the title', () => {
    fixture.detectChanges();
    const h3 = fixture.nativeElement.querySelector('h3') as HTMLElement;
    expect(h3.textContent).toContain('No data');
  });

  it('shows description when provided', () => {
    host.description = 'Try adding some items';
    fixture.detectChanges();
    const p = fixture.nativeElement.querySelector('p') as HTMLElement;
    expect(p.textContent).toContain('Try adding some items');
  });

  it('hides description when empty', () => {
    fixture.detectChanges();
    const p = fixture.nativeElement.querySelector('p');
    expect(p).toBeNull();
  });

  it('renders icon when provided', () => {
    host.icon = '📭';
    fixture.detectChanges();
    const iconEl = fixture.nativeElement.querySelector('[aria-hidden="true"]') as HTMLElement;
    expect(iconEl.textContent).toContain('📭');
  });

  it('hides icon element when icon is empty', () => {
    fixture.detectChanges();
    const iconEl = fixture.nativeElement.querySelector('[aria-hidden="true"]');
    expect(iconEl).toBeNull();
  });
});
