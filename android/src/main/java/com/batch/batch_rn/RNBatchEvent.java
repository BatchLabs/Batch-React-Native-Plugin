package com.batch.batch_rn;

import androidx.annotation.NonNull;


import com.facebook.react.bridge.WritableMap;

/**
 * Simple class to help storing batch event
 */
public class RNBatchEvent {

    @NonNull
    private final String name;

    @NonNull
    private final WritableMap params;


    public RNBatchEvent(@NonNull String name, @NonNull WritableMap params) {
        this.name = name;
        this.params = params;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public WritableMap getParams() {
        return params;
    }
}
