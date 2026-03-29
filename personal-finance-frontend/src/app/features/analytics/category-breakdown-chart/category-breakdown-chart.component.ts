import { Component, computed, input } from '@angular/core';
import { ChartConfiguration } from 'chart.js';
import { CategoryAnalyticsResponse } from '../../../core/models/analytics.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { ChartWrapperComponent } from '../../../shared/components/chart-wrapper/chart-wrapper.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

const CHART_COLORS = [
  'rgba(99,102,241,0.85)',
  'rgba(236,72,153,0.85)',
  'rgba(16,185,129,0.85)',
  'rgba(245,158,11,0.85)',
  'rgba(239,68,68,0.85)',
  'rgba(59,130,246,0.85)',
  'rgba(168,85,247,0.85)',
  'rgba(20,184,166,0.85)',
  'rgba(234,88,12,0.85)',
  'rgba(132,204,22,0.85)',
];

@Component({
  selector: 'app-category-breakdown-chart',
  imports: [ChartWrapperComponent, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-8"><app-spinner size="lg" /></div>
    } @else if (data().length === 0) {
      <app-empty-state
        title="No category data"
        description="There are no transactions for the selected period."
        icon="📊"
      />
    } @else {
      <app-chart-wrapper [config]="chartConfig()" />
    }
  `,
})
export class CategoryBreakdownChartComponent {
  readonly data = input<CategoryAnalyticsResponse[]>([]);
  readonly categories = input<CategoryResponse[]>([]);
  readonly loading = input(false);

  readonly chartConfig = computed((): ChartConfiguration => {
    const categoryMap = new Map(this.categories().map((c) => [c.id, c.name]));
    const sorted = [...this.data()].sort((a, b) => b.totalAmount - a.totalAmount);
    const labels = sorted.map((d) => categoryMap.get(d.categoryId) ?? d.categoryId);
    const amounts = sorted.map((d) => d.totalAmount);

    return {
      type: 'doughnut',
      data: {
        labels,
        datasets: [
          {
            data: amounts,
            backgroundColor: sorted.map((_, i) => CHART_COLORS[i % CHART_COLORS.length]),
            borderWidth: 2,
            borderColor: 'rgba(255,255,255,0.1)',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              boxWidth: 12,
              padding: 16,
              font: { size: 12 },
            },
          },
        },
      },
    };
  });
}

