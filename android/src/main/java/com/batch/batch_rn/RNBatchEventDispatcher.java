package com.batch.batch_rn;

import android.util.Log;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;
import com.batch.android.BatchMessage;
import com.batch.android.BatchPushPayload;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * Bridge class to handle events dispatched by the Batch native SDK
 */
public class RNBatchEventDispatcher implements BatchEventDispatcher {


    /**
     * Max number of events we keep in the event queue
     */
    private static final int MAX_QUEUE_SIZE = 5;

    /**
     * React Context
     */
    @Nullable
    private ReactApplicationContext reactContext;

    /**
     * Event Queue
     * <p>
     * We need to queue events because Batch SDK is started before
     * we have react context catalyst instance ready.
     */
    @NonNull
    private final LinkedList<RNBatchEvent> events = new LinkedList<>();

    /**
     * Whether we have a listener registered
     * Default: false
     */
    private boolean hasListener;

    /**
     * Send event to the JS bridge
     * @param event dispatched event
     */
    private void sendEvent(@NonNull RNBatchEvent event) {
        if (reactContext == null || !reactContext.hasActiveReactInstance()) {
            Log.d(RNBatchModule.LOGGER_TAG,
                    "Trying to send an event while react context is null" +
                            " or has no active react instance. Aborting.");
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

            BatchMessage messagingPayload = payload.getMessagingPayload();
            if (messagingPayload != null) {
                Bundle bundle = new Bundle();
                messagingPayload.writeToBundle(bundle);
                Bundle messagingBundle =  bundle.getBundle(BatchMessage.MESSAGING_EXTRA_PAYLOAD_KEY);
                if (messagingBundle != null) {
                    Bundle dataBundle = messagingBundle.getBundle("data");
                    if(dataBundle != null) {
                        Bundle landingPushPayload = dataBundle.getBundle("batchPushPayload");
                        if (landingPushPayload != null) {
                            params.putMap("pushPayload", Arguments.fromBundle(landingPushPayload));
                        }
                        String customPayload =  dataBundle.getString("custom_payload");
                        if(customPayload != null) {
                            try {
                                JSONObject customPayloadJSON = new JSONObject(customPayload);
                                params.putMap("messagingCustomPayload", RNUtils.convertJSONObjectToWritableMap(customPayloadJSON));
                            } catch (JSONException e) {
                                Log.d(RNBatchModule.LOGGER_TAG,"Failed to parse messaging custom payload");
                            }
                        }
                    }
                }
            }
            RNBatchEvent event = new RNBatchEvent(eventName, params);
            if (!isModuleReady() || !hasListener) {
                Log.d(RNBatchModule.LOGGER_TAG,
                        "Module is not ready or no listener registered yet. Queuing event: ".concat(eventName));
                queueEvent(event);
                return;
            }
            sendEvent(event);
        }
    }

    /**
     * Dequeue the stored events
     */
    private void dequeueEvents() {
        synchronized(events) {
            if (events.isEmpty()) {
                return;
            }
            while(!events.isEmpty()) {
                sendEvent(events.pop());
            }
        }
    }

    /**
     * Put event in queue
     * @param event event to queue
     */
    private void queueEvent(RNBatchEvent event) {
        synchronized (events) {
            if(events.size() >= MAX_QUEUE_SIZE) {
                events.clear();
            }
            events.add(event);
        }
    }

    /**
     * Set the react context instance used to emit event
     * @param reactContext context
     */
    public void setReactContext(@NonNull ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    /**
     * Simple method to know if module is ready
     * Meaning we have a react context and a catalyst instance ready
     * @return true if ready
     */
    private boolean isModuleReady() {
        return reactContext != null && reactContext.hasReactInstance();
    }

    /**
     * Indicate we have at least one registered listener
     * @param hasListener if we have a listener registered
     */
    public void setHasListener(boolean hasListener) {
        if(!this.hasListener && hasListener) {
            this.dequeueEvents();
        }
        this.hasListener = hasListener;
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
