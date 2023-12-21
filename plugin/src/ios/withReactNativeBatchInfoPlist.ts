import { ConfigPlugin, withInfoPlist, InfoPlist } from '@expo/config-plugins';

import { BATCH_DO_NOT_DISTURB_INITIAL_STATE } from '../constants';
import { Props } from '../withReactNativeBatch';

export const modifyInfoPlist = (infoPlist: InfoPlist, props: Props): InfoPlist => {
  infoPlist.BatchAPIKey = props.iosApiKey;
  infoPlist.BatchDoNotDisturbInitialState = props.enableDoNotDisturb || props.enableDoNotDistrub || BATCH_DO_NOT_DISTURB_INITIAL_STATE;
  return infoPlist;
};

export const withReactNativeBatchInfoPlist: ConfigPlugin<Props> = (config, props) => {
  return withInfoPlist(config, config => {
    config.modResults = modifyInfoPlist(config.modResults, props);
    return config;
  });
};
