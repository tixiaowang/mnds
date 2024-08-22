package com.uu.txw.auto.task;

import com.uu.txw.auto.action.ScriptInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalVar {
    public static final String KEYWORD = "关键词";
    public static final Map<String, ScriptInit> localTaskInitMap = new HashMap<>();

    public static final Map<String, Object> map = new HashMap<>();

    public static void clear() {
        map.clear();
    }

    public static void put(String k, Object v) {
        map.put(k, v);
    }

    public static List<String> getTextList(String k) {
        map.putIfAbsent(k, new ArrayList<>());
        return (List<String>) map.get(k);
    }

    public static List<Map<String, String>> getMapList(String k) {
        map.putIfAbsent(k, new ArrayList<>());
        return (List<Map<String, String>>) map.get(k);
    }

    public static Integer getInt(String k) {
        return (Integer) map.getOrDefault(k, 0);
    }

    public static Boolean getBoolean(String k) {
        return (Boolean) map.getOrDefault(k, false);
    }

    public static String getText(String k) {
        return (String) map.getOrDefault(k, "");
    }

    public static long tryGetLong(String s, long defaultVal) {
        try {
            return Long.parseLong(s);
        } catch (Exception ignore) {

        }
        return defaultVal;
    }
}
