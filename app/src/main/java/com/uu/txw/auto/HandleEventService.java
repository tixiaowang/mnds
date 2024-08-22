package com.uu.txw.auto;

import static android.view.accessibility.AccessibilityEventExt.TYPE_NOTIFICATION_STATE_CHANGED;
import static android.view.accessibility.AccessibilityEventExt.TYPE_WINDOW_STATE_CHANGED;
import static com.uu.txw.auto.util.WeUI.CLASS_NAME_TOAST;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.action.H;
import com.uu.txw.auto.task.TaskId;

public class HandleEventService {
    private static HandleEventService instance;
    private final Handler requestHandler;

    public static HandleEventService get() {
        if (instance == null) {
            synchronized (HandleEventService.class) {
                if (instance == null) {
                    instance = new HandleEventService();
                }
            }
        }
        return instance;
    }

    private HandleEventService() {
        HandlerThread handlerThread = new HandlerThread("HandleEventService");
        handlerThread.start();
        requestHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                AccessibilityEventExt obj = (AccessibilityEventExt) msg.obj;
                onHandleIntent(obj);
            }
        };
    }

    public static void startWithEvent(Context context, AccessibilityEventExt event) {
        //有新的事件，那么立马更新最后的操作时间戳
        H.getInstance().updateOpreateTime();
        if (TYPE_NOTIFICATION_STATE_CHANGED == event.getEventType() && TextUtils.equals(CLASS_NAME_TOAST, event.getClassName())) {
            H.getInstance().excuteServiceMethods(TaskId.get().mCurrentId, event);
            return;
        }
        HandleEventService.get().postEvent(event);
    }


    private void postEvent(AccessibilityEventExt event) {
        Message obtain = Message.obtain();
        obtain.obj = event;
        requestHandler.sendMessage(obtain);
    }

    protected void onHandleIntent(AccessibilityEventExt event) {
        if (event != null) {
            if (event.getEventType() == TYPE_WINDOW_STATE_CHANGED) {
                DelayKit.sleepL();
            }
            if (TaskId.get().mCurrentId != -1) {
                //任务是执行状态 才去执行脚本
                H.getInstance().excuteServiceMethods(TaskId.get().mCurrentId, event);
            }
        }
    }

    public void release() {
        requestHandler.removeCallbacksAndMessages(null);
    }
}
