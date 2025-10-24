package com.batch.batch_rn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.Map;

public class RNBatchModule extends NativeRNBatchModuleSpec {

    private final RNBatchModuleImpl impl;

    @NonNull
    @Override
    public String getName() {
        return RNBatchModuleImpl.NAME;
    }

    @Override
    public Map<String, Object> getTypedExportedConstants() {
        return RNBatchModuleImpl.getConstants();
    }

    public RNBatchModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.impl = new RNBatchModuleImpl(reactContext);
    }

    public void start() {
        impl.start(getReactApplicationContext().getCurrentActivity());
    }

    // BASE MODULE

    @Override
    public void optIn(Promise promise) {
        impl.optIn(getReactApplicationContext().getCurrentActivity(), promise);
    }

    @Override
    public void optOut(Promise promise) {
        impl.optOut(promise);
    }

    @Override
    public void optOutAndWipeData(Promise promise) {
        impl.optOutAndWipeData(promise);
    }

    @Override
    public void isOptedOut(Promise promise) {
        impl.isOptedOut(promise);
    }

    @Override
    public void updateAutomaticDataCollection(@NonNull ReadableMap dataCollection) {
        impl.updateAutomaticDataCollection(dataCollection);
    }

    @Override
    public void showDebugView() {
        impl.showDebugView(getReactApplicationContext().getCurrentActivity());
    }

    @Override
    public void addListener(String eventName) {
        impl.addListener(eventName);
    }

    @Override
    public void removeListeners(double count) {
        impl.removeListeners(count);
    }

    // PUSH MODULE

    @Override
    public void push_setShowNotifications(boolean enabled) {
        impl.push_setShowNotifications(enabled);
    }

    @Override
    public void push_shouldShowNotifications(Promise promise) {
        impl.push_shouldShowNotifications(promise);
    }

    @Override
    public void push_clearBadge() { /* No effect on android */ }

    @Override
    public void push_dismissNotifications() { /* No effect on android */ }

    @Override
    public void push_refreshToken() { /* No effect on android */ }

    @Override
    public void push_getLastKnownPushToken(Promise promise) {
        impl.push_getLastKnownPushToken(promise);
    }

    @Override
    public void push_requestNotificationAuthorization() {
        impl.push_requestNotificationAuthorization();
    }

    @Override
    public void push_requestProvisionalNotificationAuthorization() {
        /* No effect on android */
    }

    @Override
    public void push_getInitialDeeplink(Promise promise) {
        /* No effect on android */
    }

    @Override
    public void push_setShowForegroundNotification(boolean enabled) { /* No effect on android */ }

    // MESSAGING MODULE

    @Override
    public void messaging_showPendingMessage(Promise promise) {
        impl.messaging_showPendingMessage(getReactApplicationContext().getCurrentActivity(), promise);
    }

    @Override
    public void messaging_setNotDisturbed(boolean active, Promise promise) {
        impl.messaging_setNotDisturbed(active, promise);
    }

    @Override
    public void messaging_disableDoNotDisturbAndShowPendingMessage(Promise promise) {
        impl.messaging_disableDoNotDisturbAndShowPendingMessage(getReactApplicationContext().getCurrentActivity(), promise);
    }

    @Override
    public void messaging_setFontOverride(@Nullable String normalTypefaceName, @Nullable String boldTypefaceName, @Nullable String italicFontName, @Nullable String italicBoldFontName, Promise promise) {
        impl.messaging_setTypefaceOverride(normalTypefaceName, boldTypefaceName, promise);
    }

    // INBOX MODULE

    @Override
    public void inbox_getFetcher(ReadableMap options, Promise promise) {
        impl.inbox_getFetcher(options, promise);
    }

    @Override
    public void inbox_fetcher_destroy(String fetcherIdentifier, Promise promise) {
        impl.inbox_fetcher_destroy(fetcherIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_hasMore(String fetcherIdentifier, Promise promise) {
        impl.inbox_fetcher_hasMore(fetcherIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_markAllAsRead(String fetcherIdentifier, Promise promise) {
        impl.inbox_fetcher_markAllAsRead(fetcherIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_markAsRead(String fetcherIdentifier, String notificationIdentifier, Promise promise) {
        impl.inbox_fetcher_markAsRead(fetcherIdentifier, notificationIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_markAsDeleted(String fetcherIdentifier, String notificationIdentifier, Promise promise) {
        impl.inbox_fetcher_markAsDeleted(fetcherIdentifier, notificationIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_fetchNewNotifications(String fetcherIdentifier, Promise promise) {
        impl.inbox_fetcher_fetchNewNotifications(fetcherIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_fetchNextPage(String fetcherIdentifier, Promise promise) {
        impl.inbox_fetcher_fetchNextPage(fetcherIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_displayLandingMessage(String fetcherIdentifier, String notificationIdentifier, Promise promise) {
        impl.inbox_fetcher_displayLandingMessage(getReactApplicationContext().getCurrentActivity(), fetcherIdentifier, notificationIdentifier, promise);
    }

    @Override
    public void inbox_fetcher_setFilterSilentNotifications(String fetcherIdentifier, boolean filterSilentNotifications, Promise promise) {
        impl.inbox_fetcher_setFilterSilentNotifications(fetcherIdentifier, filterSilentNotifications, promise);
    }

    // USER MODULE

    @Override
    public void user_getInstallationId(Promise promise) {
        impl.user_getInstallationId(promise);
    }

    @Override
    public void user_getIdentifier(Promise promise) {
        impl.user_getIdentifier(promise);
    }

    @Override
    public void user_getRegion(Promise promise) {
        impl.user_getRegion(promise);
    }

    @Override
    public void user_getLanguage(Promise promise) {
        impl.user_getLanguage(promise);
    }

    @Override
    public void user_getAttributes(Promise promise) {
        impl.user_getAttributes(promise);
    }

    @Override
    public void user_getTags(Promise promise) {
        impl.user_getTags(promise);
    }

    @Override
    public void profile_saveEditor(ReadableArray actions) {
        impl.profile_saveEditor(actions);
    }

    @Override
    public void user_clearInstallationData() {
        impl.user_clearInstallationData();
    }

    // PROFILE MODULE

    @Override
    public void profile_identify(String identifier) {
        impl.profile_identify(identifier);
    }

    @Override
    public void profile_trackEvent(@NonNull String name, @Nullable ReadableMap serializedEventData, @NonNull Promise promise) {
        impl.profile_trackEvent(name, serializedEventData, promise);
    }

    @Override
    public void profile_trackLocation(ReadableMap serializedLocation) {
        impl.profile_trackLocation(serializedLocation);
    }
}
