export function isString(value: unknown): value is string {
  return value instanceof String || typeof value === 'string';
}

export function isNumber(value: unknown): value is number {
  return value instanceof Number || (typeof value === 'number' && !isNaN(value));
}

export function isBoolean(value: unknown): value is boolean {
  return value instanceof Boolean || typeof value === 'boolean';
}
