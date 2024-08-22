package com.uu.txw.auto.common.utils;

import android.util.Log;

import com.uu.txw.auto.common.CommonInstance;

import java.util.Arrays;
import java.util.stream.Collectors;


public class Logger {

    public static final String TAG = "mnds";
    private static String className;//类名
    private static String methodName;//方法名
    private static int lineNumber;//行数

    private Logger() {

    }

    public static boolean isDebuggable() {
        return true;
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName);
        buffer.append("(").append(className).append(":").append(lineNumber).append(")");
        buffer.append(log);
        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }


    public static void e(String message) {
        String msg = "[server] " + message;
        if (!isDebuggable())
            return;


        Log.e(TAG, msg);
    }


    public static void i(String message) {
        String msg = "[server] " + message;
        if (!isDebuggable())
            return;

        Log.i(TAG, msg);
    }

    public static void d(String message) {
        String msg = "[server] " + message;
        upLog(msg);
        if (!isDebuggable())
            return;

        Log.d(TAG, msg);
    }

    public static void clientD(String message) {
        String msg = "[client] " + message;
        upLog(msg);
        if (!isDebuggable())
            return;


        Log.d(TAG, msg);
    }

    public static void scriptD(String ...message) {
        String log = Arrays.stream(message).map(s -> {
            if (s != null) {
                if (s.length() > 20) {
                    return s.substring(0, 20);
                }
            }
            return s;
        }).collect(Collectors.joining(" "));
        String msg = "[server][script] " + log;
        upLog(msg);

        Log.d(TAG, "[server][script] " + String.join(" ", message));
        CommonInstance.getInterface().log2FloatWindow(log);
    }
    public static void scriptE(String ...message) {
        String log = Arrays.stream(message).map(s -> {
            if (s != null) {
                if (s.length() > 20) {
                    return s.substring(0, 20);
                }
            }
            return s;
        }).collect(Collectors.joining(" "));
        String msg = "[server][script] " + log;
        upLog(msg);

        Log.e(TAG, "[server][script] " + String.join(" ", message));
        CommonInstance.getInterface().log2FloatWindow(log);
    }

    public static void clientE(String message) {
        String msg = "[client] " + message;
        upLog(msg);
        if (!isDebuggable())
            return;

        Log.e(TAG, msg);
    }

    private static void upLog(String msg) {
    }


    public static void v(String message) {
        if (!isDebuggable())
            return;
        Log.v(TAG, "[server] " + message);
    }

}
