import { BatchEventAttributes } from '../BatchEventAttributes';

export function isString(value: unknown): value is string {
  return value instanceof String || typeof value === 'string';
}

export function isNumber(value: unknown): value is number {
  return value instanceof Number || (typeof value === 'number' && !isNaN(value));
}

export function isBoolean(value: unknown): value is boolean {
  return value instanceof Boolean || typeof value === 'boolean';
}

export const isObject = (value: unknown): value is BatchEventAttributes => {
  return value instanceof BatchEventAttributes;
};

export function isArray(value: unknown): value is Array<unknown> {
  return Array.isArray(value);
}

export function isStringArray(value: unknown): value is Array<string> {
  return isArray(value) && value.every(it => isString(it));
}

export function isObjectArray(value: unknown): value is Array<BatchEventAttributes> {
  return isArray(value) && value.every(it => isObject(it));
}
