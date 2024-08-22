package com.uu.txw.auto.action;

import android.view.accessibility.AccessibilityEventExt;


public interface Action {
    void run(AccessibilityEventExt event);
}
