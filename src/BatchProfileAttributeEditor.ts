const RNBatch = require('./NativeRNBatchModule').default;

/**
 * Enum defining the state of an email subscription
 */
export enum BatchEmailSubscriptionState {
  SUBSCRIBED = 'SUBSCRIBED',
  UNSUBSCRIBED = 'UNSUBSCRIBED',
}

/**
 * Enum defining the state of an SMS subscription
 */
export enum BatchSMSSubscriptionState {
  SUBSCRIBED = 'SUBSCRIBED',
  UNSUBSCRIBED = 'UNSUBSCRIBED',
}

interface IUserSettingsSetAttributeAction {
  type: 'setAttribute';
  key: string;
  value: string | boolean | number | null | string[];
}

interface IUserSettingsRemoveAttributeAction {
  type: 'removeAttribute';
  key: string;
}

interface IUserSettingsSetDateAttributeAction {
  type: 'setDateAttribute';
  key: string;
  value: number;
}

interface IUserSettingsSetURLAttributeAction {
  type: 'setURLAttribute';
  key: string;
  value: string;
}

interface IUserSettingsSetLanguageAction {
  type: 'setLanguage';
  value?: string;
}

interface IUserSettingsSetRegionAction {
  type: 'setRegion';
  value?: string;
}

interface IUserSettingsSetEmailAddressAction {
  type: 'setEmailAddress';
  value: string | null;
}

interface IUserSettingsSetEmailMarketingSubscriptionAction {
  type: 'setEmailMarketingSubscription';
  value: BatchEmailSubscriptionState;
}

interface IUserSettingsSetPhoneNumberAction {
  type: 'setPhoneNumber';
  value: string | null;
}

interface IUserSettingsSetSMSMarketingSubscriptionAction {
  type: 'setSMSMarketingSubscription';
  value: BatchSMSSubscriptionState;
}

interface IUserSettingsAddToArrayAction {
  type: 'addToArray';
  key: string;
  value: string | string[];
}

interface IUserSettingsRemoveFromArrayAction {
  type: 'removeFromArray';
  key: string;
  value: string | string[];
}

type IUserSettingsAction =
  | IUserSettingsSetAttributeAction
  | IUserSettingsRemoveAttributeAction
  | IUserSettingsSetDateAttributeAction
  | IUserSettingsSetURLAttributeAction
  | IUserSettingsSetLanguageAction
  | IUserSettingsSetRegionAction
  | IUserSettingsAddToArrayAction
  | IUserSettingsRemoveFromArrayAction
  | IUserSettingsSetEmailAddressAction
  | IUserSettingsSetEmailMarketingSubscriptionAction
  | IUserSettingsSetPhoneNumberAction
  | IUserSettingsSetSMSMarketingSubscriptionAction;

type IUserSettingsActions = IUserSettingsAction[];

/**
 * Editor class used to create and save profile attributes
 */
export class BatchProfileAttributeEditor {
  private _settings: IUserSettingsActions;

  public constructor(settings: IUserSettingsActions = []) {
    this._settings = settings;
  }

  private addAction(action: IUserSettingsAction): BatchProfileAttributeEditor {
    this._settings.push(action);
    return this;
  }

