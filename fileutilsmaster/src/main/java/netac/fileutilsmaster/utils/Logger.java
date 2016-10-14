package netac.fileutilsmaster.utils;

import android.util.Log;

/**
 * Created by siwei.zhao on 2016/10/11.
 */
public class Logger {
    private static boolean isLogEnable = true;

    public static String tag = "Logger";

    public static void debug(boolean isEnable) {
        debug(tag, isEnable);
    }

    public static void debug(String logTag, boolean isEnable) {
        tag = logTag;
        isLogEnable = isEnable;
    }

    public static void v(String msg, Object... args) {
        if (isLogEnable) Log.v(tag, String.format(msg, args));
    }

    public static void d(String msg, Object... args) {
        if (isLogEnable) Log.d(tag, String.format(msg, args));
    }

    public static void i(String msg, Object... args) {
        if (isLogEnable) Log.i(tag, String.format(msg, args));
    }

    public static void w(String msg, Object... args) {
        if (isLogEnable) Log.w(tag, String.format(msg, args));
    }


    public static void e(String msg, Object... args) {
        if (isLogEnable) Log.e(tag, String.format(msg, args));
    }

    public static void e(Throwable t) {
        if (isLogEnable) t.printStackTrace();
    }
}
