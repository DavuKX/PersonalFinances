import { TestBed } from '@angular/core/testing';
import { ComponentFixture } from '@angular/core/testing';
import { ChartWrapperComponent } from './chart-wrapper.component';
import type { ChartConfiguration } from 'chart.js';

const mockConfig: ChartConfiguration = {
  type: 'line',
  data: { labels: ['Jan', 'Feb'], datasets: [{ data: [10, 20], label: 'Test' }] },
};

describe('ChartWrapperComponent', () => {
  let fixture: ComponentFixture<ChartWrapperComponent>;
  let component: ChartWrapperComponent;

  beforeEach(() => {
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(
      new Proxy({}, { get: () => vi.fn() }) as any,
    );
    TestBed.configureTestingModule({});
    fixture = TestBed.createComponent(ChartWrapperComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('config', mockConfig);
    fixture.detectChanges();
  });

  afterEach(() => vi.restoreAllMocks());

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('destroys chart on component destroy', () => {
    const destroySpy = vi.fn();
    component['chart'] = { destroy: destroySpy } as any;
    component.ngOnDestroy();
    expect(destroySpy).toHaveBeenCalled();
    expect(component['chart']).toBeNull();
  });

  it('renders a canvas element', () => {
    const canvas = fixture.nativeElement.querySelector('canvas');
    expect(canvas).toBeTruthy();
  });
});
