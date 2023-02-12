/*
 * Copyright (C) 2023 GrapheneOS
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.ext;

import android.content.ContentResolver;
import android.content.Context;
import android.ext.settings.Setting;
import android.os.Process;
import android.provider.Settings;

import java.util.function.Consumer;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.android.settings.core.BasePreferenceController.CONDITIONALLY_UNAVAILABLE;
import static com.android.settings.core.BasePreferenceController.DISABLED_FOR_USER;

public class ExtSettingControllerHelper<T extends Setting> {
    private final Context context;
    private final T setting;

    ExtSettingControllerHelper(Context context, T setting) {
        this.context = context;
        this.setting = setting;
    }

    public static int getGlobalSettingAvailability(Context ctx) {
        return Process.myUserHandle().isSystem() ? AVAILABLE : DISABLED_FOR_USER;
    }

    public static int getDevModeSettingAvailability(Context ctx) {
        ContentResolver cr = ctx.getContentResolver();
        String key = Settings.Global.DEVELOPMENT_SETTINGS_ENABLED;

        return (Settings.Global.getInt(cr, key, 0) == 0) ?
            CONDITIONALLY_UNAVAILABLE : AVAILABLE;
    }

    int getAvailabilityStatus() {
        if (setting.getScope() != Setting.Scope.PER_USER) {
            return getGlobalSettingAvailability(context);
        }
        return AVAILABLE;
    }

    private Object observer;

    void onResume(ExtSettingPrefController espc) {
        registerObserver(espc);
    }

    void onPause(ExtSettingPrefController espc) {
        unregisterObserver();
    }

    void registerObserver(Consumer<T> settingObserver) {
        if (setting.canObserveState()) {
            observer = setting.registerObserver(context, settingObserver, context.getMainThreadHandler());
        }
    }

    void unregisterObserver() {
        if (setting.canObserveState()) {
            setting.unregisterObserver(context, observer);
        }
    }
}
