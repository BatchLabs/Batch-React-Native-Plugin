import { ConfigPlugin, withInfoPlist, InfoPlist } from '@expo/config-plugins';

import {
  BATCH_DO_NOT_DISTURB_INITIAL_STATE,
  BATCH_DEFAULT_OPT_OUT_INITIAL_STATE,
  BATCH_DEFAULT_PROFILE_CUSTOM_ID_MIGRATION,
  BATCH_DEFAULT_PROFILE_CUSTOM_DATA_MIGRATION,
} from '../constants';
import { Props } from '../withReactNativeBatch';

export const modifyInfoPlist = (infoPlist: InfoPlist, props: Props): InfoPlist => {
  infoPlist.BatchAPIKey = props.iosApiKey;
  infoPlist.BatchDoNotDisturbInitialState =
    props.enableDoNotDisturb !== undefined ? props.enableDoNotDisturb : BATCH_DO_NOT_DISTURB_INITIAL_STATE;
  infoPlist.BatchProfileCustomIdMigrationEnabled =
    props.enableProfileCustomIDMigration !== undefined ? props.enableProfileCustomIDMigration : BATCH_DEFAULT_PROFILE_CUSTOM_ID_MIGRATION;
  infoPlist.BatchProfileCustomDataMigrationEnabled =
    props.enableProfileCustomDataMigration !== undefined
      ? props.enableProfileCustomDataMigration
      : BATCH_DEFAULT_PROFILE_CUSTOM_DATA_MIGRATION;
  infoPlist.BATCH_OPTED_OUT_BY_DEFAULT =
    props.enableDefaultOptOut !== undefined ? props.enableDefaultOptOut : BATCH_DEFAULT_OPT_OUT_INITIAL_STATE;
  return infoPlist;
};

export const withReactNativeBatchInfoPlist: ConfigPlugin<Props> = (config, props) => {
  return withInfoPlist(config, config => {
    config.modResults = modifyInfoPlist(config.modResults, props);
    return config;
  });
};
