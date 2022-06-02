import { NativeModules } from 'react-native';
const RNBatch = NativeModules.RNBatch;

interface IUserSettingsSetAttributeAction {
  type: 'setAttribute';
  key: string;
  value: string | boolean | number | null;
}

interface IUserSettingsRemoveAttributeAction {
  type: 'removeAttribute';
  key: string;
}

interface IUserSettingsClearAttributesAction {
  type: 'clearAttributes';
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

interface IUserSettingsSetIdentifierAction {
  type: 'setIdentifier';
  value: string | null;
}

interface IUserSettingsAddTagAction {
  type: 'addTag';
  collection: string;
  tag: string;
}

interface IUserSettingsRemoveTagAction {
  type: 'removeTag';
  collection: string;
  tag: string;
}

interface IUserSettingsClearTagCollectionAction {
  type: 'clearTagCollection';
  collection: string;
}

interface IUserSettingsClearTagsAction {
  type: 'clearTags';
}

type IUserSettingsAction =
  | IUserSettingsSetAttributeAction
  | IUserSettingsRemoveAttributeAction
  | IUserSettingsClearAttributesAction
  | IUserSettingsSetDateAttributeAction
  | IUserSettingsSetURLAttributeAction
  | IUserSettingsSetIdentifierAction
  | IUserSettingsSetLanguageAction
  | IUserSettingsSetRegionAction
  | IUserSettingsAddTagAction
  | IUserSettingsRemoveTagAction
  | IUserSettingsClearTagsAction
  | IUserSettingsClearTagCollectionAction;

type IUserSettingsActions = IUserSettingsAction[];

/**
 * Editor class used to create and save user tags and attributes
 */
export class BatchUserEditor {
  private _settings: IUserSettingsActions;

  public constructor(settings: IUserSettingsActions = []) {
    this._settings = settings;
  }

  private addAction(action: IUserSettingsAction): BatchUserEditor {
    this._settings.push(action);
    return this;
  }

  public setAttribute(key: string, value: string | boolean | number | null): BatchUserEditor {
    return this.addAction({
      type: 'setAttribute',
      key,
      value,
    });
  }

  public setDateAttribute(key: string, value: number): BatchUserEditor {
    return this.addAction({
      type: 'setDateAttribute',
      key,
      value,
    });
  }

  public setURLAttribute(key: string, value: string): BatchUserEditor {
    return this.addAction({
      type: 'setURLAttribute',
      key,
      value,
    });
  }

  public removeAttribute(key: string): BatchUserEditor {
    return this.addAction({
      type: 'removeAttribute',
      key,
    });
  }

  public clearAttributes(): BatchUserEditor {
    return this.addAction({
      type: 'clearAttributes',
    });
  }

  public setIdentifier(value: string | null): BatchUserEditor {
    return this.addAction({
      type: 'setIdentifier',
      value,
    });
  }

  public setLanguage(value?: string): BatchUserEditor {
    return this.addAction({
      type: 'setLanguage',
      value,
    });
  }

  public setRegion(value?: string): BatchUserEditor {
    return this.addAction({
      type: 'setRegion',
      value,
    });
  }

  public addTag(collection: string, tag: string): BatchUserEditor {
    return this.addAction({
      type: 'addTag',
      collection,
      tag,
    });
  }

  public removeTag(collection: string, tag: string): BatchUserEditor {
    return this.addAction({
      type: 'removeTag',
      collection,
      tag,
    });
  }

  public clearTagCollection(collection: string): BatchUserEditor {
    return this.addAction({
      type: 'clearTagCollection',
      collection,
    });
  }

  public clearTags(): BatchUserEditor {
    return this.addAction({
      type: 'clearTags',
    });
  }

  public save(): void {
    RNBatch.userData_save(this._settings);
  }
}
