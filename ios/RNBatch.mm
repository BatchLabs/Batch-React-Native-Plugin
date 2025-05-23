# import <React/RCTConvert.h>
# import "RNBatch.h"
# import "RNBatchOpenedNotificationObserver.h"
# import "RNBatchEventDispatcher.h"
# import "BatchBridgeNotificationCenterDelegate.h"

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNBatchSpec.h"
#endif

static RNBatchEventDispatcher* dispatcher = nil;

@implementation RNBatch

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}
- (id)safeNilValue: (id)value
{
    if (value == (id)[NSNull null]) {
        return nil;
    }
    return value;
}
- (void)dealloc
{
    [_batchInboxFetcherMap removeAllObjects];
    _batchInboxFetcherMap = nil;
}
- (instancetype)init
{
    self = [super init];
    _batchInboxFetcherMap = [NSMutableDictionary new];
    [dispatcher setModuleIsReady:true];
    return self;
}

RCT_EXPORT_MODULE()

+ (void)start
{
    setenv("BATCH_PLUGIN_VERSION", PluginVersion, 1);

    NSDictionary *info = [[NSBundle mainBundle] infoDictionary];

    // Handling do not disturbed initial state
    id doNotDisturbEnabled = [info objectForKey:@"BatchDoNotDisturbInitialState"];
    if (doNotDisturbEnabled != nil) {
        [BatchMessaging setDoNotDisturb:[doNotDisturbEnabled boolValue]];
    } else {
        [BatchMessaging setDoNotDisturb:false];
    }

    // Handling profile migrations state
    id profileCustomIDMigrationEnabled = [info objectForKey:@"BatchProfileCustomIdMigrationEnabled"];
    id profileCustomDataMigrationEnabled = [info objectForKey:@"BatchProfileCustomDataMigrationEnabled"];
    BatchMigration migrations = BatchMigrationNone;
    if (profileCustomIDMigrationEnabled != nil && [profileCustomIDMigrationEnabled boolValue] == false) {
        NSLog(@"[BatchBridge] Disabling profile custom id migration");
        migrations |= BatchMigrationCustomID;
    }
    if (profileCustomDataMigrationEnabled != nil && [profileCustomDataMigrationEnabled boolValue] == false) {
        NSLog(@"[BatchBridge] Disabling profile custom data migration");
        migrations |= BatchMigrationCustomData;
    }
    [BatchSDK setDisabledMigrations:migrations];

    NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
    [BatchSDK startWithAPIKey:batchAPIKey];
    if (BatchBridgeNotificationCenterDelegate.automaticallyRegister) {
        [BatchBridgeNotificationCenterDelegate registerAsDelegate];
    }
    dispatcher = [[RNBatchEventDispatcher alloc] init];
    [BatchEventDispatcher addDispatcher:dispatcher];
}

-(void)startObserving {
    [dispatcher setSendBlock:^(RNBatchEvent* event) {
        [self sendEventWithName:event.name body:event.body];
    }];
}

-(void)stopObserving {
    [dispatcher setSendBlock:nil];
}

- (NSArray<NSString *> *)supportedEvents {
    NSMutableArray *events = [NSMutableArray new];

    for (int i = BatchEventDispatcherTypeNotificationOpen; i <= BatchEventDispatcherTypeMessagingWebViewClick; i++) {
        NSString* eventName = [RNBatchEventDispatcher mapBatchEventDispatcherTypeToRNEvent:(BatchEventDispatcherType)i];
        if (eventName != nil) {
            [events addObject:eventName];
        }
    }

    return events;
}

- (NSDictionary*)constantsToExport {
    return @{
        @"NOTIFICATION_TYPES": @{
            @"NONE":@0,
            @"SOUND":@1,
            @"VIBRATE":@2,
            @"LIGHTS":@3,
            @"ALERT":@4,
        }
    };
}

- (NSDictionary*)getConstants {
    return [self constantsToExport];
}

