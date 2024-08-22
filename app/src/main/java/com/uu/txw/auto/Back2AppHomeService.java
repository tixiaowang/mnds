package com.uu.txw.auto;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.uu.txw.auto.accessibility.AccessibilityHelper;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.util.AppHelper;

import java.util.ArrayList;
import java.util.List;

public class Back2AppHomeService extends IntentService {


    private static List<String> list = new ArrayList<>();
    public static boolean runControl;

    //可以正常的返回到目标程序主界面
    public static boolean backMainUiOk = true;

    public Back2AppHomeService() {
        super("BackToMainUiService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, Back2AppHomeService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (AppHelper.checkAppInstall() && !TaskId.get().mTaskToggle) {
            Logger.d("BackToMainUiService ,onHandleIntent ...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                long startTime = STime.currenttimemillis();
                boolean hasPerformHome = false;
                //检查屏幕，熄屏的话点亮
                wakeUp();
                runControl = true;
                while (!AndroidCurrentActivity.isMainUi() && Back2AppHomeService.runControl) {
                    Logger.d("不是主界面 ... ");
                    AppInstance.getInstance().showNotifyToast("尝试返回到应用首页");
                    //超时的判断
                    if (STime.currenttimemillis() - startTime > 60000) {
                        //超时60s
                        backMainUiOk = false;
                        Logger.e("结束任务");
                        TaskId.stop();
                        return;
                    }
                    //返回主界面
                    if (!hasPerformHome && STime.currenttimemillis() - startTime > 20_000) {
                        //超时30s
                        AccessibilityHelper.performHome();
                        hasPerformHome = true;
                        Thread.sleep(2000);
                    }

                    try {
                        if (AndroidCurrentActivity.getInstance().getCurrentActivity() == null) {
                            //尝试打开App
                            Logger.d("界面为空，尝试打开应用 ... ");
                            AppHelper.openApp(this);
                            Thread.sleep(2000);
                            if (AndroidCurrentActivity.getInstance().getCurrentActivity() == null) {
                                //如果还是为空，那么尝试返回
                                Logger.d("界面为空，尝试返回 ... ");
                                back();
                                Thread.sleep(2000);
                            }
                        }

                        String currentActivity = AndroidCurrentActivity.getInstance().getCurrentActivity();
                        if (currentActivity != null) {
                            if (concludeFilter(currentActivity)) {
                                //是登陆注册设置密码之类的界面，停止任务
                                TaskId.i("识别到排除页面，BackToMainUiService停止尝试");
                                return;
                            } else if (TaskId.currScriptTask != null) {
                                if (!TaskId.currScriptTask.main_package_main_ui.equals(currentActivity)) {
                                    //再次做一次验证，防止因为几十ms时间的误差，在主界面返回
                                    Logger.d("尝试返回 ..., main_package_main_ui: " + TaskId.currScriptTask.main_package_main_ui);
                                    back();
                                }
                            }
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (runControl) {
                    Logger.i("back to main ui ready...");
                    TaskId.get().mTaskToggle = true;
                    //模拟 点击开始按钮
                    new Handler(Looper.getMainLooper()).post(() -> AppInstance.getInstance().backToMainUiReady());
                    backMainUiOk = true;
                } else {
                    Logger.i("back to main ui stopped!");
                }
            } catch (Exception e) {
                Logger.e(Log.getStackTraceString(e));
            }
        }
    }

    private void back() {
        if (!AccessibilityHelper.performBack()) {
            // back by click back node
        }
    }

    public static boolean concludeFilter(String currentActivity) {
        for (String s : list) {

            if (TextUtils.equals(currentActivity, s)) {
                return true;
            }
        }
        return false;
    }

    private void wakeUp(){
        PowerManager pm = (PowerManager) getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();
        Logger.v("screenOn: " + screenOn);
        if (!screenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "uc:bright");
            wl.acquire(5000); // 点亮屏幕
        }
    }

}
