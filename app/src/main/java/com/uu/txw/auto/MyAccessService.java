package com.uu.txw.auto;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.accessibility.AccessibilityHelper;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.util.WeUI;

public class MyAccessService extends AccessibilityService {


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

//        AccessibilityServiceInfo mServeiceInfo = new AccessibilityServiceInfo();
//        mServeiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        mServeiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
//        setServiceInfo(mServeiceInfo);

        AccessibilityHelper.mService = this;
        Logger.d("AccessibilityService connected...");

        AppInstance.getInstance().onAccessibilityServiceConnected(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            Logger.d(AccessibilityEvent.eventTypeToString(event.getEventType()) + "; " + packageName + "; " + event.getClassName());
            String s = event.getClassName().toString();

            if ("com.android.systemui".equals(packageName) && "com.android.systemui.media.MediaProjectionPermissionActivity".equals(s)) {
                //截屏权限弹框 自动点击
                if (!AccessibilityHelper.performGestureClickByText("立即开始")) {
                    AccessibilityHelper.performGestureClickByText("允许");
                }
                return;
            }

            if (event.getClassName().toString().endsWith("Dialog") || event.getClassName().toString().endsWith("KeyboardMonitor")) {

                //LINEARLAYOUT FRAMELAYOUT和LOADING类型不处理,不参与页面切换监控和action执行
                return;
            }
            if (TextUtils.equals(WeUI.CLASS_NAME_LINEARLAYOUT, event.getClassName()) || TextUtils.equals(WeUI.CLASS_NAME_FRAMELAYOUT, event.getClassName())) {
                //LINEARLAYOUT FRAMELAYOUT和LOADING类型不处理,不参与页面切换监控和action执行
                return;
            }
            //1s 内触发上次的界面 ，直接忽略 (但是得排除上次的更新是手动的)
            if (s.equals(AndroidCurrentActivity.getAccessibilityUi()) && STime.currenttimemillis() - AndroidCurrentActivity.getInstance().getUpdateTime() < 1000 && !AndroidCurrentActivity.getInstance().isManualForTheLast()) {
                return;
            }

            if (TaskId.currScriptTask == null || packageName.equals(TaskId.currScriptTask.main_package_name)) {
                if (!s.startsWith("android.widget")) {
                    AndroidCurrentActivity.getInstance().updateCurrent(s);
                }
            }
        }
        if (TaskId.get().mTaskToggle && TaskId.get().mCurrentId > 0) {
            //任务开关打开 并且有任务id
            HandleEventService.startWithEvent(getApplicationContext(), AccessibilityEventExt.of(event));
        }
    }

    @Override
    public void onInterrupt() {

    }
}
