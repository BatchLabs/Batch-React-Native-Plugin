# import "RNBatchEventDispatcher.h"

@implementation RNBatchEventDispatcher {

    BOOL _moduleIsReady;

    NSMutableArray<RNBatchEvent*>* _events;

    void (^_sendBlock)(RNBatchEvent *_Nonnull event);
}

-(instancetype)init {
    self = [super init];
    if (self) {
        _events = [NSMutableArray array];
    }
    return self;
}

-(void)setSendBlock:(void (^)(RNBatchEvent* event))callback {
    _sendBlock = callback;
    if(_sendBlock != nil) {
        [self dequeueEvents];
    }
}

- (void)setModuleIsReady:(BOOL)ready {
    _moduleIsReady = ready;
}


- (void)dequeueEvents {
    if(_sendBlock == nil) {
        return;
    }
    @synchronized(_events) {
        NSArray *enqueuedEvents = [_events copy];
        [_events removeAllObjects];

        for (RNBatchEvent *event in enqueuedEvents) {
            _sendBlock(event);
        }
    }
}

- (void)dispatchEventWithType:(BatchEventDispatcherType)type
                      payload:(nonnull id<BatchEventDispatcherPayload>)payload {
    
    if (_moduleIsReady && _sendBlock == nil) {
        // RN Module is ready but no listener registered
        // not queuing up events
        return;
    }
    NSString* eventName = [RNBatchEventDispatcher mapBatchEventDispatcherTypeToRNEvent:type];
    if (eventName != nil) {
        RNBatchEvent* event = [[RNBatchEvent alloc] initWithName:eventName andBody:[self dictionaryWithEventDispatcherPayload:payload]];
        @synchronized(_events) {
            [_events addObject:event];
        }
        if (_sendBlock != nil) {
            [self dequeueEvents];
        }
    }
}

+ (nullable NSString *) mapBatchEventDispatcherTypeToRNEvent:(BatchEventDispatcherType)type {
    switch (type) {
        case BatchEventDispatcherTypeNotificationOpen:
            return @"notification_open";
        case BatchEventDispatcherTypeMessagingShow:
            return @"messaging_show";
        case BatchEventDispatcherTypeMessagingClose:
            return @"messaging_close";
        case BatchEventDispatcherTypeMessagingCloseError:
            return @"messaging_close_error";
        case BatchEventDispatcherTypeMessagingAutoClose:
            return @"messaging_auto_close";
        case BatchEventDispatcherTypeMessagingClick:
            return @"messaging_click";
        case BatchEventDispatcherTypeMessagingWebViewClick:
            return @"messaging_webview_click";
    }
}

- (NSDictionary*) dictionaryWithEventDispatcherPayload:(id<BatchEventDispatcherPayload>)payload
{
    NSMutableDictionary *output = [NSMutableDictionary dictionaryWithDictionary:@{
        @"isPositiveAction": @(payload.isPositiveAction),
    }];

    if (payload.deeplink != nil) {
        output[@"deeplink"] = payload.deeplink;
    }

    if (payload.trackingId != nil) {
        output[@"trackingId"] = payload.trackingId;
    }

    if (payload.deeplink != nil) {
        output[@"webViewAnalyticsIdentifier"] = payload.webViewAnalyticsIdentifier;
    }

    if (payload.notificationUserInfo != nil) {
        output[@"pushPayload"] = payload.notificationUserInfo;
    }

    return output;
}

@end

@implementation RNBatchEvent

- (nonnull instancetype)initWithName:(nonnull NSString *)name andBody:(nullable NSDictionary *)body {
    self = [super init];
    if (self) {
        _name = name;
        _body = body;
    }
    return self;
}

@end