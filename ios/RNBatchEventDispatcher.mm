# import "RNBatchEventDispatcher.h"
# define MAX_QUEUE_SIZE 5
@implementation RNBatchEventDispatcher {

    /// Whether NativeModule is ready or not
    BOOL _moduleIsReady;

    /// Event queue
    NSMutableArray<RNBatchEvent*>* _events;

    /// Block to send event from RNBatch
    void (^_sendBlock)(RNBatchEvent *_Nonnull event);
}

-(instancetype)init {
    self = [super init];
    if (self) {
        _events = [NSMutableArray array];
    }
    return self;
}

/// Set event sender block
-(void)setSendBlock:(void (^)(RNBatchEvent* event))callback {
    _sendBlock = callback;
    if (_sendBlock != nil) {
        [self dequeueEvents];
    }
}

/// Set native module is ready
- (void)setModuleIsReady:(BOOL)ready {
    _moduleIsReady = ready;
}

/// Send an event to the Js bridge throught the block
- (void)sendEvent:(RNBatchEvent*)event {
    if (_sendBlock != nil) {
        _sendBlock(event);
    }
}

/// Put an event in queue
- (void)queueEvent:(RNBatchEvent*)event {
    @synchronized(_events) {
        if ([_events count] >= MAX_QUEUE_SIZE) {
            [_events removeAllObjects];
        }
        [_events addObject:event];
    }
}

/// Dequeue all events
- (void)dequeueEvents {
    @synchronized(_events) {
        if ([_events count] == 0) {
            return;
        }
        NSArray *enqueuedEvents = [_events copy];
        [_events removeAllObjects];
        for (RNBatchEvent *event in enqueuedEvents) {
            [self sendEvent:event];
        }
    }
}

/// Batch event dispatcher callback
- (void)dispatchEventWithType:(BatchEventDispatcherType)type
                      payload:(nonnull id<BatchEventDispatcherPayload>)payload {


    NSString* eventName = [RNBatchEventDispatcher mapBatchEventDispatcherTypeToRNEvent:type];
    if (eventName != nil) {
        RNBatchEvent* event = [[RNBatchEvent alloc] initWithName:eventName andBody:[self dictionaryWithEventDispatcherPayload:payload]];
        if (!_moduleIsReady || _sendBlock == nil) {
            NSLog(@"RNBatch: Module is not ready or no listener registered. Queueing event.");
            [self queueEvent:event];
            return;
        }
        [self sendEvent:event];
    }
}

/// Mapping function
+ (nullable NSString *)mapBatchEventDispatcherTypeToRNEvent:(BatchEventDispatcherType)type {
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

/// Build payload event
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

    if (payload.sourceMessage != nil) {
        BatchMessage* sourceMessage = payload.sourceMessage;
        if ([sourceMessage isKindOfClass:BatchInAppMessage.class]) {
            output[@"messagingCustomPayload"] = ((BatchInAppMessage*) sourceMessage).customPayload;
        }
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
