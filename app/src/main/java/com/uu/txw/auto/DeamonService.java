package com.uu.txw.auto;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.accessibility.AccessibilityHelper;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.action.H;
import com.uu.txw.auto.common.ClientTrack;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.realm.KcClientTrack;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.task.data.ObtainWindowStateChangeEvent;
import com.uu.txw.auto.util.AppHelper;

import java.util.ArrayList;
import java.util.List;

public class DeamonService extends IntentService {
    private static final String EXTRA_TASK_ID = "extra_task_id";

    private AccessibilityEventExt event;
    private List<String> mTodoQueue;

    public DeamonService() {
        super("DeamonService");
    }

    public static void startWithTaskId(Context context, int taskId) {
        Intent intent = new Intent(context, DeamonService.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mTodoQueue = new ArrayList<>();
        //更新当前操作时间戳
        H.getInstance().updateOpreateTime();
        long todoTimeStrap = STime.currenttimemillis();
        boolean hasPerformHome = false;
        if (intent != null) {
            int taskId = intent.getIntExtra(EXTRA_TASK_ID, -2);
            while (TaskId.isCurrTaskId(taskId) && (TaskId.currScriptTask == null || TaskId.currScriptTask.bindUiTask())) {

                Logger.v("DeamonService");
                try {
                    if (isResponseTimeOut()) {
                        //超时
                        //手动触发动作
                        String currentActivity = AndroidCurrentActivity.getInstance().getCurrentActivity();
                        Logger.v("当前页面：" + currentActivity);
                        if (currentActivity == null) {
                            //界面为空，尝试打开App
                            AppHelper.openApp(this);
                            Thread.sleep(2000);
                            if ((currentActivity = AndroidCurrentActivity.getInstance().getCurrentActivity()) == null) {
                                //如果还是为空，那么尝试返回
                                Logger.v("界面为空，尝试返回 ... ");
                                back();
                                Thread.sleep(1000);
                                currentActivity = AndroidCurrentActivity.getInstance().getCurrentActivity();
                                if (currentActivity != null) {
                                    todoTimeStrap = STime.currenttimemillis();
                                }
                            } else {
                                todoTimeStrap = STime.currenttimemillis();
                            }
                        }
                        long diff = STime.currenttimemillis() - todoTimeStrap;
                        if (diff > 5000) {
                            Logger.v("超时5s以上：" + diff);
                            if (diff > 30_000) {
                                String message = "脚本执行超时30s";
                                Logger.e(message);
                                new KcClientTrack(ClientTrack.TRACK_LOG_ERROR, message).track();
                                Thread.sleep(3000);
                            } else if (diff > 20_000) {
                                Thread.sleep(3000);
                            }
                            if (diff > 5 * 60 * 1000) {
                                TaskId.e("脚本执行超时5min");
                                TaskId.stop();
                            } else if (!hasPerformHome && diff > 60 * 1000) {
                                Logger.e("脚本执行超时1min,perform home action");
                                AccessibilityHelper.performHome();
                                hasPerformHome = true;
                                Thread.sleep(2000);
                            } else if (TaskId.currScriptTask != null && !TaskId.currScriptTask.main_package_main_ui.equals(currentActivity)) {
                                //不是在主界面
                                if (currentActivity != null) {
                                    //界面不为空
                                    Logger.v("超时5000，返回");
                                    //超时5s以上 并且不是在主界面 ,那么执行返回
                                    if (isResponseTimeOut()) {
                                        back();
                                    }
                                    H.getInstance().updateOpreateTime();
                                    todoTimeStrap = STime.currenttimemillis();
                                }
                            } else {
                                Logger.v("准备手动触发事件");
                                todo(taskId, currentActivity);
                            }
                        } else if (TaskId.currScriptTask != null && !TaskId.currScriptTask.main_package_main_ui.equals(currentActivity) && mTodoQueue.size() == 2 && mTodoQueue.get(0).equals(mTodoQueue.get(1)) && mTodoQueue.get(0).equals(currentActivity)) {
                            //不是在主界面
                            Logger.v("连续3次相同的todo，返回：" + currentActivity);
                            //连续三次触发相同的界面 并且不是在主界面 ,那么执行返回
                            back();
                            H.getInstance().updateOpreateTime();
                            todoTimeStrap = STime.currenttimemillis();
                            //清空缓存
                            mTodoQueue.clear();
                        } else {
                            Logger.v("准备手动触发事件");
                            todo(taskId, currentActivity);
                        }
                    } else {
                        //没有超时，页面切换正常
                        if (mTodoQueue.size() > 0) {
                            //queue中有旧数据
                            if (!TextUtils.equals(AndroidCurrentActivity.getInstance().getCurrentActivity(), mTodoQueue.get(mTodoQueue.size() - 1))) {
                                //当前页面和queue中的倒数一个不同，那么清空queue
                                mTodoQueue.clear();
                            }
                        }
                        todoTimeStrap = STime.currenttimemillis();
                    }

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void back() {
        if (!AccessibilityHelper.performBack()) {
            // back by node id
        }
    }

    private void todo(int taskId, String currentActivity) {
        if (currentActivity != null) {
            if (mTodoQueue.size() == 2) {
                mTodoQueue.remove(0);
            }
            mTodoQueue.add(currentActivity);
            //是activity页面才去触发
            if (event != null) {
//                event.recycle();
            }
            event = ObtainWindowStateChangeEvent.obtainEvent(currentActivity);
            if (event != null) {
                Logger.v("手动触发Event ：" + event.getClassName());
                HandleEventService.startWithEvent(this, event);
            }
        }
    }

    /**
     * 操作超时
     *
     * @return
     */
    public static boolean isResponseTimeOut() {
        return STime.currenttimemillis() - H.getInstance().getUpdateTime() > 3000;
    }
}
