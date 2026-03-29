import {
  formatMonthYear,
  toISODateString,
  startOfMonth,
  endOfMonth,
  monthsBack,
} from './date-helpers';

describe('date-helpers', () => {
  describe('formatMonthYear', () => {
    it('formats January 2025', () => {
      expect(formatMonthYear(2025, 1)).toBe('January 2025');
    });

    it('formats December 2024', () => {
      expect(formatMonthYear(2024, 12)).toBe('December 2024');
    });
  });

  describe('toISODateString', () => {
    it('returns YYYY-MM-DD format', () => {
      expect(toISODateString(new Date('2025-03-15T10:00:00Z'))).toBe('2025-03-15');
    });
  });

  describe('startOfMonth', () => {
    it('returns first day of month', () => {
      const d = startOfMonth(2025, 3);
      expect(d.getDate()).toBe(1);
      expect(d.getMonth()).toBe(2);
    });
  });

  describe('endOfMonth', () => {
    it('returns last day of March', () => {
      expect(endOfMonth(2025, 3).getDate()).toBe(31);
    });

    it('returns last day of February (non-leap)', () => {
      expect(endOfMonth(2025, 2).getDate()).toBe(28);
    });
  });

  describe('monthsBack', () => {
    it('returns correct count', () => {
      expect(monthsBack(6, new Date(2025, 5, 1)).length).toBe(6);
    });

    it('returns months in ascending order', () => {
      const months = monthsBack(3, new Date(2025, 2, 1));
      expect(months[0]).toEqual({ year: 2025, month: 1 });
      expect(months[2]).toEqual({ year: 2025, month: 3 });
    });
  });
});



