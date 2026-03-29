import { TestBed, ComponentFixture } from '@angular/core/testing';
import { Component } from '@angular/core';
import { PaginationComponent } from './pagination.component';

@Component({
  imports: [PaginationComponent],
  template: `
    <app-pagination
      [currentPage]="currentPage"
      [totalPages]="totalPages"
      [totalElements]="totalElements"
      [pageSize]="pageSize"
      (pageChange)="onPageChange($event)"
    />
  `,
})
class TestHostComponent {
  currentPage = 1;
  totalPages = 5;
  totalElements = 50;
  pageSize = 10;
  emittedPage: number | null = null;
  onPageChange(p: number) {
    this.emittedPage = p;
  }
}

describe('PaginationComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;
  let host: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TestHostComponent] }).compileComponents();
    fixture = TestBed.createComponent(TestHostComponent);
    host = fixture.componentInstance;
  });

  it('disables previous button on first page', () => {
    fixture.detectChanges();
    const prev = fixture.nativeElement.querySelector('[aria-label="Previous page"]') as HTMLButtonElement;
    expect(prev.disabled).toBe(true);
  });

  it('enables next button when not on last page', () => {
    fixture.detectChanges();
    const next = fixture.nativeElement.querySelector('[aria-label="Next page"]') as HTMLButtonElement;
    expect(next.disabled).toBe(false);
  });

  it('disables next button on last page', () => {
    host.currentPage = 5;
    fixture.detectChanges();
    const next = fixture.nativeElement.querySelector('[aria-label="Next page"]') as HTMLButtonElement;
    expect(next.disabled).toBe(true);
  });

  it('emits next page when next is clicked', () => {
    fixture.detectChanges();
    const next = fixture.nativeElement.querySelector('[aria-label="Next page"]') as HTMLButtonElement;
    next.click();
    expect(host.emittedPage).toBe(2);
  });

  it('shows correct item range', () => {
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('1–10 of 50');
  });
});
