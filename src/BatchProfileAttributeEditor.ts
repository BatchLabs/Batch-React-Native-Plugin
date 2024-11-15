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
 * Editor class used to create and save user tags and attributes
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

  public setAttribute(key: string, value: string | boolean | number | string[] | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setAttribute',
      key,
      value,
    });
  }

  public setDateAttribute(key: string, value: number): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setDateAttribute',
      key,
      value,
    });
  }

  public setURLAttribute(key: string, value: string): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setURLAttribute',
      key,
      value,
    });
  }

  public removeAttribute(key: string): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'removeAttribute',
      key,
    });
  }

  public setEmailAddress(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setEmailAddress',
      value,
    });
  }

  public setEmailMarketingSubscription(value: BatchEmailSubscriptionState): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setEmailMarketingSubscription',
      value,
    });
  }

  public setPhoneNumber(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setPhoneNumber',
      value,
    });
  }

  public setSMSMarketingSubscription(value: BatchSMSSubscriptionState): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setSMSMarketingSubscription',
      value,
    });
  }

  public setLanguage(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setLanguage',
      value,
    });
  }

  public setRegion(value: string | null): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'setRegion',
      value,
    });
  }

  public addToArray(key: string, value: string | string[]): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'addToArray',
      key,
      value,
    });
  }

  public removeFromArray(key: string, value: string | string[]): BatchProfileAttributeEditor {
    return this.addAction({
      type: 'removeFromArray',
      key,
      value,
    });
  }

  public save(): void {
    RNBatch.profile_saveEditor(this._settings);
  }
}
