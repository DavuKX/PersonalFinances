import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { DropdownComponent, DropdownItem } from './dropdown.component';

@Component({
  imports: [DropdownComponent],
  template: `
    <app-dropdown
      [items]="items"
      [selectedValue]="selected"
      (itemSelected)="onSelect($event)"
    />
  `,
})
class TestHostComponent {
  items: DropdownItem[] = [
    { label: 'Apple', value: 'apple' },
    { label: 'Banana', value: 'banana' },
    { label: 'Disabled', value: 'disabled', disabled: true },
  ];
  selected = '';
  lastSelected: DropdownItem | null = null;
  onSelect(item: DropdownItem) { this.lastSelected = item; }
}

describe('DropdownComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('does not show list initially', () => {
    expect(fixture.nativeElement.querySelector('[role="listbox"]')).toBeNull();
  });

  it('opens list when trigger is clicked', () => {
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    btn.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="listbox"]')).not.toBeNull();
  });

  it('emits itemSelected when an item is clicked', () => {
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    btn.click();
    fixture.detectChanges();
    const options = fixture.nativeElement.querySelectorAll('[role="option"]') as NodeListOf<HTMLElement>;
    options[0].click();
    expect(host.lastSelected?.value).toBe('apple');
  });

  it('closes list after selection', () => {
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    btn.click();
    fixture.detectChanges();
    const options = fixture.nativeElement.querySelectorAll('[role="option"]') as NodeListOf<HTMLElement>;
    options[0].click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="listbox"]')).toBeNull();
  });

  it('shows placeholder when no value selected', () => {
    const btn = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(btn.textContent).toContain('Select…');
  });
});

