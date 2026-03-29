import { Component, input } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { TrendChartComponent } from './trend-chart.component';
import { ChartWrapperComponent } from '../../../shared/components/chart-wrapper/chart-wrapper.component';
import { TrendResponse } from '../../../core/models/analytics.models';

@Component({ selector: 'app-chart-wrapper', template: '<canvas></canvas>', standalone: true })
class ChartWrapperStub {
  readonly config = input.required<any>();
}

const makeTrend = (year: number, month: number): TrendResponse => ({
  year,
  month,
  totalIncome: 3000,
  totalExpenses: 1500,
  netSavings: 1500,
  savingsRate: 50,
  transactionCount: 10,
});

describe('TrendChartComponent', () => {
  let fixture: ComponentFixture<TrendChartComponent>;
  let component: TrendChartComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    TestBed.overrideComponent(TrendChartComponent, {
      remove: { imports: [ChartWrapperComponent] },
      add: { imports: [ChartWrapperStub] },
    });
    fixture = TestBed.createComponent(TrendChartComponent);
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
    expect(fixture.nativeElement.textContent).toContain('No trend data');
  });

  it('renders chart wrapper when data is provided', () => {
    fixture.componentRef.setInput('data', [makeTrend(2025, 1), makeTrend(2025, 2)]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-chart-wrapper')).toBeTruthy();
  });

  it('builds line chart config with three datasets', () => {
    fixture.componentRef.setInput('data', [makeTrend(2025, 1), makeTrend(2025, 2)]);
    fixture.detectChanges();
    const config = component.chartConfig();
    expect(config.type).toBe('line');
    expect(config.data.datasets).toHaveLength(3);
  });

  it('sorts data by year and month ascending', () => {
    fixture.componentRef.setInput('data', [makeTrend(2025, 3), makeTrend(2025, 1), makeTrend(2025, 2)]);
    fixture.detectChanges();
    const config = component.chartConfig();
    const labels = config.data.labels as string[];
    expect(labels[0]).toBe('Jan 2025');
    expect(labels[1]).toBe('Feb 2025');
    expect(labels[2]).toBe('Mar 2025');
  });

  it('hides legend and axes in mini mode', () => {
    fixture.componentRef.setInput('data', [makeTrend(2025, 1)]);
    fixture.componentRef.setInput('mini', true);
    fixture.detectChanges();
    const config = component.chartConfig();
    expect(config.options?.plugins?.legend?.display).toBe(false);
    expect((config.options?.scales as any)?.x?.display).toBe(false);
  });

  it('shows legend and axes when not in mini mode', () => {
    fixture.componentRef.setInput('data', [makeTrend(2025, 1)]);
    fixture.componentRef.setInput('mini', false);
    fixture.detectChanges();
    const config = component.chartConfig();
    expect(config.options?.plugins?.legend?.display).toBe(true);
  });

  it('dataset labels are Income, Expenses, Net Savings', () => {
    fixture.componentRef.setInput('data', [makeTrend(2025, 1)]);
    fixture.detectChanges();
    const config = component.chartConfig();
    const labels = config.data.datasets.map((d) => d.label);
    expect(labels).toContain('Income');
    expect(labels).toContain('Expenses');
    expect(labels).toContain('Net Savings');
  });
});
