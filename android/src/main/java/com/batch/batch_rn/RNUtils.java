package com.batch.batch_rn;

import androidx.annotation.Nullable;

import android.util.Log;

import com.batch.android.BatchEventAttributes;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RNUtils {
    public static WritableMap convertMapToWritableMap(Map<String, Object> input) {
        WritableMap output = new WritableNativeMap();

        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                output.putMap(key, convertMapToWritableMap((Map<String, Object>) value));
            } else if (value instanceof JSONArray) {
                output.putArray(key, convertArrayToWritableArray((Object[]) value));
            } else if (value instanceof Boolean) {
                output.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                output.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                output.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                output.putString(key, (String) value);
            } else if (value instanceof Date) {
                output.putDouble(key, ((Date) value).getTime());
            } else if (value instanceof URI) {
                output.putString(key, value.toString());
            } else {
                output.putString(key, value.toString());
            }
        }
        return output;
    }

    public static WritableArray convertArrayToWritableArray(Object[] input) {
        WritableArray output = new WritableNativeArray();

        for (int i = 0; i < input.length; i++) {
            Object value = input[i];
            if (value instanceof Map) {
                output.pushMap(convertMapToWritableMap((Map<String, Object>) value));
            } else if (value instanceof JSONArray) {
                output.pushArray(convertArrayToWritableArray((Object[]) value));
            } else if (value instanceof Boolean) {
                output.pushBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                output.pushInt((Integer) value);
            } else if (value instanceof Double) {
                output.pushDouble((Double) value);
            } else if (value instanceof String) {
                output.pushString((String) value);
            } else if (value instanceof Date) {
                output.pushDouble(((Date) value).getTime());
            } else if (value instanceof URI) {
                output.pushString(value.toString());
            } else {
                output.pushString(value.toString());
            }
        }
        return output;
    }

    @Nullable
    public static BatchEventAttributes convertSerializedEventDataToEventAttributes(@Nullable ReadableMap serializedEventData) {
        if (serializedEventData == null) {
            return null;
        }

        BatchEventAttributes eventAttributes = new BatchEventAttributes();
        ReadableArray tags = serializedEventData.getArray("$tags");
        if (tags != null && tags.size() > 0) {
            eventAttributes.putStringList("$tags", convertReadableArrayToList(tags));
        }

        String label = serializedEventData.getString("$label");
        if (label != null) {
            eventAttributes.put("$label", label);
        }

        ReadableMap attributes = serializedEventData.getMap("attributes");
        ReadableMapKeySetIterator iterator = attributes.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableMap valueMap = attributes.getMap(key);
            String type = valueMap.getString("type");
            if ("string".equals(type)) {
                eventAttributes.put(key, valueMap.getString("value"));
            } else if ("boolean".equals(type)) {
                eventAttributes.put(key, valueMap.getBoolean("value"));
            } else if ("integer".equals(type)) {
                eventAttributes.put(key, valueMap.getDouble("value"));
            } else if ("float".equals(type)) {
                eventAttributes.put(key, valueMap.getDouble("value"));
            } else if ("date".equals(type)) {
                long timestamp = (long) valueMap.getDouble("value");
                Date date = new Date(timestamp);
                eventAttributes.put(key, date);
            } else if ("url".equals(type)) {
                eventAttributes.put(key, URI.create(valueMap.getString("value")));
            } else {
                Log.e("RNBatchPush", "Invalid parameter : Unknown event_data.attributes type (" + type + ")");
            }
        }

        return eventAttributes;
    }

    /**
     * Convert a ReadableArray into a List of String
     *
     * @param array ReadableArray to convert
     * @return List of String
     */
    public static List<String> convertReadableArrayToList(ReadableArray array) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }
}
