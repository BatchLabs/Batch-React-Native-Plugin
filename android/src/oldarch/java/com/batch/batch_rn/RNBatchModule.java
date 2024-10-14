package com.batch.batch_rn;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.Map;


public class RNBatchModule extends ReactContextBaseJavaModule {

    private final RNBatchModuleImpl impl;

    static {
        System.setProperty("batch.plugin.version", RNBatchModuleImpl.PLUGIN_VERSION);
    }

    @Override
    public String getName() {
        return RNBatchModuleImpl.NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        return RNBatchModuleImpl.getConstants();
    }

    public static void initialize(Application application) {
        Log.d("RNBatchBridge","Init Batch Native Module");
        RNBatchModuleImpl.initialize(application);
    }

    private static void setDefaultProfileMigrations(Context context, String packageName) {
        RNBatchModuleImpl.setDefaultProfileMigrations(context, packageName);
    }


    public RNBatchModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.impl = new RNBatchModuleImpl(reactContext);
    }

    public void start() {
        impl.start(getCurrentActivity());
    }

    // BASE MODULE

    @ReactMethod
    public void optIn(Promise promise) {
        impl.optIn(getCurrentActivity(), promise);
    }

    @ReactMethod
    public void optOut(Promise promise) {
        impl.optOut(promise);
    }

    @ReactMethod
    public void optOutAndWipeData(Promise promise) {
        impl.optOutAndWipeData(promise);
    }

    @ReactMethod
    public void isOptedOut(Promise promise) {
        impl.isOptedOut(promise);
    }

    @ReactMethod
    public void updateAutomaticDataCollection(@NonNull ReadableMap dataCollectionConfig) {
        impl.updateAutomaticDataCollection(dataCollectionConfig);
    }

    @ReactMethod
    public void addListener(String eventName) {
        impl.addListener(eventName);
    }

    @ReactMethod
    public void removeListeners(double count) {
        impl.removeListeners(count);
    }

    // PUSH MODULE
    @ReactMethod
    public void push_setNotificationTypes(Integer notifType) {
        impl.push_setNotificationTypes(notifType);
    }

    @ReactMethod
    public void push_clearBadge() { /* No effect on android */ }

    @ReactMethod
    public void push_dismissNotifications() { /* No effect on android */ }

    @ReactMethod
    public void push_refreshToken() { /* No effect on android */ }

    @ReactMethod
    public void push_setShowForegroundNotification(boolean enabled) { /* No effect on android */ }

    @ReactMethod
    public void push_getLastKnownPushToken(Promise promise) {
        impl.push_getLastKnownPushToken(promise);
    }

    @ReactMethod
    public void push_requestNotificationAuthorization() {
        impl.push_requestNotificationAuthorization();
    }

    @ReactMethod
    public void push_requestProvisionalNotificationAuthorization() {
        /* No effect on android */
    }

    @ReactMethod
    public void push_getInitialDeeplink(Promise promise) {
        /* No effect on android */
    }

    // MESSAGING MODULE

    @ReactMethod
    public void messaging_showPendingMessage(Promise promise) {
        impl.messaging_showPendingMessage(getCurrentActivity(), promise);
    }

    @ReactMethod
    public void messaging_setNotDisturbed(final boolean active, Promise promise) {
        impl.messaging_setNotDisturbed(active, promise);
    }

    @ReactMethod
    public void messaging_disableDoNotDisturbAndShowPendingMessage(Promise promise) {
        impl.messaging_disableDoNotDisturbAndShowPendingMessage(getCurrentActivity(), promise);
    }

    @ReactMethod
    public void messaging_setTypefaceOverride(@Nullable String normalTypefaceName, @Nullable String boldTypefaceName, @Nullable String italicFontName, @Nullable String italicBoldFontName, Promise promise) {
        impl.messaging_setTypefaceOverride(normalTypefaceName, boldTypefaceName, promise);
    }

    // DEBUG MODULE
    @ReactMethod
    public void showDebugView() {
        impl.showDebugView(getCurrentActivity());
    }

    // INBOX MODULE
    @ReactMethod
    public void inbox_getFetcher(final ReadableMap options, final Promise promise) {
        impl.inbox_getFetcher(options, promise);
    }

    @ReactMethod
    public void inbox_fetcher_destroy(String fetcherIdentifier, final Promise promise) {
        impl.inbox_fetcher_destroy(fetcherIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_hasMore(String fetcherIdentifier, final Promise promise) {
        impl.inbox_fetcher_hasMore(fetcherIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_markAllAsRead(String fetcherIdentifier, final Promise promise) {
        impl.inbox_fetcher_markAllAsRead(fetcherIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_markAsRead(String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
        impl.inbox_fetcher_markAsRead(fetcherIdentifier, notificationIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_markAsDeleted(String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
        impl.inbox_fetcher_markAsDeleted(fetcherIdentifier, notificationIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_fetchNewNotifications(String fetcherIdentifier, final Promise promise) {
        impl.inbox_fetcher_fetchNewNotifications(fetcherIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_fetchNextPage(String fetcherIdentifier, final Promise promise) {
        impl.inbox_fetcher_fetchNextPage(fetcherIdentifier, promise);
    }

    @ReactMethod
    public void inbox_fetcher_displayLandingMessage(String fetcherIdentifier, String notificationIdentifier, final Promise promise) {
        impl.inbox_fetcher_displayLandingMessage(fetcherIdentifier, notificationIdentifier, promise);
    }

    // USER MODULE

    @ReactMethod
    public void user_getInstallationId(Promise promise) {
        impl.user_getInstallationId(promise);
    }

    @ReactMethod
    public void user_getIdentifier(Promise promise) {
        impl.user_getIdentifier(promise);
    }

    @ReactMethod
    public void user_getRegion(Promise promise) {
        impl.user_getRegion(promise);
    }

    @ReactMethod
    public void user_getLanguage(Promise promise) {
        impl.user_getLanguage(promise);
    }

    @ReactMethod
    public void user_getAttributes(final Promise promise) {
        impl.user_getAttributes(promise);
    }

    @ReactMethod
    public void user_getTags(final Promise promise) {
        impl.user_getTags(promise);
    }

    @ReactMethod
    public void profile_saveEditor(ReadableArray actions) {
        impl.profile_saveEditor(actions);
    }

    @ReactMethod
    public void user_clearInstallationData() {
        impl.user_clearInstallationData();
    }

    // PROFILE MODULE

    @ReactMethod
    public void profile_identify(String identifier) {
        impl.profile_identify(identifier);
    }

    @ReactMethod
    public void profile_trackEvent(@NonNull String name, @Nullable ReadableMap serializedEventData, @NonNull Promise promise) {
        impl.profile_trackEvent(name, serializedEventData, promise);
    }

    @ReactMethod
    public void profile_trackLocation(ReadableMap serializedLocation) {
        impl.profile_trackLocation(serializedLocation);
    }
}
