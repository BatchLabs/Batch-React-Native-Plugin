package com.batch.batch_rn;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.Batch;
import com.batch.android.BatchActivityLifecycleHelper;
import com.batch.android.BatchAttributesFetchListener;
import com.batch.android.BatchPushRegistration;
import com.batch.android.BatchTagCollectionsFetchListener;
import com.batch.android.BatchEmailSubscriptionState;
import com.batch.android.BatchUserAttribute;
import com.batch.android.PushNotificationType;
import com.batch.android.BatchInboxFetcher;
import com.batch.android.BatchInboxNotificationContent;
import com.batch.android.BatchMessage;
import com.batch.android.BatchUserDataEditor;
import com.batch.android.json.JSONObject;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
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

public class RNBatchModule extends ReactContextBaseJavaModule {

    private static final String NAME = "RNBatch";
    private static final String PLUGIN_VERSION_ENVIRONMENT_VARIABLE = "batch.plugin.version";
    private static final String PLUGIN_VERSION = "ReactNative/8.2.0";

    public static final String LOGGER_TAG = "RNBatchBridge";

    private static final String BATCH_BRIDGE_ERROR_CODE = "BATCH_BRIDGE_ERROR";

    private final ReactApplicationContext reactContext;

    private final Map<String, BatchInboxFetcher> batchInboxFetcherMap;

    private static final RNBatchEventDispatcher eventDispatcher = new RNBatchEventDispatcher();

    static {
        System.setProperty("batch.plugin.version", PLUGIN_VERSION);
    }

    // REACT NATIVE PLUGIN SETUP

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        // Add push notification types
        final Map<String, Object> notificationTypes = new HashMap<>();
        for (PushNotificationType type : PushNotificationType.values()) {
            notificationTypes.put(type.name(), type.ordinal());
        }
        constants.put("NOTIFICATION_TYPES", notificationTypes);

