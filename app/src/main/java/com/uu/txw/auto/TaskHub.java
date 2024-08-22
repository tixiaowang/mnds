package com.uu.txw.auto;

import android.app.Activity;

import com.uu.txw.auto.action.Action;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.realm.AccessibilityTask;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.TaskController;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.task.data.CusScriptTask;
import com.uu.txw.auto.task.data.ScriptStatement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskHub {
    //load base component
    static {
        try {
            Class.forName(AndroidCurrentActivity.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> mTaskIdMap = new HashMap<>();

    static {
        mTaskIdMap.put("999", TaskId.TASK_CUS_SCRIPT);//自定义脚本
    }

    public static boolean start(Activity checkPermissionActivity, String type, String task_name, String main_package_name, String main_package_launcher_ui,
                                String main_package_main_ui, List<ScriptStatement> initStatementList, List<Class<? extends Action>> actionClassList) {
        if (checkPermissionActivity != null) {
            if (!AppInstance.getInstance().checkAccesibilityAndIdConfig(false, checkPermissionActivity)) {
                return false;
            }
        }

        return start(new AccessibilityTask()
                .cusScriptTask(new CusScriptTask()
                        .type(type)
                        .task_name(task_name)
                        .main_package_name(main_package_name)
                        .main_package_launcher_ui(main_package_launcher_ui)
                        .main_package_main_ui(main_package_main_ui)
                        .initStatementList(initStatementList)
                        .script_clazz(actionClassList.stream().map(Class::getName).collect(Collectors.toList())))
                .id("" + System.currentTimeMillis())
                .type(TaskId.TASK_CUS_SCRIPT + "")
                .priority(1)
                .localId(UUID.randomUUID().toString())
                .localTime(STime.currenttimemillis()));
    }

    private static boolean start(AccessibilityTask task) {
        if (TaskId.get().mCurrentId > 0) {
            //有任务在执行，返回
            Logger.d("有任务在执行，返回");
            return false;
        }

        if (!"-1".equals(task.getType())) {
            //检查任务创建时间，超时1分钟，放弃执行
            Integer taskType = mTaskIdMap.get(task.getType());
            if (task.getCreateTime() > 0) {
                if (taskType != null && System.currentTimeMillis() - task.getCreateTime() > 60_000) {
                    TaskId.commitTaskResult(task, false, "已过任务创建1分钟，放弃执行");
                    return false;
                }
            }

            //-1为任务心跳，不用理睬
            Logger.d("尝试执行任务 : \n" + task);
            Integer integer = taskType;
            if (integer != null) {
                return TaskController.start(integer, task);
            } else {
                String unknowTypeMsg = "未知的任务类型，type: " + task.getType();
                TaskId.commitTaskResult(task, false, unknowTypeMsg);
                return false;
            }
        }
        return false;
    }

}
