package com.uu.txw.auto.task;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.uu.txw.auto.AppInstance;
import com.uu.txw.auto.DeamonService;
import com.uu.txw.auto.HandleEventService;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.action.H;
import com.uu.txw.auto.annotation.TASK_NAME;
import com.uu.txw.auto.annotation.TASK_SHORT_NAME;
import com.uu.txw.auto.common.ClientTrack;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.realm.AccessibilityTask;
import com.uu.txw.auto.common.realm.KcClientTrack;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.data.CusScriptTask;

import java.lang.reflect.Field;
import java.util.HashMap;


public class TaskId {

    /**
     * 开启任务是否从悬浮框开始
     */
    public boolean mStartFromWindow = false;
    /**
     * 开启任务总开关 是否准备好了（返回到了App主界面）
     */
    public boolean mTaskToggle = false;

    /**
     * 任务实体类
     */
    public AccessibilityTask mTaskEntity;

    public String mTaskServerId;

    /**
     * 任务执行过程中需要提交到服务器的日志
     */
    public StringBuilder mTaskLog;

    /**
     * 任务执行的过程中是否是成功的，没有发生异常
     */
    public boolean mSuccess = true;

    /**
     * 开始时间
     */
    public long mStartTime;

    /**
     * 结束时间
     */
    private long mEndTime;

    /**
     * 当前任务总开关
     */
    public int mCurrentId = -1;

    /**
     * 聊天窗口转发
     */
    @TASK_NAME("聊天窗口转发")
    @TASK_SHORT_NAME("转发")
    public static final int TASK_CHAT_WINDOW_GROUP = 24;

    /**
     * 自定义脚本
     */
    @TASK_NAME("自定义脚本")
    @TASK_SHORT_NAME("脚本")
    public static final int TASK_CUS_SCRIPT =  999;

    public static CusScriptTask currScriptTask;

    private static final TaskId ourInstance = new TaskId();

    public static TaskId get() {
        return ourInstance;
    }

    private TaskId() {

    }

    private SparseArray<String> mTaskNames = new SparseArray<>();
    private SparseArray<String> mTaskFieldNames = new SparseArray<>();


    /**
     * 判断是否是当前任务
     *
     * @param taskId
     * @return
     */
    public static boolean isCurrTaskId(int taskId) {
        return taskId == TaskId.get().mCurrentId;
    }

    /**
     * 开始任务
     *
     * @param id
     * @return
     */
    public static void start(int id) {
        AppInstance.getInstance().onNewTaskStarted(
                currScriptTask != null && !TextUtils.isEmpty(currScriptTask.task_name) ? currScriptTask.task_name : getTaskName(id), id);
        TaskId.get().mCurrentId = id;
        TaskId.get().mTaskLog = new StringBuilder();
        TaskId.get().mSuccess = true;
        TaskId.get().mStartTime = STime.currenttimemillis();
        TaskId.get().mEndTime = -1;
        DeamonService.startWithTaskId(AppInstance.getInstance().provideContext(), id);

    }

