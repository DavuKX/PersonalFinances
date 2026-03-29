import { TestBed } from '@angular/core/testing';
import { ComponentFixture } from '@angular/core/testing';
import { MonthlyKpiCardsComponent } from './monthly-kpi-cards.component';
import { MonthlyAnalyticsResponse } from '../../../core/models/analytics.models';

const mockData: MonthlyAnalyticsResponse = {
  year: 2025, month: 3, totalIncome: 3000, totalExpenses: 1500,
  netSavings: 1500, savingsRate: 50, transactionCount: 10,
};

describe('MonthlyKpiCardsComponent', () => {
  let fixture: ComponentFixture<MonthlyKpiCardsComponent>;
  let component: MonthlyKpiCardsComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    fixture = TestBed.createComponent(MonthlyKpiCardsComponent);
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

  it('renders four KPI cards when data is provided', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.rounded-xl');
    expect(cards.length).toBe(4);
  });

  it('displays income value from data', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('3,000.00');
  });

  it('displays savings rate with percent sign', () => {
    fixture.componentRef.setInput('data', mockData);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('%');
  });
});