  /**
   * Set an attribute for a key
   * @param key Attribute key. Cannot be null, empty or undefined. It should be made of letters, numbers or underscores
   * ([a-z0-9_]) and can't be longer than 30 characters.
   * @param value Attribute value. Accepted types are strings, numbers, booleans and array of strings.
   */
  public setAttribute(key: string, value: string | boolean | number | string[] | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setAttribute',
      key,
      value,
    });
  }

  /**
   * Set a Date attribute for a key
   *
   * @param key Attribute key. Cannot be null, empty or undefined. It should be made of letters, numbers or underscores
   * ([a-z0-9_]) and can't be longer than 30 characters.
   * @param value The date value
   *
   * Example:
   * ```js
   *  // Set a date attribute with a timestamp
   *  BatchProfile.editor().setDateAttribute("birthday", new Date('July 20, 69 00:20:18 GMT+00:00').getTime())
   * ```
   */
  public setDateAttribute(key: string, value: number): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setDateAttribute',
      key,
      value,
    });
  }

  /**
   * Set an URL attribute for a key
   *
   * @param key Attribute key. Cannot be null, empty or undefined. It should be made of letters, numbers or underscores
   * ([a-z0-9_]) and can't be longer than 30 characters.
   * @param value The URL value
   *
   * Example:
   * ```js
   *  // set an url attribute
   *  BatchProfile.editor().setURLAttribute('website', 'https://example.com')
   * ```
   */
  public setURLAttribute(key: string, value: string): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setURLAttribute',
      key,
      value,
    });
  }

  /**
   * Remove an attribute
   * @param key The key of the attribute to remove
   */
  public removeAttribute(key: string): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'removeAttribute',
      key,
    });
  }

  /**
   * Set the profile email address.
   *
   * This requires to have a custom user ID registered
   * or to call the `setIdentifier` method on the editor instance beforehand.
   * @param value A valid email address. Null to erase.
   */
  public setEmailAddress(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setEmailAddress',
      value,
    });
  }

  /**
   * Set the profile email marketing subscription state
   *
   * @param value The state of the marketing email subscription. Must be "subscribed" or "unsubscribed".
   */
  public setEmailMarketingSubscription(value: BatchEmailSubscriptionState): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setEmailMarketingSubscription',
      value,
    });
  }

  /**
   * Set the profile phone number.
   *
   * This requires to have a custom profile ID registered or to call the `identify` method beforehand.
   * @param value  A valid E.164 formatted string. Must start with a `+` and not be longer than 15 digits
   * without special characters (eg: "+33123456789"). Null to reset.
   */
  public setPhoneNumber(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setPhoneNumber',
      value,
    });
  }

  /**
   * Set the profile SMS marketing subscription state.
   *
   * Note that profile's subscription status is automatically set to unsubscribed when users send a STOP message.
   * @param value The state of the SMS marketing subscription. Must be "subscribed" or "unsubscribed".
   */
  public setSMSMarketingSubscription(value: BatchSMSSubscriptionState): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setSMSMarketingSubscription',
      value,
    });
  }

  /**
   * Set the application language. Overrides Batch's automatically detected language.
   *
   * Send null to let Batch autodetect it again.
   * @param value Language code. 2 chars minimum, or null
   */
  public setLanguage(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setLanguage',
      value,
    });
  }

  /**
   * Set the application region. Overrides Batch's automatically detected region.
   *
   * Send "null" to let Batch autodetect it again.
   * @param value Region code. 2 chars minimum, or null
   */
  public setRegion(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setRegion',
      value,
    });
  }

  /**
   * Add value to an array attribute. If the array doesn't exist it will be created.
   *
   * @param key Attribute key. Cannot be null, empty or undefined. It should be made of letters, numbers or underscores
   * ([a-z0-9_]) and can't be longer than 30 characters.
   * @param value The value to add. Cannot be null, undefined or empty. Must be an array of string or a string no longer
   * than 64 characters.
   */
  public addToArray(key: string, value: string | string[]): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'addToArray',
      key,
      value,
    });
  }

  /**
   * Remove a value from an array attribute.
   *
   * @param key Attribute key. Cannot be null, empty or undefined. It should be made of letters, numbers or underscores
   * ([a-z0-9_]) and can't be longer than 30 characters.
   * @param value The value to remove. Can be a String or an Array of String. Cannot be null, empty or undefined.
   */
  public removeFromArray(key: string, value: string | string[]): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'removeFromArray',
      key,
      value,
    });
  }

  /**
   * Save all the pending changes made in that editor. This action cannot be undone.
   */
  public save(): void {
    RNBatch.profile_saveEditor(this._settings);
  }
}
