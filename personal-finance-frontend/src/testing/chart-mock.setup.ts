import { vi } from 'vitest';

vi.mock('chart.js', () => ({
  Chart: Object.assign(
    vi.fn().mockImplementation(() => ({ destroy: vi.fn(), update: vi.fn(), data: {} })),
    { register: vi.fn() },
  ),
  registerables: [],
}));

