package com.uu.txw.auto;

import static android.content.Context.KEYGUARD_SERVICE;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;

import com.uu.txw.auto.accessibility.AccessibilityHelper;
import com.uu.txw.auto.common.utils.Logger;

public class AccesStatusUtil {


    public static void onConnected() {
        wakeUpAndUnlock();
    }

    private static boolean hasWakeUp;

    /**
     * 唤醒手机屏幕并解锁
     */
    private static void wakeUpAndUnlock() {
        if (hasWakeUp) {
            return;
        }
        hasWakeUp = true;
        PowerManager pm = (PowerManager) AppInstance.getInstance().provideContext().getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        Logger.v("screenOn: " + screenOn);
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "uu:bright");
            wl.acquire(5000); // 点亮屏幕
        }
        // 屏幕解锁
        KeyguardManager keyguardManager = (KeyguardManager) AppInstance.getInstance().provideContext().getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("uu:unLock");
        keyguardLock.disableKeyguard();
//        AccessibilityHelper.performHome();
        AccessibilityHelper.performBack();
        new Handler().postDelayed(AccessibilityHelper::performBack, 200);
        new Handler().postDelayed(AccessibilityHelper::performBack, 500);
    }
}
