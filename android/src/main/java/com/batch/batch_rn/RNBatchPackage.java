package com.batch.batch_rn;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.BaseReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;

import java.util.Map;
import java.util.HashMap;

public class RNBatchPackage extends BaseReactPackage {

    @Nullable
    @Override
    public NativeModule getModule(String name, ReactApplicationContext reactContext) {
        if (name.equals(RNBatchModule.NAME)) {
            return new RNBatchModule(reactContext);
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public ReactModuleInfoProvider getReactModuleInfoProvider() {
        return () -> {
            final Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();
            moduleInfos.put(
                    RNBatchModule.NAME,
                    new ReactModuleInfo(
                            RNBatchModule.NAME,
                            RNBatchModule.NAME,
                            false,
                            false,
                            false,
                            true
                    ));
            return moduleInfos;
        };
    }
}