    public static String getTaskName(int id) {
        try {
            String taskShortName = TaskId.get().mTaskNames.get(id);
            if (TextUtils.isEmpty(taskShortName) && TaskId.get().mTaskNames.size() == 0) {
                for (Field field : TaskId.class.getDeclaredFields()) {
                    if (field.getType().equals(int.class)) {
                        TASK_NAME annotation = field.getAnnotation(TASK_NAME.class);
                        if (annotation != null && !TextUtils.isEmpty(annotation.value())) {
                            TaskId.get().mTaskNames.put((int) field.get(TaskId.get()), annotation.value());
                        }
                        TaskId.get().mTaskFieldNames.put((int) field.get(TaskId.get()), field.getName());
                    }
                }
                taskShortName = TaskId.get().mTaskNames.get(id);
            }
            return taskShortName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void start(int id, AccessibilityTask entity) {
        HashMap<String, String> entityMap = new HashMap<>();
        entityMap.put("name", getTaskName(id));
        entityMap.put("content", entity.getContent());
        entityMap.put("targetName", entity.getTargetName());
        entityMap.put("ext", entity.getExt());
        new KcClientTrack(ClientTrack.TRACK_TASK_EXECUTE, entityMap.toString()).track();
        TaskId.get().mTaskEntity = entity;
        TaskId.get().mTaskServerId = entity.getId();
        start(id);

    }

    public static void i(String msg) {
        Logger.i(msg);
        if (TaskId.get().mTaskLog != null) {
            if (TaskId.get().mTaskLog.length() == 0) {
                TaskId.get().mTaskLog.append("[").append(TaskId.getTaskName(TaskId.get().mCurrentId)).append("]");
            }
            if (TaskId.get().mTaskLog.indexOf(msg) == -1) {
                //不包含
                TaskId.get().mTaskLog.append(msg).append(";");
            }
        }
    }

    public static void e(String msg) {
        TaskId.get().mSuccess = false;
        Logger.e(msg);
        if (TaskId.get().mTaskLog != null) {
            if (TaskId.get().mTaskLog.length() == 0) {
                TaskId.get().mTaskLog.append("[").append(TaskId.getTaskName(TaskId.get().mCurrentId)).append("]");
            }
            if (TaskId.get().mTaskLog.indexOf(msg) == -1) {
                //不包含
                TaskId.get().mTaskLog.append(msg).append(";");
            }
        }
    }


    /**
     * 只开始任务 不回调 window
     *
     * @param id
     * @return
     */
    public static void onlyStartTask(int id) {
        TaskId.get().mCurrentId = id;
    }

    /**
     * 结束任务
     *
     * @param id
     * @return
     */
    public static void stop(int id) {
        if (TaskId.get().mCurrentId == -1 || TaskId.get().mCurrentId != id) {
            return;
        }
        Logger.d("stop task, id : " + id);
        String message = null;
//        switch (id) {
//            case TaskId.TASK_CUS_SCRIPT:
//                message = "执行完毕。";
//                break;
//        }
        stop(id, message);
    }

    /**
     * 结束任务
     *
     * @return
     */
    public static void stop() {
        stop(TaskId.get().mCurrentId);
    }

    /**
     * 结束任务
     *
     * @param id
     * @return
     */
    public static void stop(int id, String message) {
        Logger.d("call stop task method stack:\n" + Log.getStackTraceString(new Throwable()));
        if (TaskId.get().mCurrentId == id) {
            AppInstance.getInstance().onTaskComplete(message);
            TaskId.get().mEndTime = STime.currenttimemillis();
            if (TaskId.get().mTaskEntity != null) {
                commitTaskResult(TaskId.get().mTaskEntity, TaskId.get().mSuccess, TaskId.get().mTaskLog.toString(), TaskId.get().mStartTime, TaskId.get().mEndTime);
            }
            TaskId.get().mCurrentId = -1;
            TaskId.get().mTaskToggle = false;
            TaskId.get().mTaskEntity = null;
            TaskId.get().mTaskServerId = null;

            //release H.actions
            H.getInstance().releaseActions();

            //stop HandleAccessibilityEventService
            HandleEventService.get().release();

        }
    }

    public static void commitTaskResult(AccessibilityTask entity, boolean success, String log) {
        commitTaskResult(entity, success, log, -1, -1);
    }

    public static void commitTaskResult(AccessibilityTask entity, boolean success, String log, long startTime, long endTime) {
        String taskName = "[" + TaskId.getTaskName(TaskId.get().mCurrentId) + "]";
        Logger.d("任务结束, " + taskName + (success ? "成功, " : "失败, ") + log + entity);
    }

    public static void onAppResume() {
        if (TaskId.get().mCurrentId > 0) {
            onlyStopTask(TaskId.get().mCurrentId);
        }
    }

    /**
     * 只结束任务 不回调 window
     *
     * @param id
     * @return
     */
    public static void onlyStopTask(int id) {
        if (TaskId.get().mCurrentId == id) {
            TaskId.get().mCurrentId = -1;
            TaskId.get().mTaskToggle = false;
        }
    }

    /**
     * 只结束任务 不回调 window
     *
     * @return
     */
    public static void onlyStopTask() {
        onlyStopTask(TaskId.get().mCurrentId);
    }

    /**
     * 结束任务
     *
     * @param id
     * @return
     */
    public static void stop(int id, String message, String leftMenutext, Runnable nextRunnable) {
        if (TaskId.get().mCurrentId == id) {
            AppInstance.getInstance().onTaskComplete(message, leftMenutext, nextRunnable);
            TaskId.get().mCurrentId = -1;
            TaskId.get().mTaskToggle = false;
        }
    }

    public static boolean isRunning() {
        return TaskId.get().mCurrentId > 0;
    }
}
