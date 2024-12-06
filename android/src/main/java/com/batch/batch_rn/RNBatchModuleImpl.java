package com.batch.batch_rn;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.Batch;
import com.batch.android.BatchActivityLifecycleHelper;
import com.batch.android.BatchAttributesFetchListener;
import com.batch.android.BatchEventAttributes;
import com.batch.android.BatchMigration;
import com.batch.android.BatchProfileAttributeEditor;
import com.batch.android.BatchPushRegistration;
import com.batch.android.BatchTagCollectionsFetchListener;
import com.batch.android.BatchEmailSubscriptionState;
import com.batch.android.BatchSMSSubscriptionState;
import com.batch.android.BatchUserAttribute;
import com.batch.android.PushNotificationType;
import com.batch.android.BatchInboxFetcher;
import com.batch.android.BatchInboxNotificationContent;
import com.batch.android.BatchMessage;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.net.URI;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RNBatchModuleImpl {

    public static final String NAME = "RNBatch";

    private static final String PLUGIN_VERSION_ENVIRONMENT_VARIABLE = "batch.plugin.version";

    public static final String PLUGIN_VERSION = "ReactNative/10.0.0";

    public static final String LOGGER_TAG = "RNBatchBridge";

    private static final String BATCH_BRIDGE_ERROR_CODE = "BATCH_BRIDGE_ERROR";

    private final ReactApplicationContext reactContext;

    private final Map<String, BatchInboxFetcher> batchInboxFetcherMap;

    private static final RNBatchEventDispatcher eventDispatcher = new RNBatchEventDispatcher();

    static {
        System.setProperty("batch.plugin.version", PLUGIN_VERSION);
    }

    private static boolean isInitialized = false;

    public static Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        // Add push notification types
        final Map<String, Object> notificationTypes = new HashMap<>();
        for (PushNotificationType type : PushNotificationType.values()) {
            notificationTypes.put(type.name(), type.ordinal());
        }
        constants.put("NOTIFICATION_TYPES", notificationTypes);

        return constants;
    }

    public static void initialize(Application application) {
        if (!isInitialized) {
            Resources resources = application.getResources();
            String packageName = application.getPackageName();
            setDefaultProfileMigrations(application.getApplicationContext(), packageName);
            String batchAPIKey = resources.getString(resources.getIdentifier("BATCH_API_KEY", "string", packageName));
            Batch.start(batchAPIKey);
            Batch.EventDispatcher.addDispatcher(eventDispatcher);
            try {
                boolean doNotDisturbEnabled = resources.getBoolean(resources.getIdentifier("BATCH_DO_NOT_DISTURB_INITIAL_STATE", "bool", packageName));
                Batch.Messaging.setDoNotDisturbEnabled(doNotDisturbEnabled);
            } catch (Resources.NotFoundException e) {
                Batch.Messaging.setDoNotDisturbEnabled(false);
            }

            application.registerActivityLifecycleCallbacks(new BatchActivityLifecycleHelper());

            isInitialized = true;
        }
    }

    public static void setDefaultProfileMigrations(Context context, String packageName) {
        try {
            Bundle metaData = context.getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    .metaData;
            if (metaData != null) {
                boolean profileCustomIdMigrationEnabled = metaData.getBoolean("batch.profile_custom_id_migration_enabled", true);
                boolean profileCustomDataMigrationEnabled = metaData.getBoolean("batch.profile_custom_data_migration_enabled", true);
                EnumSet<BatchMigration> migrations = EnumSet.noneOf(BatchMigration.class);
                if (!profileCustomIdMigrationEnabled) {
                    Log.d(LOGGER_TAG, "Disabling profile custom id migration.");
                    migrations.add(BatchMigration.CUSTOM_ID);
                }
                if (!profileCustomDataMigrationEnabled) {
                    Log.d(LOGGER_TAG, "Disabling profile custom data migration.");
                    migrations.add(BatchMigration.CUSTOM_DATA);
                }
                Batch.disableMigration(migrations);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public RNBatchModuleImpl(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        this.batchInboxFetcherMap = new HashMap<>();
        eventDispatcher.setReactContext(reactContext);
    }

    public void start(Activity activity) {
        if (activity == null) {
            return;
        }
        Batch.onStart(activity);
    }

    // BASE MODULE

    public void optIn(Activity activity, Promise promise) {
        Batch.optIn(reactContext);
        start(activity);
        promise.resolve(null);
    }

    public void optOut(Promise promise) {
        Batch.optOut(reactContext);
        promise.resolve(null);
    }

    public void optOutAndWipeData(Promise promise) {
        Batch.optOutAndWipeData(reactContext);
        promise.resolve(null);
    }

    public void isOptedOut(Promise promise) {
        boolean isOptedOut = Batch.isOptedOut(reactContext);
        promise.resolve(isOptedOut);
    }

    public void updateAutomaticDataCollection(@NonNull ReadableMap dataCollectionConfig) {
        boolean hasDeviceBrand = dataCollectionConfig.hasKey("deviceBrand");
        boolean hasDeviceModel = dataCollectionConfig.hasKey("deviceModel");
        boolean hasGeoIP = dataCollectionConfig.hasKey("geoIP");
        if (hasDeviceBrand || hasDeviceModel || hasGeoIP) {
            Batch.updateAutomaticDataCollection(batchDataCollectionConfig -> {
                if (hasDeviceBrand) {
                    batchDataCollectionConfig.setDeviceBrandEnabled(dataCollectionConfig.getBoolean("deviceBrand"));
                }
                if (hasDeviceModel) {
                    batchDataCollectionConfig.setDeviceModelEnabled(dataCollectionConfig.getBoolean("deviceModel"));
                }
                if (hasGeoIP) {
                    batchDataCollectionConfig.setGeoIPEnabled(dataCollectionConfig.getBoolean("geoIP"));
                }
            });
        } else {
            Log.e(LOGGER_TAG, "Invalid parameter : Data collection config cannot be empty");
        }
    }

    public void showDebugView(Activity currentActivity) {
        if (currentActivity != null) {
            Batch.Debug.startDebugActivity(currentActivity);
        }
    }

    public void addListener(String eventName) {
        eventDispatcher.setHasListener(true);
    }

    public void removeListeners(double count) {
    }

    // PUSH MODULE

    public void push_setNotificationTypes(Integer notifType) {
        EnumSet<PushNotificationType> pushTypes = PushNotificationType.fromValue(notifType);
        Batch.Push.setNotificationsType(pushTypes);
    }

    public void push_getLastKnownPushToken(Promise promise) {
        BatchPushRegistration registration = Batch.Push.getRegistration();
        promise.resolve(registration != null ? registration.getToken() : null);
    }

    public void push_requestNotificationAuthorization() {
        Batch.Push.requestNotificationPermission(reactContext);
    }

    // MESSAGING MODULE

    private void showPendingMessage(Activity currentActivity) {
        if (currentActivity == null) {
            return;
        }

        BatchMessage message = Batch.Messaging.popPendingMessage();
        if (message != null) {
            Batch.Messaging.show(currentActivity, message);
        }
    }

    public void messaging_showPendingMessage(Activity activity, Promise promise) {
        showPendingMessage(activity);
        promise.resolve(null);
    }

    public void messaging_setNotDisturbed(final boolean active, Promise promise) {
        Batch.Messaging.setDoNotDisturbEnabled(active);

        promise.resolve(null);
    }

    public void messaging_disableDoNotDisturbAndShowPendingMessage(Activity activity, Promise promise) {
        Batch.Messaging.setDoNotDisturbEnabled(false);
        showPendingMessage(activity);

        promise.resolve(null);
    }

    public void messaging_setTypefaceOverride(@Nullable String normalTypefaceName, @Nullable String boldTypefaceName, Promise promise) {
        AssetManager assetManager = this.reactContext.getAssets();
        @Nullable Typeface normalTypeface = normalTypefaceName != null ? createTypeface(normalTypefaceName, Typeface.NORMAL, assetManager) : null;
        @Nullable Typeface boldTypeface = boldTypefaceName != null ? createTypeface(boldTypefaceName, Typeface.BOLD, assetManager) : null;
        @Nullable Typeface boldTypefaceFallback = boldTypefaceName != null ? createTypeface(boldTypefaceName, Typeface.NORMAL, assetManager) : null;

        Batch.Messaging.setTypefaceOverride(normalTypeface, boldTypeface != null ? boldTypeface : boldTypefaceFallback);

        promise.resolve(null);
    }

    // from https://github.com/facebook/react-native/blob/dc80b2dcb52fadec6a573a9dd1824393f8c29fdc/ReactAndroid/src/main/java/com/facebook/react/views/text/ReactFontManager.java#L118
    // we need to know if the typeface is found so we cannot use it directly :(
    private static final String[] FONT_EXTENSIONS = {"", "_bold", "_italic", "_bold_italic"};
    private static final String[] FONT_FILE_EXTENSIONS = {".ttf", ".otf"};
    private static final String FONTS_ASSET_PATH = "fonts/";

    private static @Nullable
    Typeface createTypeface(
            String fontFamilyName, int style, AssetManager assetManager) {
        String extension = FONT_EXTENSIONS[style];
        for (String fileExtension : FONT_FILE_EXTENSIONS) {
            String fileName =
                    new StringBuilder()
                            .append(FONTS_ASSET_PATH)
                            .append(fontFamilyName)
                            .append(extension)
                            .append(fileExtension)
                            .toString();
            try {
                return Typeface.createFromAsset(assetManager, fileName);
            } catch (RuntimeException e) {
                // unfortunately Typeface.createFromAsset throws an exception instead of returning null
                // if the typeface doesn't exist
            }
        }

        return null;
    }

    // INBOX MODULE

    private BatchInboxFetcher getFetcherFromOptions(final ReadableMap options) {
        if (!options.hasKey("user")) {
            return Batch.Inbox.getFetcher(reactContext);
        }

        final ReadableMap userOptions = options.getMap("user");
        return Batch.Inbox.getFetcher(reactContext, userOptions.getString("identifier"), userOptions.getString("authenticationKey"));
    }

    public void inbox_getFetcher(final ReadableMap options, final Promise promise) {
        if (reactContext == null) {
            promise.reject("InboxError", "No Context");
            return;
        }

        BatchInboxFetcher fetcher = getFetcherFromOptions(options);

        if (options.hasKey("fetchLimit")) {
            fetcher.setFetchLimit(options.getInt("fetchLimit"));
        }

        if (options.hasKey("maxPageSize")) {
            fetcher.setMaxPageSize(options.getInt("maxPageSize"));
        }

        String fetcherIdentifier = UUID.randomUUID().toString();
        this.batchInboxFetcherMap.put(fetcherIdentifier, fetcher);
        promise.resolve(fetcherIdentifier);
    }

    public void inbox_fetcher_destroy(String fetcherIdentifier, final Promise promise) {
        this.batchInboxFetcherMap.remove(fetcherIdentifier);
        promise.resolve(null);
    }

    public void inbox_fetcher_hasMore(String fetcherIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        promise.resolve(fetcher.hasMore());
    }

    public void inbox_fetcher_markAllAsRead(String fetcherIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        this.batchInboxFetcherMap.get(fetcherIdentifier).markAllAsRead();
        promise.resolve(null);
    }

    BatchInboxNotificationContent findNotificationInList(List<BatchInboxNotificationContent> list, String identifier) {
        for (BatchInboxNotificationContent notification : list) {
            if (notification.getNotificationIdentifier().equals(identifier)) {
                return notification;
            }
        }
        return null;
    }

    public void inbox_fetcher_markAsRead(String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        @Nullable BatchInboxNotificationContent notification = findNotificationInList(fetcher.getFetchedNotifications(), notificationIdentifier);

        if (notification == null) {
            promise.reject("InboxError", "NOTIFICATION_NOT_FOUND");
            return;
        }

        fetcher.markAsRead(notification);
        promise.resolve(null);
    }

    public void inbox_fetcher_markAsDeleted(String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        @Nullable BatchInboxNotificationContent notification = findNotificationInList(fetcher.getFetchedNotifications(), notificationIdentifier);

        if (notification == null) {
            promise.reject("InboxError", "NOTIFICATION_NOT_FOUND");
            return;
        }

        fetcher.markAsDeleted(notification);
        promise.resolve(null);
    }

    public void inbox_fetcher_fetchNewNotifications(String fetcherIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        fetcher.fetchNewNotifications(new BatchInboxFetcher.OnNewNotificationsFetchedListener() {
            @Override
            public void onFetchSuccess(@NonNull List<BatchInboxNotificationContent> notifications,
                                       boolean foundNewNotifications,
                                       boolean endReached) {
                WritableArray formattedNotifications = RNBatchInbox.getSuccessResponse(notifications);
                WritableMap results = new WritableNativeMap();
                results.putArray("notifications", formattedNotifications);
                results.putBoolean("foundNewNotifications", foundNewNotifications);
                results.putBoolean("endReached", endReached);
                promise.resolve(results);
            }

            @Override
            public void onFetchFailure(@NonNull String error) {
                promise.reject("InboxFetchError", error);
            }
        });
    }

    public void inbox_fetcher_fetchNextPage(String fetcherIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);

        fetcher.fetchNextPage(new BatchInboxFetcher.OnNextPageFetchedListener() {
            @Override
            public void onFetchSuccess(@NonNull List<BatchInboxNotificationContent> notifications, boolean endReached) {
                WritableArray formattedNotifications = RNBatchInbox.getSuccessResponse(notifications);
                WritableMap results = new WritableNativeMap();
                results.putArray("notifications", formattedNotifications);
                results.putBoolean("endReached", endReached);
                promise.resolve(results);
            }

            @Override
            public void onFetchFailure(@NonNull String error) {
                promise.reject("InboxFetchError", error);
            }
        });
    }

    public void inbox_fetcher_displayLandingMessage(Activity currentActivity, String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        @Nullable BatchInboxNotificationContent notification = findNotificationInList(fetcher.getFetchedNotifications(), notificationIdentifier);

        if (notification == null) {
            promise.reject("InboxError", "NOTIFICATION_NOT_FOUND");
            return;
        }
        notification.displayLandingMessage(currentActivity);
        promise.resolve(null);
    }

    public void inbox_fetcher_setFilterSilentNotifications(String fetcherIdentifier, boolean filterSilentNotifications, Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }
        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        fetcher.setFilterSilentNotifications(filterSilentNotifications);
    }

    // USER MODULE

    public void user_getInstallationId(Promise promise) {
        String userId = Batch.User.getInstallationID();
        promise.resolve(userId);
    }

    public void user_getIdentifier(Promise promise) {
        String userId = Batch.User.getIdentifier(reactContext);
        promise.resolve(userId);
    }

    public void user_getRegion(Promise promise) {
        String region = Batch.User.getRegion(reactContext);
        promise.resolve(region);
    }

    public void user_getLanguage(Promise promise) {
        String language = Batch.User.getLanguage(reactContext);
        promise.resolve(language);
    }

    public void user_getAttributes(final Promise promise) {
        Batch.User.fetchAttributes(reactContext, new BatchAttributesFetchListener() {
            @Override
            public void onSuccess(@NonNull Map<String, BatchUserAttribute> map) {

                WritableMap bridgeAttributes = new WritableNativeMap();

                for (Map.Entry<String, BatchUserAttribute> attributeEntry : map.entrySet()) {
                    WritableMap typedBridgeAttribute = new WritableNativeMap();
                    BatchUserAttribute attribute = attributeEntry.getValue();
                    String type;
                    switch (attribute.type) {
                        case BOOL:
                            type = "b";
                            typedBridgeAttribute.putBoolean("value", (Boolean) attribute.value);
                            break;
                        case DATE: {
                            type = "d";
                            Date dateValue = attribute.getDateValue();
                            if (dateValue == null) {
                                promise.reject(BATCH_BRIDGE_ERROR_CODE, "Fetch attribute: Could not parse date for key: " + attributeEntry.getKey());
                                return;
                            }
                            typedBridgeAttribute.putDouble("value", dateValue.getTime());
                            break;
                        }
                        case STRING:
                            type = "s";
                            typedBridgeAttribute.putString("value", (String) attribute.value);
                            break;
                        case URL:
                            type = "u";
                            URI uriValue = attribute.getUriValue();
                            if (uriValue == null) {
                                promise.reject(BATCH_BRIDGE_ERROR_CODE, "Fetch attribute: Could not parse URI for key: " + attributeEntry.getKey());
                                return;
                            }
                            typedBridgeAttribute.putString("value", uriValue.toString());
                            break;
                        case LONGLONG:
                            type = "i";
                            typedBridgeAttribute.putDouble("value", (long) attribute.value);
                            break;
                        case DOUBLE:
                            type = "f";
                            typedBridgeAttribute.putDouble("value", (double) attribute.value);
                            break;
                        default:
                            promise.reject(BATCH_BRIDGE_ERROR_CODE, "Fetch attribute: Unknown attribute type " + attribute.type + " for key: " + attributeEntry.getKey());
                            return;
                    }
                    typedBridgeAttribute.putString("type", type);
                    bridgeAttributes.putMap(attributeEntry.getKey(), typedBridgeAttribute);
                }
                promise.resolve(bridgeAttributes);
            }

            @Override
            public void onError() {
                promise.reject(BATCH_BRIDGE_ERROR_CODE, "Native SDK fetchAttributes returned an error");
            }
        });
    }

    public void user_getTags(final Promise promise) {
        Batch.User.fetchTagCollections(reactContext, new BatchTagCollectionsFetchListener() {
            @Override
            public void onSuccess(@NonNull Map<String, Set<String>> map) {
                WritableMap bridgeTagCollections = new WritableNativeMap();
                for (Map.Entry<String, Set<String>> tagCollection : map.entrySet()) {
                    WritableArray tags = new WritableNativeArray();
                    for (String tag : tagCollection.getValue()) {
                        tags.pushString(tag);
                    }
                    bridgeTagCollections.putArray(tagCollection.getKey(), tags);
                }
                promise.resolve(bridgeTagCollections);
            }

            @Override
            public void onError() {
                promise.reject(BATCH_BRIDGE_ERROR_CODE,"Native fetchTagCollections returned an error");
            }
        });
    }

    public void user_clearInstallationData() {
        Batch.User.clearInstallationData();
    }

    // PROFILE MODULE

    public void profile_identify(String identifier) {
        Batch.Profile.identify(identifier);
    }

    public void profile_trackEvent(@NonNull String name, @Nullable ReadableMap serializedEventData, @NonNull Promise promise) {
        BatchEventAttributes attributes = RNUtils.convertSerializedEventDataToEventAttributes(serializedEventData);
        if (attributes != null) {
            List<String> errors = attributes.validateEventAttributes();
            if (!errors.isEmpty()) {
                promise.reject(BATCH_BRIDGE_ERROR_CODE, errors.toString());
                return;
            }
        }
        Batch.Profile.trackEvent(name, attributes);
        promise.resolve(null);
    }

    public void profile_trackLocation(ReadableMap serializedLocation) {
        Location nativeLocation = new Location("com.batch.batch_rn");
        nativeLocation.setLatitude(serializedLocation.getDouble("latitude"));
        nativeLocation.setLongitude(serializedLocation.getDouble("longitude"));

        if (serializedLocation.hasKey("precision")) {
            nativeLocation.setAccuracy((float) serializedLocation.getDouble("precision"));
        }

        if (serializedLocation.hasKey("date")) {
            nativeLocation.setTime((long) serializedLocation.getDouble("date"));
        }

        Batch.Profile.trackLocation(nativeLocation);
    }

    public void profile_saveEditor(ReadableArray actions) {
        BatchProfileAttributeEditor editor = Batch.Profile.editor();
        for (int i = 0; i < actions.size(); i++) {
            ReadableMap action = actions.getMap(i);
            String type = action.getString("type");

            if (type.equals("setAttribute")) {
                String key = action.getString("key");
                ReadableType valueType = action.getType("value");
                switch (valueType) {
                    case Null:
                        editor.removeAttribute(key);
                        break;
                    case Boolean:
                        editor.setAttribute(key, action.getBoolean("value"));
                        break;
                    case Number:
                        editor.setAttribute(key, action.getDouble("value"));
                        break;
                    case String:
                        editor.setAttribute(key, action.getString("value"));
                        break;
                    case Array:
                        ReadableArray array = action.getArray("value");
                        editor.setAttribute(key, RNUtils.convertReadableArrayToList(array));
                }
            } else if (type.equals("setDateAttribute")) {
                String key = action.getString("key");
                long timestamp = (long) action.getDouble("value");
                Date date = new Date(timestamp);
                editor.setAttribute(key, date);
            } else if (type.equals("setURLAttribute")) {
                String key = action.getString("key");
                String url = action.getString("value");
                editor.setAttribute(key, URI.create(url));
            } else if (type.equals("removeAttribute")) {
                String key = action.getString("key");
                editor.removeAttribute(key);
            } else if (type.equals("setEmailAddress")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setEmailAddress(null);
                } else {
                    String value = action.getString("value");
                    editor.setEmailAddress(value);
                }
            } else if (type.equals("setEmailMarketingSubscription")) {
                String value = action.getString("value");
                editor.setEmailMarketingSubscription(BatchEmailSubscriptionState.valueOf(value));
            } else if (type.equals("setPhoneNumber")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setPhoneNumber(null);
                } else {
                    String value = action.getString("value");
                    editor.setPhoneNumber(value);
                }
            } else if (type.equals("setSMSMarketingSubscription")) {
                String value = action.getString("value");
                editor.setSMSMarketingSubscription(BatchSMSSubscriptionState.valueOf(value));
            } else if (type.equals("setLanguage")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setLanguage(null);
                } else {
                    String value = action.getString("value");
                    editor.setLanguage(value);
                }
            } else if (type.equals("setRegion")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setRegion(null);
                } else {
                    String value = action.getString("value");
                    editor.setRegion(value);
                }
            } else if (type.equals("addToArray")) {
                String key = action.getString("key");
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.String)) {
                    String value = action.getString("value");
                    editor.addToArray(key, value);
                } else if(valueType.equals(ReadableType.Array)) {
                    ReadableArray arrayValue = action.getArray("value");
                    editor.addToArray(key, RNUtils.convertReadableArrayToList(arrayValue));
                }
            } else if (type.equals("removeFromArray")) {
                String key = action.getString("key");
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.String)) {
                    String value = action.getString("value");
                    editor.removeFromArray(key, value);
                } else if(valueType.equals(ReadableType.Array)) {
                    ReadableArray arrayValue = action.getArray("value");
                    editor.removeFromArray(key, RNUtils.convertReadableArrayToList(arrayValue));
                }
            }
        }
        editor.save();
    }
}
