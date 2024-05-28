import { NativeModules, Platform } from 'react-native';
export * from './BatchEventAttributes';
export * from './BatchInbox';
export * from './BatchInboxFetcher';
export * from './BatchMessaging';
export * from './BatchPush';
export * from './BatchUser';
export * from './BatchUserAttribute';
export * from './BatchEventEmitter';
export * from './BatchProfile';

const RNBatch = NativeModules.RNBatch;

/**
 * Object holding the configuration parameters for the automatic data collect.
 */
export interface DataCollectionConfig {
  /**
   * Whether Batch can send the device brand information. (Android only)
   * @defaultValue false
   */
  deviceBrand?: boolean;
  /**
   * Whether Batch can send the device model information.
   * @defaultValue false
   */
  deviceModel?: boolean;
  /**
   * Whether Batch can resolve the GeoIP on server side.
   * @defaultValue false
   */
  geoIP?: boolean;
}

/**
 * Batch React-Native Module
 */
export const Batch = {
  /**
   * Opt In to Batch SDK Usage.
   *
   * This method will be taken into account on next full application start (full process restart)
   *
   * Only useful if you called batch.optOut() or batch.optOutAndWipeData() or opted out by default in the manifest
   *
   * Some features might not be disabled until the next app start if you call this late into the application's life. It is strongly
   * advised to restart the application (or at least the current activity) after opting in.
   */
  optIn: (): Promise<void> => RNBatch.optIn(),

  /**
   * Opt Out from Batch SDK Usage
   *
   * Note that calling the SDK when opted out is discouraged: Some modules might behave unexpectedly
   * when the SDK is opted-out from.
   *
   * Opting out will:
   * - Prevent batch.start()
   * - Disable any network capability from the SDK
   * - Disable all In-App campaigns
   * - Make the Inbox module return an error immediately when used
   * - Make the SDK reject any editor calls
   * - Make the SDK reject calls to batch.profile.trackEvent(), batch.profile.trackLocation()
   *   and any related methods
   *
   * Even if you opt in afterwards, data that has been generated while opted out WILL be lost.
   *
   * If you're also looking at deleting user data, please use batch.optOutAndWipeData()
   *
   * Note that calling this method will stop Batch.
   * Your app should be prepared to handle these cases.
   * Some features might not be disabled until the next app start.
   */
  optOut: (): Promise<void> => RNBatch.optOut(),

  /**
   * Opt Out from Batch SDK Usage
   *
   * Same as batch.optOut(Context) but also wipes data.
   *
   * Note that calling this method will stop Batch.
   * Your app should be prepared to handle these cases.
   */
  optOutAndWipeData: (): Promise<void> => RNBatch.optOutAndWipeData(),

  /**
   * Checks whether Batch has been opted out from or not.
   *
   * @returns {Promise<boolean>} A promise that resolves to a boolean value indicating whether Batch has been
   * opted out from or not.
   */
  isOptedOut: (): Promise<boolean> => RNBatch.isOptedOut(),

  /**
   * Configure the SDK Automatic Data Collection.
   *
   * @param {DataCollectionConfig} dataCollection A configuration object to fine-tune the data you authorize to be tracked by Batch.
   * @see {@link DataCollectionConfig} for more info.
   * @example
   * Here's an example:
   * ```
   * Batch.updateAutomaticDataCollection({
   *    geoIP: false, // Deny Batch from resolving the user's region from the ip address.
   *    deviceModel: true // Authorize Batch to use the user's device model information.
   * });
   * ```
   * @remarks Batch will persist the changes, so you can call this method at any time according to user consent.
   */
  updateAutomaticDataCollection: (dataCollection: DataCollectionConfig): void => RNBatch.updateAutomaticDataCollection(dataCollection),

  /**
   * Shows debug view
   *
   * Android: https://doc.batch.com/android/troubleshooting#implementing-batch-debugger
   * iOS: https://doc.batch.com/ios/troubleshooting#implementing-batch-debugger
   */
  showDebugView: (): void => {
    switch (Platform.OS) {
      case 'android':
        RNBatch.debug_startDebugActivity();
        break;
      default:
        RNBatch.presentDebugViewController();
        break;
    }
  },
};
