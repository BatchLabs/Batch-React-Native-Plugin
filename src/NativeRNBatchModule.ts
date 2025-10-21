import { TurboModule, TurboModuleRegistry } from 'react-native';

import { BatchInboxFetcher, BatchUserAttribute, IInboxNotification } from './Batch';

export interface Spec extends TurboModule {
  addListener: (eventName: string) => void;
  removeListeners: (count: number) => void;

  getConstants(): {
    NOTIFICATION_TYPES: {
      NONE: number;
      SOUND: number;
      VIBRATE: number;
      LIGHTS: number;
      ALERT: number;
    };
  };

  // Core Module
  optIn(): Promise<void>;
  optOut(): Promise<void>;
  optOutAndWipeData(): Promise<void>;
  isOptedOut(): Promise<boolean>;
  updateAutomaticDataCollection(dataCollection: Object): void;
  showDebugView(): void;

  // Push Module
  push_setShowNotifications(enabled: boolean): void;
  push_shouldShowNotifications(): Promise<boolean | undefined>;
  push_clearBadge(): void;
  push_dismissNotifications(): void;
  push_getLastKnownPushToken(): Promise<string | null>;
  push_requestNotificationAuthorization(): void;
  push_requestProvisionalNotificationAuthorization(): void;
  push_refreshToken(): void;
  push_setShowForegroundNotification(enabled: boolean): void;
  push_getInitialDeeplink(): Promise<string | null>;

  // Messaging Module
  messaging_showPendingMessage(): Promise<void>;
  messaging_setNotDisturbed(active: boolean): Promise<void>;
  messaging_disableDoNotDisturbAndShowPendingMessage(): Promise<void>;
  messaging_setFontOverride(
    normalFontName?: string | null,
    boldFontName?: string | null,
    italicFontName?: string | null,
    italicBoldFontName?: string | null
  ): Promise<void>;

  // Inbox Module
  inbox_getFetcher(options: Object): Promise<BatchInboxFetcher>;
  inbox_fetcher_destroy(fetcherIdentifier: string): Promise<boolean>;
  inbox_fetcher_hasMore(fetcherIdentifier: string): Promise<boolean>;
  inbox_fetcher_markAllAsRead(fetcherIdentifier: string): Promise<void>;
  inbox_fetcher_markAsRead(fetcherIdentifier: string, notificationIdentifier: string): Promise<void>;
  inbox_fetcher_markAsDeleted(fetcherIdentifier: string, notificationIdentifier: string): Promise<void>;
  inbox_fetcher_fetchNewNotifications(fetcherIdentifier: string): Promise<{
    notifications: IInboxNotification[];
    endReached: boolean;
    foundNewNotifications: boolean;
  }>;
  inbox_fetcher_fetchNextPage(fetcherIdentifier: string): Promise<{
    notifications: IInboxNotification[];
    endReached: boolean;
  }>;
  inbox_fetcher_displayLandingMessage(fetcherIdentifier: string, notificationIdentifier: string): Promise<void>;
  inbox_fetcher_setFilterSilentNotifications(fetcherIdentifier: string, filterSilentNotifications: boolean): Promise<void>;

  // User Module
  user_getInstallationId(): Promise<string>;
  user_getIdentifier(): Promise<string | undefined>;
  user_getRegion(): Promise<string | undefined>;
  user_getLanguage(): Promise<string | undefined>;
  user_getAttributes(): Promise<{ [key: string]: BatchUserAttribute }>;
  user_getTags(): Promise<{ [key: string]: string[] }>;
  user_clearInstallationData(): void;

  // Profile Module
  profile_identify(identifier: string | null): void;
  profile_trackEvent(name: string, data?: Object): Promise<void>;
  profile_trackLocation(serializedLocation: Object): void;
  profile_saveEditor(actions: Object[]): void;
}

export default TurboModuleRegistry.get<Spec>('RNBatch') as Spec | null;
