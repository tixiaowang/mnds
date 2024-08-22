package com.uu.txw.auto.action;

import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.accessibility.AccessibilityHelper;

/**
 * @Author mnmn
 * @DATE 2024/8/22
 * @DESC
 */
public class ActionClickCenterInterval extends AccessibilityHelper implements Action {
    @Override
    public void run(AccessibilityEventExt event) {

        while (isRunning()) {
            sleep();
            performGestureClick(500, 1000);
            sleep();
            performGestureClick(500, 1000);
        }
    }
}
