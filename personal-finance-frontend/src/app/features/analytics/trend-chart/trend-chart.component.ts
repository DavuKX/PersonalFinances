import { Component, computed, input } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { TrendResponse } from '../../../core/models/analytics.models';
import { ChartWrapperComponent } from '../../../shared/components/chart-wrapper/chart-wrapper.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

@Component({
  selector: 'app-trend-chart',
  imports: [ChartWrapperComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-8"><app-spinner size="lg" /></div>
    } @else if (data().length === 0) {
      <app-empty-state
        title="No trend data"
        description="Not enough transactions to show trends."
        icon="📈"
      />
    } @else {
      <app-chart-wrapper [config]="chartConfig()" />
    }
  `,
})
export class TrendChartComponent {
  readonly data = input<TrendResponse[]>([]);
  readonly loading = input(false);
  readonly mini = input(false);

  readonly chartConfig = computed((): ChartConfiguration => {
    const sorted = [...this.data()].sort((a, b) => a.year - b.year || a.month - b.month);
    const labels = sorted.map((d) => `${MONTH_NAMES[d.month - 1]} ${d.year}`);

    return {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Income',
            data: sorted.map((d) => d.totalIncome),
            borderColor: 'rgba(16,185,129,1)',
            backgroundColor: 'rgba(16,185,129,0.1)',
            fill: true,
            tension: 0.3,
            pointRadius: this.mini() ? 0 : 3,
          },
          {
            label: 'Expenses',
            data: sorted.map((d) => d.totalExpenses),
            borderColor: 'rgba(244,63,94,1)',
            backgroundColor: 'rgba(244,63,94,0.1)',
            fill: true,
            tension: 0.3,
            pointRadius: this.mini() ? 0 : 3,
          },
          {
            label: 'Net Savings',
            data: sorted.map((d) => d.netSavings),
            borderColor: 'rgba(99,102,241,1)',
            backgroundColor: 'rgba(99,102,241,0.1)',
            fill: true,
            tension: 0.3,
            pointRadius: this.mini() ? 0 : 3,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: !this.mini() },
        },
        scales: {
          x: { display: !this.mini() },
          y: { display: !this.mini() },
        },
      },
    };
  });
}

