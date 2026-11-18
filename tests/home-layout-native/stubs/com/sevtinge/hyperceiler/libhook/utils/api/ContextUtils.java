package com.sevtinge.hyperceiler.libhook.utils.api;

import android.content.Context;

/** Minimal host stub: the preference refresher asks for the system context. */
public final class ContextUtils {
    public static final int FlAG_ONLY_ANDROID = 2;
    public static final int FLAG_CURRENT_APP = 1;

    private ContextUtils() {
    }

    public static Context getContextNoError(int flag) {
        return new Context();
    }

    private static volatile Runnable screenOn;

    public static Runnable observeScreenOn(Context context, Runnable callback) {
        screenOn = callback;
        return () -> screenOn = null;
    }

    public static void signalScreenOn() {
        Runnable callback = screenOn;
        if (callback != null) callback.run();
    }
}
