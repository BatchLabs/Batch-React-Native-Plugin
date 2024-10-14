package com.batch.batch_rn;

import android.app.Application;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfo;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.TurboReactPackage;

import java.util.Map;
import java.util.HashMap;

public class RNBatchPackage extends TurboReactPackage {

    public RNBatchPackage(Application application) {
        super();
        RNBatchModule.initialize(application);
    }

    @Nullable
    @Override
    public NativeModule getModule(String name, ReactApplicationContext reactContext) {
        if (name.equals(RNBatchModuleImpl.NAME)) {
            return new RNBatchModule(reactContext);
        } else {
            return null;
        }
    }

    @Override
    public ReactModuleInfoProvider getReactModuleInfoProvider() {
        return () -> {
            final Map<String, ReactModuleInfo> moduleInfos = new HashMap<>();
            moduleInfos.put(
                    RNBatchModuleImpl.NAME,
                    new ReactModuleInfo(
                            RNBatchModuleImpl.NAME,
                            RNBatchModuleImpl.NAME,
                            false, // canOverrideExistingModule
                            false, // needsEagerInit
                            true, // hasConstants
                            false, // isCxxModule
                            BuildConfig.IS_NEW_ARCHITECTURE_ENABLED // isTurboModule
                    ));
            return moduleInfos;
        };
    }
}
