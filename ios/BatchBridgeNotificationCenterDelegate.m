//
//  Copyright © Batch.com. All rights reserved.
//

#import "BatchBridgeNotificationCenterDelegate.h"

#import <Batch/BatchPush.h>

@implementation BatchBridgeNotificationCenterDelegate
{
    __weak __nullable id<UNUserNotificationCenterDelegate> _previousDelegate;
}

static BOOL _batBridgeNotifDelegateShouldAutomaticallyRegister = true;


+ (BatchBridgeNotificationCenterDelegate *)sharedInstance
{
    static BatchBridgeNotificationCenterDelegate *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[BatchBridgeNotificationCenterDelegate alloc] init];
    });

    return sharedInstance;
}

+ (void)registerAsDelegate
{
    UNUserNotificationCenter *notifCenter = [UNUserNotificationCenter currentNotificationCenter];
    BatchBridgeNotificationCenterDelegate *instance = [self sharedInstance];
    instance.previousDelegate = notifCenter.delegate;
    notifCenter.delegate = instance;
}

+ (BOOL)automaticallyRegister
{
    return _batBridgeNotifDelegateShouldAutomaticallyRegister;
}

+ (void)setAutomaticallyRegister:(BOOL)automaticallyRegister
{
    _batBridgeNotifDelegateShouldAutomaticallyRegister = automaticallyRegister;
}

- (nullable id<UNUserNotificationCenterDelegate>)previousDelegate
{
    return _previousDelegate;
}

- (void)setPreviousDelegate:(nullable id<UNUserNotificationCenterDelegate>)delegate
{
    // Do not register default Batch delegate as previous one
    if ([delegate isKindOfClass:BatchUNUserNotificationCenterDelegate.class]) {
        NSLog(@"RNBatch: It looks like you are still using [BatchUNUserNotificationCenterDelegate registerAsDelegate]. Please remove it or set `BatchBridgeNotificationCenterDelegate.automaticallyRegister = false` before [RNBatch start] but calling `setShowForegroundNotification` will not work anymore.");
        _previousDelegate = nil;
        return;
    }
    // Do not register ourselves as previous delegate to avoid
    // an infinite loop
    if (delegate == self || [delegate isKindOfClass:[self class]]) {
        _previousDelegate = nil;
    } else {
        _previousDelegate = delegate;
    }
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        _showForegroundNotifications = true;
        _shouldUseChainedCompletionHandlerResponse = true;
    }
    return self;
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler
{
    [BatchPush handleUserNotificationCenter:center willPresentNotification:notification willShowSystemForegroundAlert:self.showForegroundNotifications];

    id<UNUserNotificationCenterDelegate> chainDelegate = self.previousDelegate;
    // It's the chain delegate's responsibility to call the completionHandler
    if ([chainDelegate respondsToSelector:@selector(userNotificationCenter:willPresentNotification:withCompletionHandler:)]) {
        //returnType (^blockName)(parameterTypes) = ^returnType(parameters) {...};
        void (^chainCompletionHandler)(UNNotificationPresentationOptions);

        if (self.shouldUseChainedCompletionHandlerResponse) {
            // Set iOS' completion handler as the one we give to the method, as we don't want to override the result
            chainCompletionHandler = completionHandler;
        } else {
            // Set ourselves as the chained completion handler so we can wait for the implementation but rewrite the response
            chainCompletionHandler = ^(UNNotificationPresentationOptions ignored) {
                [self performPresentCompletionHandler:completionHandler];
            };
        }

        [chainDelegate userNotificationCenter:center
                      willPresentNotification:notification
                        withCompletionHandler:chainCompletionHandler];
    } else {
        [self performPresentCompletionHandler:completionHandler];
    }
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler
{
    [BatchPush handleUserNotificationCenter:center didReceiveNotificationResponse:response];

    id<UNUserNotificationCenterDelegate> chainDelegate = self.previousDelegate;
    // It's the chain delegate's responsibility to call the completionHandler
    if ([chainDelegate respondsToSelector:@selector(userNotificationCenter:didReceiveNotificationResponse:withCompletionHandler:)]) {
        [chainDelegate userNotificationCenter:center
               didReceiveNotificationResponse:response
                        withCompletionHandler:completionHandler];
    } else {
        if (completionHandler) {
            completionHandler();
        }
    }

}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center openSettingsForNotification:(UNNotification *)notification
{
    if (@available(iOS 12.0, *)) {
        id<UNUserNotificationCenterDelegate> chainDelegate = self.previousDelegate;
        if ([chainDelegate respondsToSelector:@selector(userNotificationCenter:openSettingsForNotification:)]) {
            [self.previousDelegate userNotificationCenter:center
                              openSettingsForNotification:notification];
        }
    }
}

/// Call iOS back on the "present" completion handler with Batch controlled presentation options
- (void)performPresentCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    UNNotificationPresentationOptions options = UNNotificationPresentationOptionNone;
    if (self.showForegroundNotifications) {
        options = UNNotificationPresentationOptionBadge | UNNotificationPresentationOptionSound;

#ifdef __IPHONE_14_0
        if (@available(iOS 14.0, *)) {
            options = options | UNNotificationPresentationOptionList | UNNotificationPresentationOptionBanner;
        } else {
            options = options | UNNotificationPresentationOptionAlert;
        }
#else
        options = options | UNNotificationPresentationOptionAlert;
#endif
    }

    if (completionHandler) {
        completionHandler(options);
    };
}

@end
