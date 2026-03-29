import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { FormFieldComponent } from './form-field.component';

@Component({
  imports: [FormFieldComponent],
  template: `
    <app-form-field [label]="label" [error]="error" [hint]="hint" [required]="required" fieldId="name">
      <input id="name" />
    </app-form-field>
  `,
})
class TestHostComponent {
  label = '';
  error = '';
  hint = '';
  required = false;
}

describe('FormFieldComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('renders label when provided', () => {
    host.label = 'Email';
    fixture.detectChanges();
    const label = fixture.nativeElement.querySelector('label') as HTMLElement;
    expect(label.textContent).toContain('Email');
  });

  it('hides label when empty', () => {
    fixture.detectChanges();
    const label = fixture.nativeElement.querySelector('label');
    expect(label).toBeNull();
  });

  it('shows required asterisk when required is true', () => {
    host.label = 'Email';
    host.required = true;
    fixture.detectChanges();
    const asterisk = fixture.nativeElement.querySelector('[aria-hidden="true"]') as HTMLElement;
    expect(asterisk.textContent).toContain('*');
  });

  it('shows error message', () => {
    host.error = 'This field is required';
    fixture.detectChanges();
    const err = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(err.textContent).toContain('This field is required');
  });

  it('shows hint when no error', () => {
    host.hint = 'Enter your email';
    fixture.detectChanges();
    const hint = fixture.nativeElement.querySelector('p') as HTMLElement;
    expect(hint.textContent).toContain('Enter your email');
  });

  it('prefers error over hint', () => {
    host.error = 'Required';
    host.hint = 'Some hint';
    fixture.detectChanges();
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(alert.textContent).toContain('Required');
    const paragraphs = fixture.nativeElement.querySelectorAll('p');
    expect(paragraphs.length).toBe(1);
  });
});
