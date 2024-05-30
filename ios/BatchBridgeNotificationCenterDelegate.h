//
//  Copyright © Batch.com. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UserNotifications/UserNotifications.h>

NS_ASSUME_NONNULL_BEGIN

// Batch's bridge UNUserNotificationCenterDelegate
// Handles:
// - Forwarding calls to another delegate (chaining, rather than swizzling)
// - Giving notification callbacks to Batch
// - Enabling/Disabling foreground notifications
@interface BatchBridgeNotificationCenterDelegate : NSObject <UNUserNotificationCenterDelegate>

/// Shared singleton BatchUNUserNotificationCenterDelegate.
/// Using this allows you to set the instance as UNUserNotificationCenter's delegate without having to retain it yourself.
/// The shared instance is lazily loaded.
@property (class, retain, readonly, nonnull) BatchBridgeNotificationCenterDelegate* sharedInstance;

/// Registers this class' sharedInstance as UNUserNotificationCenter's delegate, and stores the previous one as a property
+ (void)registerAsDelegate;

/// Should iOS display notifications even if the app is in foreground?
/// Default: true
@property (assign) BOOL showForegroundNotifications;

/// Should Batch use the chained delegate's completionHandler responses or force its own, while still calling the chained delegate.
/// This is useful if you want Batch to enforce its "showForegroundNotifications" setting while still informing the chained delegate.
/// Default: true, but the plugin will automatically set that to false when calling "setShowForegroundNotification" from JavaScript.
@property (assign) BOOL shouldUseChainedCompletionHandlerResponse;

/// Previous delegate
@property (weak, nullable) id<UNUserNotificationCenterDelegate> previousDelegate;

/// Should this class automatically register itself as UNUserNotificationCenterDelegate when the app is launched? Default: true
/// This value needs to be changed before `[RNBatch start]` be called.
@property (class, assign) BOOL automaticallyRegister;

@end

NS_ASSUME_NONNULL_END
