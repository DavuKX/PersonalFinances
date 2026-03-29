import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { AutofocusDirective } from './autofocus.directive';

@Component({
  imports: [AutofocusDirective],
  template: `<input appAutofocus id="target" />`,
})
class TestHostComponent {}

describe('AutofocusDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
  });

  it('should focus the element after view init', async () => {
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 10));
    const input = fixture.nativeElement.querySelector('#target') as HTMLInputElement;
    expect(document.activeElement).toBe(input);
  });
});

