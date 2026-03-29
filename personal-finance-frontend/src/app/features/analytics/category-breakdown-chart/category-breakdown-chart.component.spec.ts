import { Component, input } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { CategoryBreakdownChartComponent } from './category-breakdown-chart.component';
import { ChartWrapperComponent } from '../../../shared/components/chart-wrapper/chart-wrapper.component';
import { CategoryAnalyticsResponse } from '../../../core/models/analytics.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';

@Component({ selector: 'app-chart-wrapper', template: '<canvas></canvas>', standalone: true })
class ChartWrapperStub {
  readonly config = input.required<any>();
}

const mockData: CategoryAnalyticsResponse[] = [
  { categoryId: 'cat-1', transactionType: TransactionType.EXPENSE, year: 2025, month: 3, totalAmount: 500, transactionCount: 5 },
  { categoryId: 'cat-2', transactionType: TransactionType.EXPENSE, year: 2025, month: 3, totalAmount: 300, transactionCount: 3 },
];

const mockCategories: CategoryResponse[] = [
  { id: 'cat-1', name: 'Food', transactionType: TransactionType.EXPENSE, parentId: null, isDefault: true, createdAt: '' },
  { id: 'cat-2', name: 'Transport', transactionType: TransactionType.EXPENSE, parentId: null, isDefault: true, createdAt: '' },
];

describe('CategoryBreakdownChartComponent', () => {
  let fixture: ComponentFixture<CategoryBreakdownChartComponent>;
  let component: CategoryBreakdownChartComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    TestBed.overrideComponent(CategoryBreakdownChartComponent, {
      remove: { imports: [ChartWrapperComponent] },
      add: { imports: [ChartWrapperStub] },
    });
    fixture = TestBed.createComponent(CategoryBreakdownChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('shows spinner when loading', () => {
    fixture.componentRef.setInput('loading', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-spinner')).toBeTruthy();
  });

  it('shows empty state when data is empty', () => {
    fixture.componentRef.setInput('data', []);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No category data');
  });

  it('renders chart wrapper when data is provided', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.componentRef.setInput('categories', mockCategories);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-chart-wrapper')).toBeTruthy();
  });

  it('builds doughnut chart config with category names as labels', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.componentRef.setInput('categories', mockCategories);
    fixture.detectChanges();
    const config = component.chartConfig();
    expect(config.type).toBe('doughnut');
    expect(config.data.labels).toContain('Food');
    expect(config.data.labels).toContain('Transport');
  });

  it('falls back to categoryId when category name is not found', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.componentRef.setInput('categories', []);
    fixture.detectChanges();
    const config = component.chartConfig();
    expect(config.data.labels).toContain('cat-1');
  });

  it('sorts data by totalAmount descending', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.componentRef.setInput('categories', mockCategories);
    fixture.detectChanges();
    const config = component.chartConfig();
    const amounts = config.data.datasets[0].data as number[];
    expect(amounts[0]).toBeGreaterThanOrEqual(amounts[1]);
  });
});