RCT_EXPORT_METHOD(optIn:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    if (BatchSDK.isOptedOut) {
        // Opt-in SDK
        [BatchSDK optIn];
        
        // Get API key and restart sdk
        NSDictionary *info = [[NSBundle mainBundle] infoDictionary];
        NSString *batchAPIKey = [info objectForKey:@"BatchAPIKey"];
        dispatch_async(dispatch_get_main_queue(), ^{
            [BatchSDK startWithAPIKey:batchAPIKey];
        });
    } else {
        NSLog(@"RNBatch: Batch SDK is already opted-in");
    }
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(optOut:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [BatchSDK optOut];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(optOutAndWipeData:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [BatchSDK optOutAndWipeData];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(isOptedOut:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    resolve([NSNumber numberWithBool:BatchSDK.isOptedOut]);
}

RCT_EXPORT_METHOD(updateAutomaticDataCollection:(NSDictionary *)dataCollectionConfig) {
    BOOL hasDeviceModel = [dataCollectionConfig objectForKey:@"deviceModel"] != nil;
    BOOL hasGeoIP = [dataCollectionConfig objectForKey:@"geoIP"] != nil;

    if (hasDeviceModel || hasGeoIP) {
        [BatchSDK updateAutomaticDataCollection:^(BatchDataCollectionConfig * _Nonnull batchDataCollectionConfig) {
            if (hasDeviceModel) {
                batchDataCollectionConfig.deviceModelEnabled = [dataCollectionConfig[@"deviceModel"] boolValue];
            }
            if (hasGeoIP) {
                batchDataCollectionConfig.geoIPEnabled = [dataCollectionConfig[@"geoIP"] boolValue];
            }
        }];
    } else {
        NSLog(@"BatchBridge - Invalid parameter: Data collection config cannot be empty.");
    }
}

RCT_EXPORT_METHOD(showDebugView)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *debugVC = [BatchSDK makeDebugViewController];
        if (debugVC) {
            [RCTPresentedViewController() presentViewController:debugVC animated:YES completion:nil];
        }
    });
}

// Push Module
RCT_EXPORT_METHOD(push_requestNotificationAuthorization)
{
    [BatchPush requestNotificationAuthorization];
}

RCT_EXPORT_METHOD(push_requestProvisionalNotificationAuthorization)
{
    [BatchPush requestProvisionalNotificationAuthorization];
}

RCT_EXPORT_METHOD(push_refreshToken)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [BatchPush refreshToken];
    });
}

RCT_EXPORT_METHOD(push_setShowForegroundNotification:(BOOL) enabled)
{
    BatchBridgeNotificationCenterDelegate *delegate = [BatchBridgeNotificationCenterDelegate sharedInstance];
    delegate.showForegroundNotifications = enabled;
    delegate.shouldUseChainedCompletionHandlerResponse = false;
}

RCT_EXPORT_METHOD(push_clearBadge)
{
    [BatchPush clearBadge];
}

RCT_EXPORT_METHOD(push_dismissNotifications)
{
    [BatchPush dismissNotifications];
}

RCT_EXPORT_METHOD(push_getLastKnownPushToken:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    NSString* lastKnownPushToken = [BatchPush lastKnownPushToken];
    resolve(lastKnownPushToken);
}

RCT_EXPORT_METHOD(push_getInitialDeeplink:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    resolve([RNBatchOpenedNotificationObserver getInitialDeeplink]);
}

RCT_EXPORT_METHOD(push_setNotificationTypes:(double)notifType) {
    // Android only
}


// User module

RCT_EXPORT_METHOD(user_getInstallationId:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    NSString* installationId = [BatchUser installationID];
    resolve(installationId);
}

RCT_EXPORT_METHOD(user_getIdentifier:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    NSString* userId = [BatchUser identifier];
    resolve(userId);
}

RCT_EXPORT_METHOD(user_getRegion:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    NSString* region = [BatchUser region];
    resolve(region);
}

RCT_EXPORT_METHOD(user_getLanguage:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    NSString* language = [BatchUser language];
    resolve(language);
}

