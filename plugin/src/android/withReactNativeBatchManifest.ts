import { ConfigPlugin, AndroidManifest, withAndroidManifest } from '@expo/config-plugins';

import {
  BATCH_DEFAULT_PROFILE_CUSTOM_DATA_MIGRATION,
  BATCH_DEFAULT_PROFILE_CUSTOM_ID_MIGRATION,
  BATCH_DEFAULT_OPT_OUT_INITIAL_STATE,
} from '../constants';
import { Props } from '../withReactNativeBatch';

export const modifyAndroidManifest = (modResults: AndroidManifest, props: Props): AndroidManifest => {
  const profileCustomIdMigrationEnabled =
    props.enableProfileCustomIDMigration !== undefined ? props.enableProfileCustomIDMigration : BATCH_DEFAULT_PROFILE_CUSTOM_ID_MIGRATION;
  const profileCustomDataMigrationEnabled =
    props.enableProfileCustomDataMigration !== undefined
      ? props.enableProfileCustomDataMigration
      : BATCH_DEFAULT_PROFILE_CUSTOM_DATA_MIGRATION;
  const defaultOptedOut = props.enableDefaultOptOut !== undefined ? props.enableDefaultOptOut : BATCH_DEFAULT_OPT_OUT_INITIAL_STATE;
  modResults.manifest?.application?.map(element => {
    if (element['meta-data']) {
      element['meta-data'].push({
        $: {
          'android:name': 'batch.profile_custom_id_migration_enabled',
          'android:value': String(profileCustomIdMigrationEnabled),
        },
      });
      element['meta-data'].push({
        $: {
          'android:name': 'batch.profile_custom_data_migration_enabled',
          'android:value': String(profileCustomDataMigrationEnabled),
        },
      });
      element['meta-data'].push({
        $: {
          'android:name': 'batch_opted_out_by_default',
          'android:value': String(defaultOptedOut),
        },
      });
    }
  });
  return modResults;
};

export const withReactNativeBatchManifest: ConfigPlugin<Props> = (config, props) => {
  return withAndroidManifest(config, config => {
    config.modResults = modifyAndroidManifest(config.modResults, props);
    return config;
  });
};
