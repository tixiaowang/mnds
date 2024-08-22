package com.uu.txw.auto;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import com.uu.txw.auto.action.H;
import com.uu.txw.auto.common.utils.SP;


import java.util.Random;

/**
 * delay tool
 */
public class DelayKit {
    private static final Handler gUiHandler = new Handler(Looper.getMainLooper());


    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static void post(Runnable r) {
        gUiHandler.post(r);
    }

    public static void postDelayed(long delay, Runnable r) {
        gUiHandler.postDelayed(r, delay);
    }

    /**
     * 睡眠固定的时间，不可以被用户设置的执行速度调整
     *
     * @param time
     */
    public static void sleep(long time) {
        try {
            H.getInstance().updateOpreateTimeForward(time);
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 基础睡眠周期
     */
    private static int mBaseSleepDuration = 200;

    public static int getCurrentBaseSleepDuration() {
        return mBaseSleepDuration;
    }

    /**
     * 睡眠一个周期
     */
    public static void sleep() {
        try {
            H.getInstance().updateOpreateTimeForward(mBaseSleepDuration);
            Thread.sleep(mBaseSleepDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 睡眠2个周期
     */
    public static void sleepL() {
        try {
            int i = new Random().nextInt(200);
            H.getInstance().updateOpreateTimeForward(2 * mBaseSleepDuration + i);
            Thread.sleep(2 * mBaseSleepDuration + i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 睡眠5个周期
     */
    public static void sleepLL() {
        try {
            int i = new Random().nextInt(500);
            H.getInstance().updateOpreateTimeForward(5 * mBaseSleepDuration + i);
            Thread.sleep(5 * mBaseSleepDuration + i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void sleepLLL() {
        sleepLL();
        sleepLL();
    }

    public static void sleepRandom(int base) {
        try {
            int i = new Random().nextInt(base / 2) + base;
            H.getInstance().updateOpreateTimeForward(i);
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
