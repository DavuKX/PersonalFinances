import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { ClickOutsideDirective } from './click-outside.directive';

@Component({
  imports: [ClickOutsideDirective],
  template: `
    <div appClickOutside (clickOutside)="onOutside()">
      <button id="inside">inside</button>
    </div>
    <button id="outside">outside</button>
  `,
})
class TestHostComponent {
  outsideClicked = false;
  onOutside() {
    this.outsideClicked = true;
  }
}

describe('ClickOutsideDirective', () => {
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
  });

  it('should not emit when clicking inside', () => {
    const inside = fixture.nativeElement.querySelector('#inside') as HTMLElement;
    inside.click();
    expect(fixture.componentInstance.outsideClicked).toBe(false);
  });

  it('should emit when clicking outside', () => {
    const outside = fixture.nativeElement.querySelector('#outside') as HTMLElement;
    outside.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(fixture.componentInstance.outsideClicked).toBe(true);
  });
});

