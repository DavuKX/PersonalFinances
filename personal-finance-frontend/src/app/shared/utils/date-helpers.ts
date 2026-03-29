export function formatMonthYear(year: number, month: number): string {
  return new Date(year, month - 1).toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
}

export function toISODateString(date: Date): string {
  return date.toISOString().split('T')[0];
}

export function startOfMonth(year: number, month: number): Date {
  return new Date(year, month - 1, 1);
}

export function endOfMonth(year: number, month: number): Date {
  return new Date(year, month, 0);
}

export function monthsBack(count: number, from: Date = new Date()): { year: number; month: number }[] {
  return Array.from({ length: count }, (_, i) => {
    const d = new Date(from.getFullYear(), from.getMonth() - i, 1);
    return { year: d.getFullYear(), month: d.getMonth() + 1 };
  }).reverse();
}