RCT_EXPORT_METHOD(user_getAttributes:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    [BatchUser fetchAttributes:^(NSDictionary<NSString *,BatchUserAttribute *> * _Nullable attributes) {

        if (attributes == nil) {
            reject(@"BatchBridgeError", @"Native SDK fetchAttributes returned an error", nil);
            return;
        }
        NSMutableDictionary<NSString*, NSDictionary<NSString*, id>*>* bridgeAttributes = [[NSMutableDictionary alloc] initWithCapacity:attributes.count];
        [attributes enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, BatchUserAttribute * _Nonnull attribute, BOOL * _Nonnull stop) {
            NSString *bridgeType;
            id bridgeValue = nil;

            switch (attribute.type) {
                case BatchUserAttributeTypeBool:
                    bridgeType = @"b";
                    bridgeValue = attribute.numberValue;
                    break;
                case BatchUserAttributeTypeDate:
                {
                    bridgeType = @"d";
                    NSDate *dateValue = attribute.dateValue;
                    if (dateValue != nil) {
                        bridgeValue = @(floor(dateValue.timeIntervalSince1970 * 1000));
                    }
                    break;
                }
                case BatchUserAttributeTypeDouble:
                    bridgeType = @"f";
                    bridgeValue = attribute.numberValue;
                    break;
                case BatchUserAttributeTypeLongLong:
                    bridgeType = @"i";
                    bridgeValue = attribute.numberValue;
                    break;
                case BatchUserAttributeTypeString:
                    bridgeType = @"s";
                    bridgeValue = attribute.stringValue;
                    break;
                case BatchUserAttributeTypeURL:
                    bridgeType = @"u";
                    bridgeValue = attribute.urlValue.absoluteString;
                    break;
                default:
                {
                    reject(@"BatchBridgeError", [NSString stringWithFormat:@"Fetch attribute: Unknown attribute type %lu.", (unsigned long)attribute.type], nil);
                    *stop = true;
                    return;
                }
            }
            if (bridgeValue == nil) {
                reject(@"BatchBridgeError", [NSString stringWithFormat:@"Fetch attribute: Failed to serialize attribute for type %@", bridgeType], nil);
                *stop = true;
                return;
             }
            *stop = false;
            bridgeAttributes[key] = @{
                @"type": bridgeType,
                @"value": bridgeValue,
            };
        }];
        resolve(bridgeAttributes);
    }];
}

RCT_EXPORT_METHOD(user_getTags:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    [BatchUser fetchTagCollections:^(NSDictionary<NSString *,NSSet<NSString *> *> * _Nullable collections) {
        if (collections == nil) {
            reject(@"BatchBridgeError", @"Native SDK fetchTagCollections returned an error", nil);
            return;
        }

        NSMutableDictionary<NSString*, NSArray<NSString*>*>* bridgeTagCollections = [[NSMutableDictionary alloc] initWithCapacity:collections.count];
        [collections enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, NSSet<NSString *> * _Nonnull obj, BOOL * _Nonnull stop) {
            bridgeTagCollections[key] = [obj allObjects];
        }];
        resolve(bridgeTagCollections);
    }];

}

RCT_EXPORT_METHOD(user_clearInstallationData)
{
    [BatchUser clearInstallationData];
}

// Profile

RCT_EXPORT_METHOD(profile_identify:(NSString*)identifier)
{
    [BatchProfile identify:identifier];
}

