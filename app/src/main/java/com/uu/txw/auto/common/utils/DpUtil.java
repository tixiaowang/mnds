package com.uu.txw.auto.common.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IInterface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.uu.txw.auto.common.CommonInstance;

import java.lang.reflect.Method;

public class DpUtil {
    private DpUtil() {
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    public static int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, CommonInstance.getInterface().provideContext().getResources().getDisplayMetrics());
    }


    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) CommonInstance.getInterface().provideContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得dpi
     *
     * @param context
     * @return
     */
    public static int getDpi(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.densityDpi;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight() {
//        WindowManager wm = (WindowManager) CommonInstance.getInterface().provideContext()
//                .getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(outMetrics);
//        return outMetrics.heightPixels;

        try {
            IInterface display = a("display", "android.hardware.display.IDisplayManager");
            java.lang.Object invoke = display.getClass().getMethod("getDisplayInfo", java.lang.Integer.TYPE).invoke(display, 0);
            if (invoke == null) {
                return 0;
            }
            java.lang.Class<?> cls = invoke.getClass();
            int i3 = cls.getDeclaredField("logicalHeight").getInt(invoke);
            return i3;
        } catch (Exception ignore) {

        }
        return 0;
    }

    private static android.os.IInterface a(java.lang.String str, java.lang.String str2) {
        try {
            Method getService = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            return (android.os.IInterface) java.lang.Class.forName(str2 + "$Stub").getMethod("asInterface", android.os.IBinder.class).invoke(null, (android.os.IBinder) getService.invoke(null, str));
        } catch (java.lang.Exception e2) {
            throw new java.lang.AssertionError(e2);
        }
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth();
        int height = getScreenHeight();
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth();
        int height = getScreenHeight();
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }
}