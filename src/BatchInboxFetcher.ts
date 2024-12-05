import { IInboxNotification } from './BatchInbox';

const RNBatch = require('./NativeRNBatchModule').default;

export class BatchInboxFetcher {
  private readonly identifier: string;

  public constructor(identifier: string) {
    this.identifier = identifier;
  }

  /**
   * Destroys the fetcher.
   *
   * You'll usually want to use this when your component unmounts in order to free up memory.
   */
  public destroy(): Promise<void> {
    return RNBatch.inbox_fetcher_destroy(this.identifier);
  }

  /**
   * Returns whether there is more notification to fetch.
   */
  public hasMore(): Promise<boolean> {
    return RNBatch.inbox_fetcher_hasMore(this.identifier);
  }

  /**
   * Marks all notifications as read.
   */
  public markAllNotificationsAsRead(): Promise<void> {
    return RNBatch.inbox_fetcher_markAllAsRead(this.identifier);
  }

  /**
   * Marks a notification as read.
   *
   * The notification must have been fetched by this fetcher before.
   *
   * @param notificationIdentifier The identifier of the notification to mark as read
   */
  public markNotificationAsRead(notificationIdentifier: string): Promise<void> {
    return RNBatch.inbox_fetcher_markAsRead(this.identifier, notificationIdentifier);
  }

  /**
   * Marks a notification as deleted.
   *
   * The notification must have been fetched by this fetcher before.
   *
   * @param notificationIdentifier The identifier of the notification to mark as deleted
   */
  public markNotificationAsDeleted(notificationIdentifier: string): Promise<void> {
    return RNBatch.inbox_fetcher_markAsDeleted(this.identifier, notificationIdentifier);
  }

  /**
   * Display the landing message attached to the notification.
   *
   * The notification must have been fetched by this fetcher before.
   *
   * @param notificationIdentifier The identifier of the notification to display
   */
  public displayNotificationLandingMessage(notificationIdentifier: string): Promise<void> {
    return RNBatch.inbox_fetcher_displayLandingMessage(this.identifier, notificationIdentifier);
  }

  /**
   * Sets whether the SDK should filter silent notifications (pushes that don't result in a message being
   * shown to the user). Default: true
   *
   * @param filterSilentNotifications Whether the SDK should filter silent notifications
   */
  public setFilterSilentNotifications(filterSilentNotifications: boolean): Promise<void> {
    return RNBatch.inbox_fetcher_setFilterSilentNotifications(this.identifier, filterSilentNotifications);
  }

  /**
   * Fetches new notifications (and resets pagination to 0).
   *
   * Usually used as an initial fetch and refresh method in an infinite list.
   */
  public fetchNewNotifications(): Promise<{
    notifications: IInboxNotification[];
    endReached: boolean;
    foundNewNotifications: boolean;
  }> {
    return RNBatch.inbox_fetcher_fetchNewNotifications(this.identifier).then(result => ({
      ...result,
      notifications: parseNotifications(result.notifications),
    }));
  }

  /**
   * Fetches the next page of notifications.
   *
   * Usually used as a "fetchMore" method in an infinite list.
   */
  public fetchNextPage(): Promise<{
    notifications: IInboxNotification[];
    endReached: boolean;
  }> {
    return RNBatch.inbox_fetcher_fetchNextPage(this.identifier).then(result => ({
      ...result,
      notifications: parseNotifications(result.notifications),
    }));
  }
}

const parseNotifications = (notifications: IInboxNotification[]): IInboxNotification[] => {
  return notifications.map(notification => {
    if (!notification.payload) return notification;

    const batchPayload = notification.payload['com.batch'] as string;

    // Try parsing the raw batch payload
    try {
      return {
        ...notification,
        payload: {
          ...(notification.payload as Record<string, unknown>),
          'com.batch': typeof batchPayload == 'string' ? JSON.parse(batchPayload) : batchPayload,
        },
      };
    } catch (error) {
      console.warn('Failed parsing notification.', error);
      return notification;
    }
  });
};
