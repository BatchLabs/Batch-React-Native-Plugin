import { ConfigPlugin, createRunOncePlugin } from '@expo/config-plugins';
import { withApplyPlugin, withClassPath, withGoogleServicesFile } from '@expo/config-plugins/build/android/GoogleServices';

import { withReactNativeBatchAppBuildGradle } from './android/withReactNativeBatchAppBuildGradle';
import { withReactNativeBatchMainActivity } from './android/withReactNativeBatchMainActivity';
import { withReactNativeBatchMainApplication } from './android/withReactNativeBatchMainApplication';
import { withReactNativeBatchManifest } from './android/withReactNativeBatchManifest';
import { withReactNativeBatchAppDelegate } from './ios/withReactNativeBatchAppDelegate';
import { withReactNativeBatchEntitlements } from './ios/withReactNativeBatchEntitlements';
import { withReactNativeBatchInfoPlist } from './ios/withReactNativeBatchInfoPlist';

export type Props = {
  androidApiKey: string;
  iosApiKey: string;
  enableDoNotDisturb?: boolean;
  enableDefaultOptOut?: boolean;
  enableProfileCustomIDMigration?: boolean;
  enableProfileCustomDataMigration?: boolean;
};
/**
 * Apply react-native-batch configuration for Expo SDK 42 projects.
 */
const withReactNativeBatch: ConfigPlugin<Props | void> = (config, props) => {
  const _props = props || { androidApiKey: '', iosApiKey: '' };

  let newConfig = withGoogleServicesFile(config);
  newConfig = withClassPath(newConfig);
  newConfig = withApplyPlugin(newConfig);
  newConfig = withReactNativeBatchManifest(newConfig, _props);
  newConfig = withReactNativeBatchAppBuildGradle(newConfig, _props);
  newConfig = withReactNativeBatchMainApplication(newConfig);
  newConfig = withReactNativeBatchMainActivity(newConfig);
  newConfig = withReactNativeBatchInfoPlist(newConfig, _props);
  newConfig = withReactNativeBatchEntitlements(newConfig);
  newConfig = withReactNativeBatchAppDelegate(newConfig);
  // Return the modified config.
  return newConfig;
};

const pkg = {
  // Prevent this plugin from being run more than once.
  // This pattern enables users to safely migrate off of this
  // out-of-tree `@config-plugins/react-native-batch` to a future
  // upstream plugin in `react-native-batch`
  name: '@batch.com/react-native-plugin',
  // Indicates that this plugin is dangerously linked to a module,
  // and might not work with the latest version of that module.
  version: 'UNVERSIONED',
};

export default createRunOncePlugin(withReactNativeBatch, pkg.name, pkg.version);
