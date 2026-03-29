import { CurrencyFormatPipe } from './currency-format.pipe';

describe('CurrencyFormatPipe', () => {
  const pipe = new CurrencyFormatPipe();

  it('formats USD by default', () => {
    expect(pipe.transform(1234.5)).toBe('$1,234.50');
  });

  it('formats EUR currency', () => {
    const result = pipe.transform(500, 'EUR', 'de-DE');
    expect(result).toContain('500');
    expect(result).toContain('€');
  });

  it('formats zero correctly', () => {
    expect(pipe.transform(0)).toBe('$0.00');
  });

  it('formats negative values', () => {
    expect(pipe.transform(-99.99)).toBe('-$99.99');
  });
});

