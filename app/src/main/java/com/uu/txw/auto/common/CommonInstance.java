package com.uu.txw.auto.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.uu.txw.auto.common.realm.AccessibilityTask;
import com.uu.txw.auto.common.utils.SP;


public class CommonInstance {
    public static final String IMAGE_HOST = "";
    private int APP_VERSION_PLATFORM;
    private LibInterface libInterface;
    private static final CommonInstance ourInstance = new CommonInstance();
    private Handler mHandler;

    private CommonInstance() {
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public static CommonInstance get() {
        return ourInstance;
    }

    public static Handler getHandler(){
        return CommonInstance.get().mHandler;
    }
    public static LibInterface getInterface() {
        return ourInstance.libInterface;
    }

    public void regist(Context context ,LibInterface libInterface) {
        this.libInterface = libInterface;
        SP.init(context);
    }


    public interface LibInterface {
        Context provideContext();
        AccessibilityTask getCurrTaskEntity();
        void log2FloatWindow(String msg);
    }
}
