import { Directive, ElementRef, inject, AfterViewInit } from '@angular/core';

@Directive({
  selector: '[appAutofocus]',
})
export class AutofocusDirective implements AfterViewInit {
  private readonly elementRef = inject(ElementRef);

  ngAfterViewInit(): void {
    setTimeout(() => (this.elementRef.nativeElement as HTMLElement).focus(), 0);
  }
}