        return constants;
    }

    private static boolean isInitialized = false;

    public static void initialize(Application application) {
        if (!isInitialized) {
            Resources resources = application.getResources();
            String packageName = application.getPackageName();
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

    public RNBatchModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.batchInboxFetcherMap = new HashMap<>();
        eventDispatcher.setReactContext(reactContext);
    }

    public void start() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        Batch.onStart(activity);
    }

    // BASE MODULE

    @ReactMethod
    public void optIn(Promise promise) {
        Batch.optIn(reactContext);
        start();
        promise.resolve(null);
    }

    @ReactMethod
    public void optOut(Promise promise) {
        Batch.optOut(reactContext);
        promise.resolve(null);
    }

    @ReactMethod
    public void optOutAndWipeData(Promise promise) {
        Batch.optOutAndWipeData(reactContext);
        promise.resolve(null);
    }


    @ReactMethod
    public void addListener(String eventName) {
        eventDispatcher.setHasListener(true);
    }

    @ReactMethod
    public void removeListeners(double count) {
    }

    // PUSH MODULE

    @ReactMethod
    public void push_registerForRemoteNotifications() { /* No effect on android */ }

    @ReactMethod
    public void push_setNotificationTypes(Integer notifType) {
        EnumSet<PushNotificationType> pushTypes = PushNotificationType.fromValue(notifType);
        Batch.Push.setNotificationsType(pushTypes);
    }

    @ReactMethod
    public void push_clearBadge() { /* No effect on android */ }

    @ReactMethod
    public void push_dismissNotifications() { /* No effect on android */ }

    @ReactMethod
    public void push_getLastKnownPushToken(Promise promise) {
        BatchPushRegistration registration = Batch.Push.getRegistration();
        promise.resolve(registration != null ? registration.getToken() : null);
    }

    @ReactMethod
    public void push_requestNotificationAuthorization() {
        Batch.Push.requestNotificationPermission(reactContext);
    }

    // MESSAGING MODULE

    private void showPendingMessage() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        BatchMessage message = Batch.Messaging.popPendingMessage();
        if (message != null) {
            Batch.Messaging.show(activity, message);
        }
    }

    @ReactMethod
    public void messaging_showPendingMessage(Promise promise) {
        showPendingMessage();

        promise.resolve(null);
    }

    @ReactMethod
    public void messaging_setNotDisturbed(final boolean active, Promise promise) {
        Batch.Messaging.setDoNotDisturbEnabled(active);

        promise.resolve(null);
    }

    @ReactMethod
    public void messaging_disableDoNotDisturbAndShowPendingMessage(Promise promise) {
        Batch.Messaging.setDoNotDisturbEnabled(false);
        showPendingMessage();

        promise.resolve(null);
    }

    @ReactMethod
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


    // DEBUG MODULE

    @ReactMethod
    public void debug_startDebugActivity() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            Batch.Debug.startDebugActivity(currentActivity);
        }
    }


    // INBOX MODULE

    private BatchInboxFetcher getFetcherFromOptions(final ReadableMap options, Activity activity) {
        if (!options.hasKey("user")) {
            return Batch.Inbox.getFetcher(activity);
        }

        final ReadableMap userOptions = options.getMap("user");
        return Batch.Inbox.getFetcher(activity, userOptions.getString("identifier"), userOptions.getString("authenticationKey"));
    }

    @ReactMethod
    public void inbox_getFetcher(final ReadableMap options, final Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("InboxError", "NO_ACTIVITY");
            return;
        }

        BatchInboxFetcher fetcher = getFetcherFromOptions(options, activity);

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

    @ReactMethod
    public void inbox_fetcher_destroy(String fetcherIdentifier, final Promise promise) {
        this.batchInboxFetcherMap.remove(fetcherIdentifier);
        promise.resolve(null);
    }

    @ReactMethod
    public void inbox_fetcher_hasMore(String fetcherIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        BatchInboxFetcher fetcher = this.batchInboxFetcherMap.get(fetcherIdentifier);
        promise.resolve(fetcher.hasMore());
    }

    @ReactMethod
    public void inbox_fetcher_markAllAsRead(String fetcherIdentifier, final Promise promise) {
        if (!this.batchInboxFetcherMap.containsKey(fetcherIdentifier)) {
            promise.reject("InboxError", "FETCHER_NOT_FOUND");
            return;
        }

        this.batchInboxFetcherMap.get(fetcherIdentifier).markAllAsRead();
        promise.resolve(null);
    }

    private @Nullable
    BatchInboxNotificationContent findNotificationInList(List<BatchInboxNotificationContent> list, String identifier) {
        for (BatchInboxNotificationContent notification : list) {
            if (notification.getNotificationIdentifier().equals(identifier)) {
                return notification;
            }
        }
        return null;
    }

    @ReactMethod
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

    @ReactMethod
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

    @ReactMethod
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

    @ReactMethod
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

    @ReactMethod
    public void inbox_fetcher_displayLandingMessage(String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
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
        notification.displayLandingMessage(reactContext);
        promise.resolve(null);
    }

    // USER DATA EDITOR MODULE

    @ReactMethod
    public void userData_getInstallationId(Promise promise) {
        String userId = Batch.User.getInstallationID();
        promise.resolve(userId);
    }

    @ReactMethod
    public void userData_getIdentifier(Promise promise) {
        String userId = Batch.User.getIdentifier(reactContext);
        promise.resolve(userId);
    }

    @ReactMethod
    public void userData_getRegion(Promise promise) {
        String region = Batch.User.getRegion(reactContext);
        promise.resolve(region);
    }

    @ReactMethod
    public void userData_getLanguage(Promise promise) {
        String language = Batch.User.getLanguage(reactContext);
        promise.resolve(language);
    }

    @ReactMethod
    public void userData_getAttributes(final Promise promise) {
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

    @ReactMethod
    public void userData_getTags(final Promise promise) {
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

    @ReactMethod
    public void userData_save(ReadableArray actions) {
        BatchUserDataEditor editor = Batch.User.editor();
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
            } else if (type.equals("clearAttributes")) {
                editor.clearAttributes();
            } else if (type.equals("setIdentifier")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setIdentifier(null);
                } else {
                    String value = action.getString("value");
                    editor.setIdentifier(value);
                }
            }  else if (type.equals("setEmail")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setEmail(null);
                } else {
                    String value = action.getString("value");
                    editor.setEmail(value);
                }
            } else if (type.equals("setEmailMarketingSubscriptionState")) {
                String value = action.getString("value");
                editor.setEmailMarketingSubscriptionState(BatchEmailSubscriptionState.valueOf(value));
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
            } else if (type.equals("setAttributionId")) {
                ReadableType valueType = action.getType("value");
                if (valueType.equals(ReadableType.Null)) {
                    editor.setAttributionIdentifier(null);
                } else {
                    String value = action.getString("value");
                    editor.setAttributionIdentifier(value);
                }
            } else if (type.equals("addTag")) {
                String collection = action.getString("collection");
                String tag = action.getString("tag");
                editor.addTag(collection, tag);
            } else if (type.equals("removeTag")) {
                String collection = action.getString("collection");
                String tag = action.getString("tag");
                editor.removeTag(collection, tag);
            } else if (type.equals("clearTagCollection")) {
                String collection = action.getString("collection");
                editor.clearTagCollection(collection);
            } else if (type.equals("clearTags")) {
                editor.clearTags();
            }
        }
        editor.save();
    }

    @ReactMethod
    public void userData_trackEvent(String name, String label, ReadableMap serializedEventData) {
        Batch.User.trackEvent(name, label, RNUtils.convertSerializedEventDataToEventData(serializedEventData));
    }

    @ReactMethod
    public void userData_trackTransaction(double amount, ReadableMap data) {
        JSONObject transactionData = null;
        if (data != null) {
            transactionData = new JSONObject(data.toHashMap());
        }
        Batch.User.trackTransaction(amount, transactionData);
    }

    @ReactMethod
    public void userData_trackLocation(ReadableMap serializedLocation) {
        Location nativeLocation = new Location("com.batch.batch_rn");
        nativeLocation.setLatitude(serializedLocation.getDouble("latitude"));
        nativeLocation.setLongitude(serializedLocation.getDouble("longitude"));

        if (serializedLocation.hasKey("precision")) {
            nativeLocation.setAccuracy((float) serializedLocation.getDouble("precision"));
        }

        if (serializedLocation.hasKey("date")) {
            nativeLocation.setTime((long) serializedLocation.getDouble("date"));
        }

        Batch.User.trackLocation(nativeLocation);
    }
}
