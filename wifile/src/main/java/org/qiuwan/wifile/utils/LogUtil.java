package org.qiuwan.wifile.utils;

import android.util.Log;

import org.qiuwan.wifile.BuildConfig;

import java.util.Locale;

/**
 * Log的工具类.也是封装了系统的Log类,方便输出.不用再写Tag了.
 * Created by qiuwan.zheng on 14-2-15.
 */
public class LogUtil {

    public static String TAG = "Wifile";

    public static boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE) || BuildConfig.DEBUG;

    public static void setTag(String tag) {
        TAG = tag;
        DEBUG = Log.isLoggable(TAG, Log.VERBOSE);
    }

    public static void v(String message) {
        if (DEBUG) {
            Log.v(TAG, buildMessage(message));
        }
    }

    public static void d(String message) {
        if (DEBUG) {
            Log.i(TAG, buildMessage(message));
        }
    }

    public static void i(String message) {
        Log.i(TAG, buildMessage(message));
    }

    public static void e(String format) {
        Log.e(TAG, buildMessage(format));
    }

    public static void e(Throwable tr, String format) {
        Log.e(TAG, buildMessage(format), tr);
    }

    public static void wtf(String format) {
        Log.wtf(TAG, buildMessage(format));
    }

    public static void wtf(Throwable tr, String format) {
        Log.wtf(TAG, buildMessage(format), tr);
    }

    private static String buildMessage(String message) {
        String msg = message;
        StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();

        String caller = "<unknown>";
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            String callingClass = trace[i].getClassName();
            callingClass = callingClass.substring(callingClass
                    .lastIndexOf('.') + 1);
            callingClass = callingClass.substring(callingClass
                    .lastIndexOf('$') + 1);

            caller = callingClass + "." + trace[i].getMethodName();
            break;
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread()
                .getId(), caller, msg);
    }
}
