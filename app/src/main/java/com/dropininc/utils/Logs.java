package com.dropininc.utils;

import android.util.Log;


public class Logs {
    private static boolean isDebug = true;

    public static void log(Exception e) {
        if (isDebug)
            e.printStackTrace();
    }

    public static void log(String type, String className, String message) {
        if (isDebug) {
            if (type.equalsIgnoreCase("d")) {
                Log.d(className, message);
            } else if (type.equalsIgnoreCase("e")) {
                Log.e(className, message);
            } else if (type.equalsIgnoreCase("i")) {
                Log.i(className, message);
            } else if (type.equalsIgnoreCase("v")) {
                Log.v(className, message);
            } else {
                Log.w(className, message);
            }
        }
    }

    public static void log(String className, String message) {
        if (isDebug) {
            Log.d(className, message);
        }
    }

    public static void log(String message) {
        if (isDebug) {
            Log.d("Logs", message);
        }
    }
}
