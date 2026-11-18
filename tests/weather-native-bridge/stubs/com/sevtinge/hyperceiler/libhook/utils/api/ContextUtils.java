package com.sevtinge.hyperceiler.libhook.utils.api;
import android.content.Context;
public class ContextUtils {
    public static final int FlAG_ONLY_ANDROID = 2;
    public static boolean missing;
    private static volatile Runnable screenOn;
    public static Context getContextNoError(int flag) { return missing ? null : new Context(); }
    public static Runnable observeScreenOn(Context context, Runnable callback) {
        screenOn = callback;
        return () -> screenOn = null;
    }
    public static void signalScreenOn() {
        Runnable callback = screenOn;
        if (callback != null) callback.run();
    }
}
