package com.uu.txw.auto.task.data;

import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.util.WeUI;


public class ObtainWindowStateChangeEvent {
    public static AccessibilityEventExt obtainEvent(String className) {
        try {
            AccessibilityEventExt accessibilityEvent = new AccessibilityEventExt();
            accessibilityEvent.setEventType(AccessibilityEventExt.TYPE_WINDOW_STATE_CHANGED);
            accessibilityEvent.setClassName(className);
            return accessibilityEvent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
