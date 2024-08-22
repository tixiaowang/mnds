package com.uu.txw.auto.action;

import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.accessibility.AccessibilityHelper;

/**
 * @Author mnmn
 * @DATE 2024/8/22
 * @DESC
 */
public class ActionScrollForwardInterval extends AccessibilityHelper implements Action {
    @Override
    public void run(AccessibilityEventExt event) {

        while (isRunning()) {
            sleepLLL();
            performGestureScrollForward();
            sleepLLL();
            sleepLLL();
        }
    }
}
