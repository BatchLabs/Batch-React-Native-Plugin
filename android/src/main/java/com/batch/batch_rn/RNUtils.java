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
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
            } else if (value instanceof JSONObject) {
                try {
                    output.putMap(key, convertJSONObjectToWritableMap((JSONObject) value));
                } catch (JSONException e) {
                    Log.e(RNBatchModuleImpl.LOGGER_TAG, "Failed to parse JSON Object.", e);
                }
            }
            else if (value instanceof JSONArray) {
                output.putArray(key, convertArrayToWritableArray((JSONArray) value));
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
            } else if (value == null || value == JSONObject.NULL) {
                output.putNull(key);
            } else {
                output.putString(key, value.toString());
            }
        }
        return output;
    }

    public static WritableArray convertArrayToWritableArray(JSONArray input) {
        WritableArray output = new WritableNativeArray();

        for (int i = 0; i < input.length(); i++) {
            Object value = null;
            try {
                value = input.get(i);
                if (value instanceof Map) {
                    output.pushMap(convertMapToWritableMap((Map<String, Object>) value));
                }
                else if (value instanceof JSONObject) {
                    output.pushMap(convertJSONObjectToWritableMap((JSONObject) value));
                }
                else if (value instanceof JSONArray) {
                    output.pushArray(convertArrayToWritableArray((JSONArray) value));
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
            } catch (JSONException e) {
                Log.e(RNBatchModuleImpl.LOGGER_TAG, "Failed to parse JSON Array.", e);
            }
        }
        return output;
    }

    @Nullable
    public static BatchEventAttributes convertSerializedEventDataToEventAttributes(@Nullable ReadableMap attributes) {
        if (attributes == null) {
            return null;
        }
        BatchEventAttributes eventAttributes = new BatchEventAttributes();
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
            } else if ("object".equals(type)) {
                BatchEventAttributes object = convertSerializedEventDataToEventAttributes(valueMap.getMap("value"));
                if (object != null) {
                    eventAttributes.put(key, object);
                }
            } else if ("string_array".equals(type)) {
                List<String> list = convertReadableArrayToList(valueMap.getArray("value"));
                eventAttributes.putStringList(key, list);
            } else if ("object_array".equals(type)) {
                List<BatchEventAttributes> list = new ArrayList<>();
                ReadableArray array = valueMap.getArray("value");
                for (int i = 0; i < array.size(); i++) {
                    BatchEventAttributes object = convertSerializedEventDataToEventAttributes(array.getMap(i));
                    if (object != null) {
                        list.add(object);
                    }
                }
                eventAttributes.putObjectList(key, list);
            } else {
                Log.e(RNBatchModuleImpl.LOGGER_TAG, "Invalid parameter : Unknown event_data.attributes type (" + type + ")");
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

    /**
     * Convert a JSONObject into a Map
     * @param jsonObject The JSONObject to convert
     * @return the Map
     */
    public static WritableMap convertJSONObjectToWritableMap(JSONObject jsonObject) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            map.put(key, value);
        }
        return convertMapToWritableMap(map);
    }
}
