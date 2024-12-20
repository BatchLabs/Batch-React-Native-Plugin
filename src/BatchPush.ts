import { Linking, Platform } from 'react-native';

import { BatchEventEmitter, EmitterSubscription } from './BatchEventEmitter';

const RNBatch = require('./NativeRNBatchModule').default;

export interface IAndroidNotificationTypes {
  NONE: number;
  SOUND: number;
  VIBRATE: number;
  LIGHTS: number;
  ALERT: number;
}

export interface BatchPushEventPayload {
  isPositiveAction: boolean;
  pushPayload: Record<string, unknown>;
  deeplink?: string | null;
}

export const AndroidNotificationTypes: IAndroidNotificationTypes = RNBatch.getConstants().NOTIFICATION_TYPES;

/**
 * Batch's push module
 */
export const BatchPush = {
  AndroidNotificationTypes,

  /**
   * Ask users if they want to accept push notifications.
   * Required to be able to push users (or use requestProvisionalNotificationAuthorization - ios only).
   *
   */
  requestNotificationAuthorization: (): void => {
    RNBatch.push_requestNotificationAuthorization();
  },

  /**
   * Ask iOS for provisional notifications (no alert to users).
   * Required to be able to push users (or use requestNotificationAuthorization).
   *
   * No effect on Android.
   */
  requestProvisionalNotificationAuthorization: (): void => {
    if (Platform.OS === 'ios') {
      RNBatch.push_requestProvisionalNotificationAuthorization();
    }
  },

  /**
   * Synchronizes the user's iOS token with Batch. Should be called at each app launch.
   *
   * No effect on Android.
   */
  refreshToken: (): void => {
    if (Platform.OS === 'ios') {
      RNBatch.push_refreshToken();
    }
  },

  setShowForegroundNotification: (enabled: boolean): void => {
    if (Platform.OS === 'ios') {
      RNBatch.push_setShowForegroundNotification(enabled);
    }
  },

  /**
   * Change the used remote notification types on Android. (Ex: sound, vibrate, alert)
   *
   * __Does not work with iOS__
   *
   * Example : setAndroidNotificationTypes(batch.push.AndroidNotificationTypes.ALERT | batch.push.AndroidNotificationTypes.SOUND)
   */
  setAndroidNotificationTypes: (notificationTypes: number[]): void => {
    const notificationType = notificationTypes.reduce((sum, value) => sum + value, 0);
    RNBatch.push_setNotificationTypes(notificationType);
  },

  /**
   * Clear the app badge on iOS. No effect on Android.
   *
   */
  clearBadge: (): void => RNBatch.push_clearBadge(),

  /**
   * Dismiss the app's shown notifications on iOS. Should be called on startup.
   *
   * No effect on Android.
   */
  dismissNotifications: (): void => RNBatch.push_dismissNotifications(),

  /**
   * Gets the last known push token.
   * Batch MUST be started in order to use this method.
   *
   * The returned token might be outdated and invalid if this method is called
   * too early in your application lifecycle.
   *
   * On iOS, your application should still register for remote notifications
   * once per launch, in order to keep this value valid.
   */
  getLastKnownPushToken: (): Promise<string> => RNBatch.push_getLastKnownPushToken(),

  /**
   * Gets the app's initial URL.
   *
   * On iOS, make sure to call this only once
   * (only the first call will return something, if Linking.getInitialURL doesn't return anything)
   */
  getInitialURL: async (): Promise<string | null> => {
    const initialURL = await Linking.getInitialURL();
    if (initialURL) {
      return initialURL;
    }

    if (Platform.OS === 'ios') {
      return (await RNBatch.push_getInitialDeeplink()) || null;
    }

    return null;
  },

  /**
   * Listen for push events
   *
   * dismiss and display are only supported on Android (you can still call addListener on those but it's a no-op).
   *
   * Push payload will vary depending on the platform.
   */
  addListener(eventType: 'open' | 'dismiss' | 'display', callback: (payload: BatchPushEventPayload) => void): EmitterSubscription {
    if (Platform.OS === 'ios' && ['dismiss', 'display'].includes(eventType)) {
      // not supported by Batch iOS SDK
      return {
        remove: () => {
          // do nothing
        },
      };
    }

    const subscription = BatchEventEmitter.addListener(`notification_${eventType}`, callback);
    return {
      remove: () => subscription.remove(),
    };
  },
};
