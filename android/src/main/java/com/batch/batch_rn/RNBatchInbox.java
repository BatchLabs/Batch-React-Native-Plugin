package com.batch.batch_rn;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.batch.android.BatchInboxNotificationContent;
import com.batch.android.BatchPushPayload;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

public class RNBatchInbox {
    final private static String TAG = "BatchRNPluginInbox";

    protected static WritableArray getSuccessResponse(List<BatchInboxNotificationContent> notifications, Context context)
    {
        final WritableArray rnNotifications = new WritableNativeArray();
        for (BatchInboxNotificationContent notification : notifications) {
            rnNotifications.pushMap(getWritableMapNotification(notification, context));
        }

        return rnNotifications;
    }

    protected static JSONObject getErrorResponse(String reason)
    {
        try {
            final JSONObject json = new JSONObject();
            json.put("error", reason);
            return json;
        } catch (JSONException e) {
            Log.d(TAG, "Could not convert error", e);
            try {
                return new JSONObject().put("error", "Internal native error (-200)");
            } catch (JSONException error) {
                // Used to prevent above message to require JSONException handling
                return new JSONObject();
            }
        }
    }

    private static WritableMap getWritableMapNotification(BatchInboxNotificationContent notification, Context context)
    {
        final WritableMap output = new WritableNativeMap();

        output.putString("identifier", notification.getNotificationIdentifier());
        output.putString("body", notification.getBody());
        output.putBoolean("isUnread", notification.isUnread());
        output.putBoolean("isSilent", notification.isSilent());
        output.putDouble("date", notification.getDate().getTime());

        int source = 0; // UNKNOWN
        switch (notification.getSource()) {
            case CAMPAIGN:
                source = 1;
                break;
            case TRANSACTIONAL:
                source = 2;
                break;
        }
        output.putInt("source", source);

        final String title = notification.getTitle();
        if (title != null) {
            output.putString("title", title);
        }

        output.putMap("payload", RNUtils.convertMapToWritableMap((Map) notification.getRawPayload()));
        output.putBoolean("hasLandingMessage", notification.hasLandingMessage());

        try {
            BatchPushPayload pushPayload = notification.getPushPayload();

            // Deeplink
            output.putString("deeplink", pushPayload.getDeeplink());

            // Custom large icon
            output.putString("androidCustomLargeIcon", pushPayload.getCustomLargeIconURL(context));

            // Big picture
            output.putString("androidBigPicture", pushPayload.getBigPictureURL(context));
        } catch (BatchPushPayload.ParsingException e) {
            Log.d(TAG, "Failed to parse push payload: " + e.getMessage());
            output.putNull("deeplink");
            output.putNull("androidCustomLargeIcon");
            output.putNull("androidBigPicture");
        }

        return output;
    }
}