RCT_EXPORT_METHOD(profile_trackEvent:(NSString*)name data:(NSDictionary*)serializedEventData resolve: (RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    BatchEventAttributes *batchEventAttributes = nil;

    if ([serializedEventData isKindOfClass:[NSDictionary class]]) {

        batchEventAttributes = [self convertSerializedEventDataToEventAttributes:serializedEventData];

        NSError *err;
        [batchEventAttributes validateWithError:&err];
        if (batchEventAttributes != nil && err == nil) {
            [BatchProfile trackEventWithName:name attributes:batchEventAttributes];
            resolve([NSNull null]);
        } else {
            reject(@"BatchBridgeError", @"Event attributes validation failed:", err);
        }
        return;
    }
    [BatchProfile trackEventWithName:name attributes:batchEventAttributes];
    resolve([NSNull null]);
}

- (BatchEventAttributes*) convertSerializedEventDataToEventAttributes:(NSDictionary *) serializedAttributes {

    BatchEventAttributes *eventAttributes = [BatchEventAttributes new];

    if (![serializedAttributes isKindOfClass:[NSDictionary class]]) {
        NSLog(@"RNBatch: Error while tracking event data: event data.attributes should be a dictionary");
        return nil;
    }

    for (NSString *key in serializedAttributes.allKeys) {
        NSDictionary *typedAttribute = serializedAttributes[key];
        if (![typedAttribute isKindOfClass:[NSDictionary class]])
        {
            NSLog(@"RNBatch: Error while tracking event data: event data.attributes childrens should all be String/Dictionary tuples");
            return nil;
        }

        NSString *type = typedAttribute[@"type"];
        NSObject *value = typedAttribute[@"value"];

        if ([@"string" isEqualToString:type]) {
            if (![value isKindOfClass:[NSString class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected string value, got something else");
                return nil;
            }
            [eventAttributes putString:(NSString*)value forKey:key];
        } else if ([@"boolean" isEqualToString:type]) {
            if (![value isKindOfClass:[NSNumber class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number (boolean) value, got something else");
                return nil;
            }
            [eventAttributes putBool:[(NSNumber*)value boolValue] forKey:key];
        } else if ([@"integer" isEqualToString:type]) {
            if (![value isKindOfClass:[NSNumber class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number (integer) value, got something else");
                return nil;
            }
            [eventAttributes putInteger:[(NSNumber*)value integerValue] forKey:key];
        } else if ([@"float" isEqualToString:type]) {
            if (![value isKindOfClass:[NSNumber class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number (float) value, got something else");
                return nil;
            }
            [eventAttributes putDouble:[(NSNumber*)value doubleValue] forKey:key];
        } else if ([@"date" isEqualToString:type]) {
            if (![value isKindOfClass:[NSNumber class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected number value, got something else");
                return nil;
            }
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:[(NSNumber*)value doubleValue] / 1000.0];
            [eventAttributes putDate:date forKey:key];
        } else if ([@"url" isEqualToString:type]) {
            if (![value isKindOfClass:[NSString class]])
            {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected string value, got something else");
                return nil;
            }
            [eventAttributes putURL:[NSURL URLWithString:(NSString*) value] forKey:key];
        }
        else if ([@"object" isEqualToString:type]) {
            if (![value isKindOfClass:[NSDictionary class]]){
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected dictionary value, got something else");
                return nil;
            }
            BatchEventAttributes *attributes = [self convertSerializedEventDataToEventAttributes:(NSDictionary*)value];
            if (attributes != nil) {
                [eventAttributes putObject:attributes forKey:key];
            }
        }
        else if ([@"string_array" isEqualToString:type]) {
            if (![value isKindOfClass:[NSArray class]]){
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected string array value, got something else");
                return nil;
            }

            [eventAttributes putStringArray:(NSArray*)value forKey:key];
        } else if ([@"object_array" isEqualToString:type]) {
            if (![value isKindOfClass:[NSArray class]]) {
                NSLog(@"RNBatch: Error while tracking event data: event data.attributes: expected dictionnary value, got something else");
                return nil;
            }
            NSMutableArray<BatchEventAttributes *> *list = [NSMutableArray array];
            NSArray *array = (NSArray*)value;
            for (int i = 0; i < array.count; i++) {
                BatchEventAttributes *object = [self convertSerializedEventDataToEventAttributes:array[i]];
                if (object != nil) {
                    [list addObject:object];
                }
            }
            [eventAttributes putObjectArray:list forKey:key];
        }else {
            NSLog(@"RNBatch: Error while tracking event data: Unknown event data.attributes type");
            return nil;
        }
    }
    return eventAttributes;
}

RCT_EXPORT_METHOD(profile_trackLocation:(NSDictionary*)serializedLocation)
{
    if (![serializedLocation isKindOfClass:[NSDictionary class]] || [serializedLocation count]==0)
    {
        NSLog(@"RNBatch: Empty or null parameters for trackLocation");
        return;
    }

    NSNumber *latitude = serializedLocation[@"latitude"];
    NSNumber *longitude = serializedLocation[@"longitude"];
    NSNumber *date = serializedLocation[@"date"]; // MS
    NSNumber *precision = serializedLocation[@"precision"];

    if (![latitude isKindOfClass:[NSNumber class]])
    {
        NSLog(@"RNBatch: latitude should be a string");
        return;
    }

    if (![longitude isKindOfClass:[NSNumber class]])
    {
        NSLog(@"RNBatch: longitude should be a string");
        return;
    }

    NSTimeInterval ts = 0;

    if (date)
    {
        if ([date isKindOfClass:[NSNumber class]]) {
            ts = [date doubleValue] / 1000.0;
        } else {
            NSLog(@"RNBatch: date should be an object or undefined");
            return;
        }
    }

    NSDate *parsedDate = ts != 0 ? [NSDate dateWithTimeIntervalSince1970:ts] : [NSDate date];

    NSInteger parsedPrecision = 0;
    if (precision)
    {
        if ([precision isKindOfClass:[NSNumber class]]) {
            parsedPrecision = [precision integerValue];
        } else {
            NSLog(@"RNBatch: precision should be an object or undefined");
            return;
        }
    }

    [BatchProfile trackLocation:[[CLLocation alloc] initWithCoordinate:CLLocationCoordinate2DMake([latitude doubleValue], [longitude doubleValue])
                                                           altitude:0
                                                 horizontalAccuracy:parsedPrecision
                                                   verticalAccuracy:-1
                                                             course:0
                                                              speed:0
                                                          timestamp:parsedDate]];
}

RCT_EXPORT_METHOD(profile_saveEditor:(NSArray*)actions)
{
    BatchProfileEditor *editor = [BatchProfile editor];
    for (NSDictionary* action in actions) {
        NSString* type = action[@"type"];
        NSString* key = action[@"key"];

        // Set double, long, NSString, bool class values
        if([type isEqualToString:@"setAttribute"]) {
            id value = action[@"value"];
            if ([value isKindOfClass:[NSString class]]) {
                [editor setStringAttribute:value forKey:key error:nil];
            } else if([value isKindOfClass:[NSNumber class]]) {
                if (value == (id)kCFBooleanTrue || value == (id)kCFBooleanFalse) {
                    [editor setBooleanAttribute:[value boolValue] forKey:key error:nil];
                } else {
                    [editor setDoubleAttribute:[value doubleValue] forKey:key error:nil];
                }
            } else if([value isKindOfClass:[NSString class]]) {
                [editor setStringAttribute:value forKey:key error:nil];
            } else if([value isKindOfClass:[NSArray class]]) {
                [editor setStringArrayAttribute:value forKey:key error:nil];
            } else if([value isKindOfClass:[NSNull class]]) {
                [editor removeAttributeForKey:key error:nil];
            }
        }
        // Handle dates
        // @TODO: prevent date parsing from erroring
        else if([type isEqualToString:@"setDateAttribute"]) {
            double timestamp = [action[@"value"] doubleValue];
            NSTimeInterval unixTimeStamp = timestamp / 1000.0;
            NSDate *date = [NSDate dateWithTimeIntervalSince1970:unixTimeStamp];
            [editor setDateAttribute:date forKey:key error:nil];
        }
        else if([type isEqualToString:@"setURLAttribute"]) {
            NSURL *url = [NSURL URLWithString:[self safeNilValue:action[@"value"]]];
            [editor setURLAttribute:url forKey:action[@"key"] error:nil];

        }
        else if([type isEqualToString:@"removeAttribute"]) {
            [editor removeAttributeForKey:action[@"key"] error:nil];
        }

        else if([type isEqualToString:@"setEmailAddress"]) {
            [editor setEmailAddress:[self safeNilValue:action[@"value"]] error:nil];
        }

        else if([type isEqualToString:@"setEmailMarketingSubscription"]) {
            NSString* value = action[@"value"];
            if([value isEqualToString:@"SUBSCRIBED"]) {
                [editor setEmailMarketingSubscriptionState:BatchEmailSubscriptionStateSubscribed];
            } else if ([value isEqualToString:@"UNSUBSCRIBED"]) {
                 [editor setEmailMarketingSubscriptionState: BatchEmailSubscriptionStateUnsubscribed];
            }
        }

        else if([type isEqualToString:@"setPhoneNumber"]) {
            [editor setPhoneNumber:[self safeNilValue:action[@"value"]] error:nil];
        }

        else if([type isEqualToString:@"setSMSMarketingSubscription"]) {
            NSString* value = action[@"value"];
            if([value isEqualToString:@"SUBSCRIBED"]) {
                [editor setSMSMarketingSubscriptionState:BatchSMSSubscriptionStateSubscribed];
            } else if ([value isEqualToString:@"UNSUBSCRIBED"]) {
                 [editor setSMSMarketingSubscriptionState: BatchSMSSubscriptionStateUnsubscribed];
            }
        }

        else if([type isEqualToString:@"setLanguage"]) {
            [editor setLanguage:[self safeNilValue:action[@"value"]] error:nil];
        }

        else if([type isEqualToString:@"setRegion"]) {
            [editor setRegion:[self safeNilValue:action[@"value"]] error:nil];
        }

        else if([type isEqualToString:@"addToArray"]) {
            id value = action[@"value"];
            if ([value isKindOfClass:[NSString class]]) {
                [editor addItemToStringArrayAttribute:value forKey:key error:nil];
            } else if ([value isKindOfClass:[NSArray class]]) {
                for (NSString *item in value) {
                    [editor addItemToStringArrayAttribute:item forKey:key error:nil];
                }
            }
        }

        else if([type isEqualToString:@"removeFromArray"]) {
            id value = action[@"value"];
            if ([value isKindOfClass:[NSString class]]) {
                [editor removeItemFromStringArrayAttribute:value forKey:key error:nil];
            } else if ([value isKindOfClass:[NSArray class]]) {
                for (NSString *item in value) {
                    [editor removeItemFromStringArrayAttribute:item forKey:key error:nil];
                }
            }
        }
    }
    [editor save];
}

// Inbox module

- (BatchInboxFetcher*) getFetcherFromOptions:(NSDictionary *) options {
    NSDictionary* userOptions = options[@"user"];

    if (!userOptions) {
        return [BatchInbox fetcher];
    }

    return [BatchInbox fetcherForUserIdentifier:userOptions[@"identifier"] authenticationKey:userOptions[@"authenticationKey"]];
}

RCT_EXPORT_METHOD(inbox_getFetcher:
                  (NSDictionary *) options
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {

    BatchInboxFetcher* fetcher = [self getFetcherFromOptions:options];

    if (options[@"fetchLimit"]) {
        fetcher.limit = [options[@"fetchLimit"] unsignedIntegerValue];
    }

    if (options[@"maxPageSize"]) {
        fetcher.maxPageSize = [options[@"maxPageSize"] unsignedIntegerValue];
    }

    NSString* fetcherIdentifier = [[NSUUID UUID] UUIDString];
    _batchInboxFetcherMap[fetcherIdentifier] = fetcher;

    resolve(fetcherIdentifier);
}

RCT_EXPORT_METHOD(inbox_fetcher_destroy:
                  (NSString *) fetcherIdentifier
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    [_batchInboxFetcherMap removeObjectForKey:fetcherIdentifier];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(inbox_fetcher_hasMore:
                  (NSString *) fetcherIdentifier
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }
    resolve(@(!fetcher.endReached));
}

RCT_EXPORT_METHOD(inbox_fetcher_markAllAsRead:
                  (NSString *) fetcherIdentifier
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }
    [fetcher markAllNotificationsAsRead];
    resolve([NSNull null]);
}

- (BatchInboxNotificationContent *) findNotificationInList: (NSArray<BatchInboxNotificationContent *> *) allNotifications
                                withNotificationIdentifier: (NSString*) notificationIdentifier {
    NSUInteger notificationIndex = [allNotifications indexOfObjectPassingTest:^BOOL(id currentNotification, NSUInteger idx, BOOL *stop) {
        return ([[(BatchInboxNotificationContent *)currentNotification identifier] isEqualToString:notificationIdentifier]);
    }];

    if (notificationIndex == NSNotFound) {
        return nil;
    }

    return [allNotifications objectAtIndex:notificationIndex];
}

RCT_EXPORT_METHOD(inbox_fetcher_markAsRead:
                  (NSString *) fetcherIdentifier
                  notificationIdentifier:
                  (NSString *) notificationIdentifier
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    BatchInboxNotificationContent * notification = [self findNotificationInList:[fetcher allFetchedNotifications] withNotificationIdentifier:notificationIdentifier];

    if (!notification) {
        reject(@"InboxError", @"NOTIFICATION_NOT_FOUND", nil);
        return;
    }

    [fetcher markNotificationAsRead:notification];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(inbox_fetcher_markAsDeleted:
                  (NSString *) fetcherIdentifier
                  notificationIdentifier:
                  (NSString *) notificationIdentifier
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    BatchInboxNotificationContent * notification = [self findNotificationInList:[fetcher allFetchedNotifications] withNotificationIdentifier:notificationIdentifier];

    if (!notification) {
        reject(@"InboxError", @"NOTIFICATION_NOT_FOUND", nil);
        return;
    }

    [fetcher markNotificationAsDeleted:notification];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(inbox_fetcher_displayLandingMessage:
                  (NSString *) fetcherIdentifier
                  notificationIdentifier:
                  (NSString *) notificationIdentifier
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    BatchInboxNotificationContent * notification = [self findNotificationInList:[fetcher allFetchedNotifications] withNotificationIdentifier:notificationIdentifier];

    if (!notification) {
        reject(@"InboxError", @"NOTIFICATION_NOT_FOUND", nil);
        return;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [notification displayLandingMessage];
        resolve([NSNull null]);
    });
}

RCT_EXPORT_METHOD(inbox_fetcher_setFilterSilentNotifications:
                  (NSString *) fetcherIdentifier
                  filterSilentNotifications:
                  (BOOL) filterSilentNotifications
                  resolve:
                  (RCTPromiseResolveBlock) resolve
                  reject:
                  (RCTPromiseRejectBlock) reject) {
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    [fetcher setFilterSilentNotifications:filterSilentNotifications];
    resolve([NSNull null]);
}


RCT_EXPORT_METHOD(inbox_fetcher_fetchNewNotifications:
                  (NSString *) fetcherIdentifier
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    [fetcher fetchNewNotifications:^(NSError * _Nullable error, NSArray<BatchInboxNotificationContent *> * _Nullable notifications, BOOL foundNewNotifications, BOOL endReached) {

        if (error) {
            NSString* errorMsg = [NSString stringWithFormat:@"Failed to fetch new notifications %@", [error localizedDescription]];
            reject(@"InboxFetchError", errorMsg, error);
        } else {
            NSMutableArray *formattedNotifications = [NSMutableArray new];
            for (BatchInboxNotificationContent *notification in notifications) {
                [formattedNotifications addObject:[self dictionaryWithNotification:notification]];
            }

            NSMutableDictionary *result = [NSMutableDictionary new];
            result[@"notifications"] = formattedNotifications;
            result[@"endReached"] = @(endReached);
            result[@"foundNewNotifications"] = @(foundNewNotifications);

            resolve(result);
        }

    }];
}

RCT_EXPORT_METHOD(inbox_fetcher_fetchNextPage:
                  (NSString *) fetcherIdentifier
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    BatchInboxFetcher* fetcher = _batchInboxFetcherMap[fetcherIdentifier];
    if (!fetcher) {
        reject(@"InboxError", @"FETCHER_NOT_FOUND", nil);
        return;
    }

    [fetcher fetchNextPage:^(NSError * _Nullable error, NSArray<BatchInboxNotificationContent *> * _Nullable notifications, BOOL endReached) {

        if (error) {
            NSString* errorMsg = [NSString stringWithFormat:@"Failed to fetch new notifications %@", [error localizedDescription]];
            reject(@"InboxFetchError", errorMsg, error);
        } else {
            NSMutableArray *formattedNotifications = [NSMutableArray new];
            for (BatchInboxNotificationContent *notification in notifications) {
                [formattedNotifications addObject:[self dictionaryWithNotification:notification]];
            }

            NSMutableDictionary *result = [NSMutableDictionary new];
            result[@"notifications"] = formattedNotifications;
            result[@"endReached"] = @(endReached);

            resolve(result);
        }
    }];
}

- (NSDictionary*) dictionaryWithNotification:(BatchInboxNotificationContent*)notification
{
    NSNumber *source = [NSNumber numberWithInt:0];
    switch (notification.source) {
        case BatchNotificationSourceCampaign:
            source = [NSNumber numberWithInt:1];
            break;
        case BatchNotificationSourceTransactional:
            source = [NSNumber numberWithInt:2];
            break;
        default:
            break;
    }

    NSString *title = notification.message.title;
    NSString *body = notification.message.body;

    NSDictionary *output = @{
        @"identifier": notification.identifier,
        @"isUnread": @(notification.isUnread),
        @"isSilent": @(notification.isSilent),
        @"date": [NSNumber numberWithDouble:notification.date.timeIntervalSince1970 * 1000],
        @"source": source,
        @"payload": notification.payload,
        @"hasLandingMessage": @(notification.hasLandingMessage)
    };

    NSMutableDictionary *mutableOutput = [output mutableCopy];
    if (title != nil) {
        mutableOutput[@"title"] = title;
    }
    if (body != nil) {
        mutableOutput[@"body"] = body;
    }
    output = mutableOutput;
    return output;
}

// Messaging module

RCT_EXPORT_METHOD(messaging_setNotDisturbed:(BOOL) active
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    [BatchMessaging setDoNotDisturb:active];
    resolve([NSNull null]);
}

RCT_EXPORT_METHOD(messaging_showPendingMessage:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [BatchMessaging showPendingMessage];
        resolve([NSNull null]);
    });
}

RCT_EXPORT_METHOD(messaging_disableDoNotDisturbAndShowPendingMessage:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        [BatchMessaging setDoNotDisturb:false];
        [BatchMessaging showPendingMessage];
        resolve([NSNull null]);
    });
}

RCT_EXPORT_METHOD(messaging_setFontOverride:(nullable NSString*) normalFontName boldFontName:(nullable NSString*) boldFontName italicFontName:(nullable NSString*) italicFontName italicBoldFontName:(nullable NSString*) italicBoldFontName
                  resolve: (RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    UIFont* normalFont = normalFontName != nil ? [UIFont fontWithName:normalFontName size: 14] : nil;
    UIFont* boldFont = boldFontName != nil ? [UIFont fontWithName:boldFontName size: 14] : nil;
    UIFont* italicFont = italicFontName != nil ? [UIFont fontWithName:italicFontName size: 14] : nil;
    UIFont* italicBoldFont = italicBoldFontName != nil ? [UIFont fontWithName:italicBoldFontName size: 14] : nil;

    [BatchMessaging setFontOverride:normalFont boldFont:boldFont italicFont:italicFont boldItalicFont:italicBoldFont];

    resolve([NSNull null]);
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNBatchModuleSpecJSI>(params);
}
#endif

@end
