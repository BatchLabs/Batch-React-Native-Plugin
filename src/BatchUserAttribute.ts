export enum BatchUserAttributeType {
  STRING = 's',
  BOOLEAN = 'b',
  INTEGER = 'i',
  DOUBLE = 'f',
  DATE = 'd',
  URL = 'u',
}

export class BatchUserAttribute {
  public constructor(
    private _type: BatchUserAttributeType,
    private _value: unknown
  ) {}

  public getType(): BatchUserAttributeType {
    return this._type;
  }

  public getValue(): unknown {
    // Dates are mutable so they should be copied
    if (this._value instanceof Date) {
      return new Date(this._value.getTime());
    }
    return this._value;
  }

  public getStringValue(): string | undefined {
    if (this._type === BatchUserAttributeType.STRING) {
      return this._value as string;
    }
    return undefined;
  }

  public getBooleanValue(): boolean | undefined {
    if (this._type === BatchUserAttributeType.BOOLEAN) {
      return this._value as boolean;
    }
    return undefined;
  }

  public getNumberValue(): number | undefined {
    if (this._type === BatchUserAttributeType.INTEGER || this._type === BatchUserAttributeType.DOUBLE) {
      return this._value as number;
    }
    return undefined;
  }

  public getDateValue(): Date | undefined {
    if (this._type === BatchUserAttributeType.DATE) {
      return new Date(this._value as number);
    }
    return undefined;
  }

  public getURLValue(): URL | undefined {
    if (this._type === BatchUserAttributeType.URL) {
      return new URL(this._value as string);
    }
    return undefined;
  }
}
