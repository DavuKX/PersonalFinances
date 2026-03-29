import {
  afterNextRender,
  Component,
  effect,
  ElementRef,
  input,
  OnDestroy,
  viewChild,
} from '@angular/core';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-chart-wrapper',
  template: `<canvas #canvas class="block w-full h-full"></canvas>`,
})
export class ChartWrapperComponent implements OnDestroy {
  readonly config = input.required<ChartConfiguration>();

  private readonly canvasRef = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart: Chart | null = null;

  constructor() {
    afterNextRender(() => {
      this.chart = new Chart(this.canvasRef().nativeElement, this.config());
    });

    effect(() => {
      const cfg = this.config();
      if (!this.chart) return;
      this.chart.data = cfg.data;
      this.chart.update('none');
    });
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
    this.chart = null;
  }
}

