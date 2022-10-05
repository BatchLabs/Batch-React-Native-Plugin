package com.batch.batch_rn;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;
import com.batch.android.BatchPushPayload;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.LinkedList;

/**
 * Bridge class to handle events dispatched by the Batch native SDK
 */
public class RNBatchEventDispatcher implements BatchEventDispatcher {

    /**
     * React Context
     */
    @Nullable
    private ReactApplicationContext reactContext;

    /**
     * Event Queue
     *
     * We need to queue events because Batch SDK is started before
     * we have react context catalyst instance ready.
     */
    @NonNull
    private final LinkedList<RNBatchEvent> events = new LinkedList<>();

    /**
     * Send event to the JS bridge
     * @param event dispatched event
     */
    private void sendEvent(@NonNull RNBatchEvent event) {
        if (reactContext == null || !reactContext.hasActiveCatalystInstance()) {
            return;
        }
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event.getName(), event.getParams());
    }

    /**
     * Batch event dispatcher callback
     * @param type event type
     * @param payload event payload
     */
    @Override
    public void dispatchEvent(@NonNull Batch.EventDispatcher.Type type,
                              @NonNull Batch.EventDispatcher.Payload payload) {
        String eventName = this.mapBatchEventDispatcherTypeToRNEvent(type);
        if (eventName != null) {
            WritableMap params = Arguments.createMap();
            params.putBoolean("isPositiveAction", payload.isPositiveAction());
            params.putString("deeplink", payload.getDeeplink());
            params.putString("trackingId", payload.getTrackingId());
            params.putString("webViewAnalyticsIdentifier", payload.getWebViewAnalyticsID());

            BatchPushPayload pushPayload = payload.getPushPayload();
            if (pushPayload != null) {
                params.putMap("pushPayload", Arguments.fromBundle(pushPayload.getPushBundle()));
            }

            RNBatchEvent event = new RNBatchEvent(eventName, params);
            synchronized (events) {
                events.add(event);
            }

            if (reactContext == null || !reactContext.hasActiveCatalystInstance()) {
                Log.d(RNBatchModule.LOGGER_TAG,
                        "React context is null or has no active catalyst instance. Queuing event.");
                return;
            }
            dequeueEvents();
        }
    }

    /**
     * Dequeue the stored events
     */
    public void dequeueEvents() {
        if (events.isEmpty()) {
            return;
        }
        synchronized(events) {
            sendEvent(events.pop());
        }
        dequeueEvents();
    }

    /**
     * Set the react context instance
     *
     * Note: We are using a LifecycleEventListener to be notified when the react context
     * has a catalyst instance ready
     * @param reactContext context
     */
    public void setReactContext(final ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        this.reactContext.addLifecycleEventListener(new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                // No we should have a catalyst instance ready
                if (reactContext.hasActiveCatalystInstance()) {
                    dequeueEvents();
                    reactContext.removeLifecycleEventListener(this);
                }
            }

            @Override
            public void onHostPause() {
                // do noting
            }

            @Override
            public void onHostDestroy() {
                // do noting
            }
        });
    }

    /**
     * Helper method to map native batch event with react-native event
     * @param type event type
     * @return the event emitted through the JS bridge
     */
    private @Nullable
    String mapBatchEventDispatcherTypeToRNEvent(@NonNull Batch.EventDispatcher.Type type) {
        switch (type) {
            case MESSAGING_SHOW:
                return "messaging_show";
            case MESSAGING_CLICK:
                return "messaging_click";
            case MESSAGING_CLOSE:
                return "messaging_close";
            case MESSAGING_AUTO_CLOSE:
                return "messaging_auto_close";
            case MESSAGING_CLOSE_ERROR:
                return "messaging_close_error";
            case MESSAGING_WEBVIEW_CLICK:
                return "messaging_webview_click";
            case NOTIFICATION_OPEN:
                return "notification_open";
            case NOTIFICATION_DISMISS:
                return "notification_dismiss";
            case NOTIFICATION_DISPLAY:
                return "notification_display";
            default:
                return null;
        }
    }
}
