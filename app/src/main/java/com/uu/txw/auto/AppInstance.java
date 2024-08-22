package com.uu.txw.auto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;


import com.uu.txw.auto.accessibility.AccessibilityHelper;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.common.ClientTrack;
import com.uu.txw.auto.common.CommonInstance;
import com.uu.txw.auto.common.realm.AccessibilityTask;
import com.uu.txw.auto.common.realm.KcClientTrack;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.util.ToastUtil;
import com.uu.txw.auto.util.AppHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AppInstance {
    private static final AppInstance ourInstance = new AppInstance();
    private Context context;

    public static AppInstance getInstance() {
        return ourInstance;
    }

    private AppInstance() {
    }

    public void regist(Context context, Class<?> uiConfClass) {
        this.context = context;
        CommonInstance.get().regist(context, new ModuleCommonCallback(context));
        AndroidCurrentActivity.getInstance().initUi(uiConfClass);
        FloatWindowUtil.getInstance().init(context);
    }

    public void onAccessibilityServiceConnected(MyAccessService myAccessService) {
        AccesStatusUtil.onConnected();
    }

    public void backToMainUiReady() {
        if (!FloatWindowUtil.getInstance().getTaskStatus()) {
            FloatWindowUtil.getInstance().initTaskData();
        }
    }

    public void onNewTaskStarted(String taskShortDesc, int id) {
        FloatWindowUtil.getInstance().taskStarted(taskShortDesc, id);
    }

    public void onTaskComplete(String message) {
        FloatWindowUtil.getInstance().completeTask(message);
    }

    public void onTaskComplete(String message, String leftMenutext, Runnable nextRunnable) {
        FloatWindowUtil.getInstance().completeTask(message, leftMenutext, nextRunnable);
    }

    public void onWindowChange(String className) {
        FloatWindowUtil.getInstance().showOnAppActivity(className);
    }

    public void showNotifyToast(String message) {
        FloatWindowUtil.getInstance().showShortNotifyToast(message);
    }

    public Context provideContext() {
        return this.context;
    }

    public boolean run(Activity activity, Runnable runnable) {
        return run(true, activity, -1, runnable);
    }

    public boolean run(Activity activity, int taskId, Runnable runnable) {
        return run(true, activity, taskId, runnable);
    }

    public boolean run(boolean checkIdConfig, Activity activity, int taskId, Runnable runnable) {
        //判断App安装？
        if (!AppHelper.checkAppInstall()) {
            ToastUtil.show("应用未安装！");
            Logger.e("【严重】应用未安装！");
            return false;
        }
        if (activity == null || AppInstance.getInstance().checkAccesibilityAndIdConfig(checkIdConfig, activity)) {
                AppInstance.getInstance().startTask(taskId, runnable);
            if (TaskId.currScriptTask == null || !TaskId.currScriptTask.immediatelySingleThreadTask()) {
                AppHelper.openApp(AppInstance.getInstance().provideContext());
            }
            if (!showStartControl(taskId)) {
                if (TaskId.get().mStartFromWindow || functionNeedNotLogin(taskId) || (TaskId.currScriptTask != null && TaskId.currScriptTask.immediatelySingleThreadTask())) {
                    TaskId.get().mTaskToggle = true;
                    DelayKit.postDelayed(500, () -> AppInstance.getInstance().backToMainUiReady());
                } else {
                    TaskId.get().mTaskToggle = false;
                    Back2AppHomeService.start(AppInstance.getInstance().provideContext());
                }
            }
            return true;
        }
        return false;
    }

    private void startTask(int taskId, Runnable runnable) {
        FloatWindowUtil.getInstance().startTask(taskId, runnable);
    }

    //显示开始按钮，让用户自己点击的任务列表
    private static final List<Integer> showStartControlTaskList = Arrays.asList(TaskId.TASK_CHAT_WINDOW_GROUP);

    //不需要登录的功能
    private static final List<Integer> needNotLoginTaskList = Collections.emptyList();

    public static boolean showStartControl(int taskId) {
        return showStartControlTaskList.contains(taskId) || (TaskId.currScriptTask != null && TaskId.currScriptTask.immediatelySingleThreadTask());
    }

    public static boolean functionNeedNotLogin(int taskId) {
        return needNotLoginTaskList.contains(taskId);
    }

    public boolean checkAccesibilityAndIdConfig(boolean checkIdConfig, Activity activity) {
        if (!Settings.canDrawOverlays(activity)) {
            if ("Meizu".equals(Build.MANUFACTURER)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            }
            return false;
        } else if (!AccessibilityHelper.isServiceRunning(AccessibilityHelper.mService)) {
            AccessibilityHelper.openAccessibilityServiceSettings(activity);
            return false;
        }
        return true;
    }

    public boolean checkAccesibilityAndIdConfig() {
        return checkAccesibilityAndIdConfig(null);
    }

    public boolean checkAccesibilityAndIdConfig(AccessibilityTask task) {
        if (!Settings.canDrawOverlays(provideContext())) {
            String msg = "悬浮窗权限未打开！！！";
            if (task == null) {
                Logger.e(msg);
            }else {
                TaskId.commitTaskResult(task, false, msg);
            }
            new KcClientTrack(ClientTrack.TRACK_LOG_ERROR, msg).track();
            return false;
        } else if (!AccessibilityHelper.isServiceRunning(AccessibilityHelper.mService)) {
            String msg = "无障碍辅助服务未开启！！！";
            if (task == null || BuildConfig.DEBUG) {
                Logger.e(msg);
            }else {
                TaskId.commitTaskResult(task, false, msg);
            }
            new KcClientTrack(ClientTrack.TRACK_LOG_ERROR, msg).track();
            return false;
        }
        return true;
    }
}
