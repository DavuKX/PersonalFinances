import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { CardComponent } from './card.component';

@Component({
  imports: [CardComponent],
  template: `<app-card [title]="title"><span id="body">Body</span></app-card>`,
})
class TestHostComponent {
  title = '';
}

describe('CardComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('renders projected content', () => {
    fixture.detectChanges();
    const body = fixture.nativeElement.querySelector('#body') as HTMLElement;
    expect(body.textContent).toBe('Body');
  });

  it('shows title when provided', () => {
    host.title = 'My Card';
    fixture.detectChanges();
    const heading = fixture.nativeElement.querySelector('h3') as HTMLElement;
    expect(heading.textContent).toContain('My Card');
  });

  it('hides title header when title is empty', () => {
    fixture.detectChanges();
    const heading = fixture.nativeElement.querySelector('h3');
    expect(heading).toBeNull();
  });
});
