import { Log } from './helpers/Logger';
import { isBoolean, isNumber, isObject, isObjectArray, isString, isStringArray } from './helpers/TypeHelpers';

export const Consts = {
  AttributeKeyRegexp: /^[a-zA-Z0-9_]{1,30}$/,
  EventDataMaxTags: 10,
  EventDataMaxValues: 15,
  EventDataStringMaxLength: 64,
  EventDataURLMaxLength: 2048,
};

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

export type TypedEventAttributeValue = string | boolean | number | TypedEventAttributes | Array<string | TypedEventAttributes>;

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

  private addTags(tags: Array<string>): BatchEventAttributes {
    tags.forEach(tag => {
      if (typeof tag === 'undefined') {
        Log(false, 'BatchEventData - A tag is required');
        return this;
      }

      if (isString(tag)) {
        if (tag.length === 0 || tag.length > Consts.EventDataStringMaxLength) {
          Log(
            false,
            "BatchEventData - Tags can't be empty or longer than " +
              Consts.EventDataStringMaxLength +
              " characters. Ignoring tag '" +
              tag +
              "'."
          );
          return this;
        }
      } else {
        Log(false, 'BatchEventData - Tag argument must be a string');
        return this;
      }
      if (tags.length >= Consts.EventDataMaxTags) {
        Log(false, 'BatchEventData - Event data cannot hold more than ' + Consts.EventDataMaxTags + " tags. Ignoring tag: '" + tag + "'");
        return this;
      }
    });
    this._attributes['$tags'] = {
      type: TypedEventAttributeType.StringArray,
      value: tags,
    };
    return this;
  }

  private checkBeforePuttingAttribute(key: string, value: unknown): void {
    if (!isString(key)) {
      Log(false, 'BatchEventData - Key must be a string');
      throw new Error();
    }

    if (!Consts.AttributeKeyRegexp.test(key || '')) {
      if (key !== '$tags' && key !== '$label') {
        Log(
          false,
          'BatchEventData - Invalid key. Please make sure that the key is made of letters, underscores and numbers only (a-zA-Z0-9_).' +
            "It also can't be longer than 30 characters. Ignoring attribute '" +
            key +
            "'"
        );
        throw new Error();
      }
    }

    if (typeof value === 'undefined' || value === null) {
      Log(false, 'BatchEventData - Value cannot be undefined or null');
      throw new Error();
    }

    if (Object.keys(this._attributes).length >= Consts.EventDataMaxValues && !Object.prototype.hasOwnProperty.call(this._attributes, key)) {
      Log(
        false,
        'BatchEventData - Event data cannot hold more than ' + Consts.EventDataMaxValues + " attributes. Ignoring attribute: '" + key + "'"
      );
      throw new Error();
    }
  }

  private prepareAttributeKey(key: string): string {
    return key.toLowerCase();
  }

  public putDate(key: string, value: number): BatchEventAttributes {
    key = this.prepareAttributeKey(key);
    try {
      this.checkBeforePuttingAttribute(key, value);
    } catch {
      return this;
    }

    this._attributes[key] = {
      type: TypedEventAttributeType.Date,
      value,
    };

    return this;
  }

  public putURL(key: string, url: string): BatchEventAttributes {
    key = this.prepareAttributeKey(key);
    try {
      this.checkBeforePuttingAttribute(key, url);
    } catch {
      return this;
    }

    if (url.length > Consts.EventDataURLMaxLength) {
      Log(
        false,
        "BatchEventData - Event data can't be longer than " +
          Consts.EventDataURLMaxLength +
          " characters. Ignoring event data value '" +
          url +
          "'."
      );
      return this;
    }

    this._attributes[key] = {
      type: TypedEventAttributeType.URL,
      value: url,
    };

    return this;
  }

  public put(
    key: string,
    value: string | number | boolean | Array<string | BatchEventAttributes> | BatchEventAttributes
  ): BatchEventAttributes {
    key = this.prepareAttributeKey(key);

    try {
      this.checkBeforePuttingAttribute(key, value);
    } catch {
      return this;
    }

    // Check if data contains legacy tags
    if (key == '$tags' && isStringArray(value)) {
      this.addTags(value);
      return this;
    }

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
      Log(false, 'BatchEventData - Invalid attribute value type. Must be a string, number or boolean');
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
