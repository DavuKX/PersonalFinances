import { Directive, ElementRef, output, inject, HostListener } from '@angular/core';

@Directive({
  selector: '[appClickOutside]',
})
export class ClickOutsideDirective {
  private readonly elementRef = inject(ElementRef);

  readonly clickOutside = output<void>();

  @HostListener('document:click', ['$event.target'])
  onDocumentClick(target: EventTarget | null): void {
    if (target && !this.elementRef.nativeElement.contains(target)) {
      this.clickOutside.emit();
    }
  }
}

