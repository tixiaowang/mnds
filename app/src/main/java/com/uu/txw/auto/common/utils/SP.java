package com.uu.txw.auto.common.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SP {

    private static SharedPreferences sp;

    public static void init(Context context) {
        if (null == sp) {
            sp = context.getSharedPreferences("uu", Context.MODE_PRIVATE);
        }
    }

    public static void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public static void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static void putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }


    public static long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    public static int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public static void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key,
                                     boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    /**
     * 移除
     *
     * @param key
     */
    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }

}
