import { TruncatePipe } from './truncate.pipe';

describe('TruncatePipe', () => {
  const pipe = new TruncatePipe();

  it('returns the original string when within limit', () => {
    expect(pipe.transform('hello', 10)).toBe('hello');
  });

  it('truncates to the limit and appends trail', () => {
    expect(pipe.transform('hello world', 5)).toBe('hello…');
  });

  it('uses default limit of 50', () => {
    const long = 'a'.repeat(60);
    const result = pipe.transform(long);
    expect(result.length).toBe(51);
    expect(result.endsWith('…')).toBe(true);
  });

  it('uses custom trail', () => {
    expect(pipe.transform('hello world', 5, '...')).toBe('hello...');
  });

  it('returns string unchanged when exactly at limit', () => {
    expect(pipe.transform('hello', 5)).toBe('hello');
  });
});

