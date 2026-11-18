package com.batch.batch_rn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import com.batch.android.Batch;
import com.batch.android.BatchPushPayload;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/** Passive FCM receipt observer. Batch still owns delivery and presentation. */
final class RNBatchPushReceivedReceiver extends BroadcastReceiver {
    private final ReactApplicationContext reactContext;
    private final Context context;
    private volatile boolean registered;

    RNBatchPushReceivedReceiver(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        this.context = reactContext.getApplicationContext();
    }

    /** Observe only while the React Native module is alive; never start a headless runtime. */
    synchronized void register() {
        if (registered) return;
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        filter.addCategory(context.getPackageName());
        // Play services is a different UID. Restrict the exported receiver to its SEND permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, "com.google.android.c2dm.permission.SEND", null,
                    Context.RECEIVER_EXPORTED);
        } else {
            context.registerReceiver(this, filter, "com.google.android.c2dm.permission.SEND", null);
        }
        registered = true;
    }

    /** Release the observer when React Native invalidates the module. */
    synchronized void unregister() {
        if (!registered) return;
        registered = false;
        context.unregisterReceiver(this);
    }

    /** Emit a live hint only; do not queue payloads across sessions or consume the broadcast. */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!registered || !reactContext.hasActiveReactInstance() || Batch.isOptedOut(context)) return;
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        BatchPushPayload payload;
        try {
            payload = BatchPushPayload.payloadFromReceiverExtras(extras);
        } catch (BatchPushPayload.ParsingException ignored) {
            // Not a valid Batch push. Other providers still receive the broadcast.
            return;
        }
        WritableMap body = Arguments.createMap();
        body.putBoolean("isPositiveAction", false);
        body.putMap("pushPayload", Arguments.fromBundle(payload.getPushBundle()));
        body.putString("deeplink", payload.getDeeplink());
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("notification_receive", body);
    }
}
