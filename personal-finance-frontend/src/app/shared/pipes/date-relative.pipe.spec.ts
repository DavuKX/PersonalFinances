import { DateRelativePipe } from './date-relative.pipe';

describe('DateRelativePipe', () => {
  const pipe = new DateRelativePipe();

  const ago = (ms: number) => new Date(Date.now() - ms);

  it('returns "just now" for < 60 seconds', () => {
    expect(pipe.transform(ago(30_000))).toBe('just now');
  });

  it('returns minutes ago', () => {
    expect(pipe.transform(ago(5 * 60 * 1000))).toBe('5m ago');
  });

  it('returns hours ago', () => {
    expect(pipe.transform(ago(3 * 60 * 60 * 1000))).toBe('3h ago');
  });

  it('returns days ago', () => {
    expect(pipe.transform(ago(10 * 24 * 60 * 60 * 1000))).toBe('10d ago');
  });

  it('returns months ago', () => {
    expect(pipe.transform(ago(45 * 24 * 60 * 60 * 1000))).toBe('1mo ago');
  });

  it('returns years ago', () => {
    expect(pipe.transform(ago(400 * 24 * 60 * 60 * 1000))).toBe('1y ago');
  });

  it('accepts ISO string input', () => {
    const result = pipe.transform(ago(90_000).toISOString());
    expect(result).toBe('1m ago');
  });
});

