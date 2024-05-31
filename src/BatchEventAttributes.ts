import { Log } from './helpers/Logger';
import { isBoolean, isNumber, isObject, isObjectArray, isString, isStringArray } from './helpers/TypeHelpers';
export enum TypedEventAttributeType {
  String = 'string',
  Boolean = 'boolean',
  Integer = 'integer',
  Float = 'float',
  Date = 'date',
  URL = 'url',
  StringArray = 'string_array',
  ObjectArray = 'object_array',
  Object = 'object',
}

export type TypedEventAttributeValue = string | boolean | number | string[] | TypedEventAttributes | TypedEventAttributes[];

export type TypedEventAttributes = { [key: string]: ITypedEventAttribute };

export interface ITypedEventAttribute {
  type: TypedEventAttributeType;
  value: TypedEventAttributeValue;
}

export class BatchEventAttributes {
  private readonly _attributes: { [key: string]: ITypedEventAttribute }; // tslint:disable-line

  public constructor() {
    this._attributes = {};
  }
  private prepareAttributeKey(key: string): string {
    return key.toLowerCase();
  }

  public putDate(key: string, value: number): BatchEventAttributes {
    key = this.prepareAttributeKey(key);

    this._attributes[key] = {
      type: TypedEventAttributeType.Date,
      value,
    };

    return this;
  }

  public putURL(key: string, url: string): BatchEventAttributes {
    key = this.prepareAttributeKey(key);

    this._attributes[key] = {
      type: TypedEventAttributeType.URL,
      value: url,
    };

    return this;
  }

  public put(
    key: string,
    value: string | number | boolean | string[] | BatchEventAttributes | BatchEventAttributes[]
  ): BatchEventAttributes {
    key = this.prepareAttributeKey(key);

    let typedAttrValue: ITypedEventAttribute | undefined;
    if (isString(value)) {
      typedAttrValue = {
        type: TypedEventAttributeType.String,
        value,
      };
    } else if (isNumber(value)) {
      typedAttrValue = {
        type: value % 1 === 0 ? TypedEventAttributeType.Integer : TypedEventAttributeType.Float,
        value,
      };
    } else if (isBoolean(value)) {
      typedAttrValue = {
        type: TypedEventAttributeType.Boolean,
        value,
      };
    } else if (isObject(value)) {
      typedAttrValue = {
        type: TypedEventAttributeType.Object,
        value: value._attributes,
      };
    } else if (isStringArray(value)) {
      typedAttrValue = {
        type: TypedEventAttributeType.StringArray,
        value,
      };
    } else if (isObjectArray(value)) {
      const array = [];
      value.forEach(item => {
        array.push(item._attributes);
      });
      typedAttrValue = {
        type: TypedEventAttributeType.ObjectArray,
        value: array,
      };
    } else {
      Log(false, 'BatchEventAttributes - Invalid attribute value type. Must be a string, number or boolean');
      return this;
    }

    if (typedAttrValue) {
      this._attributes[key] = typedAttrValue;
    }
    return this;
  }
  protected _toInternalRepresentation(): unknown {
    return this._attributes;
  }
}
