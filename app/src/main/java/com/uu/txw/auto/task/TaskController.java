package com.uu.txw.auto.task;

import static com.uu.txw.auto.common.realm.AccessibilityTask.F_N_CONTENT;
import static com.uu.txw.auto.common.realm.AccessibilityTask.F_N_EXT;
import static com.uu.txw.auto.common.realm.AccessibilityTask.F_N_TARGET_NAME;
import static com.uu.txw.auto.common.realm.AccessibilityTask.F_N_TARGET_NAME_LIST;

import android.text.TextUtils;
import android.util.Log;

import com.uu.txw.auto.AppInstance;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.action.ScriptInit;
import com.uu.txw.auto.annotation.TASK_ID;
import com.uu.txw.auto.annotation.TASK_PARAMETERS;
import com.uu.txw.auto.annotation.TASK_PARAMETER_CHECK;
import com.uu.txw.auto.common.CommonInstance;
import com.uu.txw.auto.common.realm.AccessibilityTask;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.common.utils.MD5Utills;
import com.uu.txw.auto.task.data.CusScriptTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TaskController {
    private static Map<Integer, Method> methodMap;
    private static Map<Method, List<String>> methodParameterFieldMap;
    private static Map<Method, Map<String, Function<Object, Boolean>>> methodParameterCheckMap;
    //需要权限的任务列表
    private static List<Integer> taskNeedPermission = Collections.emptyList();
    public static final String CUS_SCRIPT_INIT_STATEMENT_VAR_DELIMITER = "/@/#/";



    public static boolean start(int taskIdParameter, AccessibilityTask task) {
        if (!AppInstance.getInstance().checkAccesibilityAndIdConfig(task)) {
            return false;
        }
        if (TaskId.get().mCurrentId > 0) {
            Logger.v("当前已有任务在执行");
            return false;
        }
        if (methodMap == null) {
            //缓存方法注解
            methodMap = new HashMap<>();
            methodParameterFieldMap = new HashMap<>();
            methodParameterCheckMap = new HashMap<>();
            for (Method method : TaskController.class.getDeclaredMethods()) {
                TASK_ID taskIdAnn = method.getAnnotation(TASK_ID.class);
                if (taskIdAnn != null) {
                    int taskId = taskIdAnn.value();
                    methodMap.put(taskId, method);
                    //参数字段注解
                    TASK_PARAMETERS parameterAnn = method.getAnnotation(TASK_PARAMETERS.class);
                    if (parameterAnn != null) {
                        String[] parameterArr = parameterAnn.value();
                        if (parameterArr.length > 0) {
                            methodParameterFieldMap.put(method, Arrays.asList(parameterArr));
                        }
                    }
                    //自定义参数检查注解
                    TASK_PARAMETER_CHECK[] parameterCheckAnns = method.getAnnotationsByType(TASK_PARAMETER_CHECK.class);
                    if (parameterCheckAnns != null) {
                        Map<String, Function<Object, Boolean>> checkMap = new HashMap<>();
                        for (TASK_PARAMETER_CHECK parameterCheckAnn : parameterCheckAnns) {
                            try {
                                checkMap.put(parameterCheckAnn.fieldName(), parameterCheckAnn.function().newInstance());
                            } catch (Exception e) {
                                Logger.e(Log.getStackTraceString(e));
                            }
                        }
                        methodParameterCheckMap.put(method, checkMap);
                    }
                }
            }
        }
        Method method = methodMap.get(taskIdParameter);
        if (method != null) {
            List<String> parameterList = methodParameterFieldMap.get(method);
            List<Object> parameterValues = new ArrayList<>();
            if (parameterList != null) {
                for (String fieldName : parameterList) {
                    try {
                        Field field = AccessibilityTask.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        parameterValues.add(field.get(task));
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                        TaskId.commitTaskResult(task, false, String.format("没有找到该taskId对应的脚本对应的参数,参数: %s", fieldName));
                        return false;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        TaskId.commitTaskResult(task, false, String.format("获取不到该taskId对应的脚本对应的参数的值,参数: %s", fieldName));
                        return false;
                    }
                }
            }
            //参数检查
            if (parameterList != null) {
                Map<String, Function<Object, Boolean>> parameterCheckMap = methodParameterCheckMap.get(method);
                for (int i = 0; i < parameterList.size(); i++) {
                    String parameter = parameterList.get(i);
                    Object parameterValue = parameterValues.get(i);
                    if (parameterValue == null) {
                        TaskId.commitTaskResult(task, false, parameter + "不能为null");
                        return false;
                    } else {
                        switch (parameter) {
                            case F_N_TARGET_NAME:
                            case F_N_CONTENT:
                            case F_N_EXT:
                                if (TextUtils.isEmpty((String) parameterValue)) {
                                    TaskId.commitTaskResult(task, false, parameter + "不能为空");
                                    return false;
                                }
                                break;
                            case F_N_TARGET_NAME_LIST:
                                if (((List<String>) parameterValue).size() == 0) {
                                    TaskId.commitTaskResult(task, false, F_N_TARGET_NAME_LIST + "不能为空");
                                    return false;
                                }
                                break;
                        }
                        //自定义参数检查注解 检查
                        if (parameterCheckMap != null) {
                            Function<Object, Boolean> checkFunction = parameterCheckMap.get(parameter);
                            if (checkFunction != null) {
                                if (!checkFunction.apply(parameterValue)) {
                                    TaskId.commitTaskResult(task, false, String.format("参数校验错误，请检查：%s=%s[%s]", parameter, parameterValue, checkFunction.getClass().getSimpleName()));
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

            TaskId.get().mTaskServerId = task.getId();
            if(taskIdParameter == TaskId.TASK_CUS_SCRIPT){
                try {
                    TaskId.currScriptTask = task.getCusScriptTask();
                    if (!TextUtils.isEmpty(TaskId.currScriptTask.main_package_main_ui)) {
                        AndroidCurrentActivity.getInstance().addUiConditionNone(TaskId.currScriptTask.main_package_main_ui);
                    }
                } catch (Exception e) {
                    Logger.e(e.getMessage());
                    e.printStackTrace();
                    TaskId.currScriptTask = new CusScriptTask();
                }
            } else {
                TaskId.currScriptTask = null;
            }

            Logger.i("准备执行");
            AppInstance.getInstance().showNotifyToast("准备执行 " + (TaskId.currScriptTask != null ? TaskId.currScriptTask.task_name : ""));
            return AppInstance.getInstance().run(null, taskIdParameter, () -> {
                try {
                    if (parameterValues.size() > 0) {
                        method.invoke(TaskController.class, parameterValues.toArray());
                    } else {
                        method.invoke(TaskController.class);
                    }
                    TaskId.start(taskIdParameter, task);
                } catch (Exception e) {
                    e.printStackTrace();
                    TaskId.commitTaskResult(task, false, String.format("执行该taskId对应的脚本异常,taskId: %s", taskIdParameter));
                    Logger.e(Log.getStackTraceString(e));
                }
            });
        } else {
            TaskId.commitTaskResult(task, false, String.format("没有找到该taskId对应的脚本,taskId: %s", taskIdParameter));
            return false;
        }
    }


    public static void stopTask() {
        TaskId.stop();
    }


    /**
     * 保持
     */
    @TASK_ID(TaskId.TASK_CUS_SCRIPT)
    @TASK_PARAMETERS()
    public static void cusScript() {
        ScriptInit.reset();
        GlobalVar.clear();
        if (TaskId.currScriptTask.initStatementList != null && !TaskId.currScriptTask.initStatementList.isEmpty()) {
            TaskId.currScriptTask.initStatementList.forEach(scriptStatement -> {
                switch (scriptStatement.name) {
                    case "新建变量":
                        if (scriptStatement.args_count == 2 && scriptStatement.args.size() == 2) {
                            GlobalVar.put(String.valueOf(scriptStatement.args.get(0)), String.valueOf(scriptStatement.args.get(1)));
                        }
                        break;
                    case "新建变量-列表":
                        if (scriptStatement.args_count == 2 && scriptStatement.args.size() == 2) {
                            GlobalVar.put(String.valueOf(scriptStatement.args.get(0)), Arrays.asList(String.valueOf(scriptStatement.args.get(1)).split(CUS_SCRIPT_INIT_STATEMENT_VAR_DELIMITER)));
                        }
                        break;
                }
            });
        }

        if (TaskId.currScriptTask.script_clazz != null && !TaskId.currScriptTask.script_clazz.isEmpty()) {
            TaskId.currScriptTask.script_classes(TaskId.currScriptTask.script_clazz.stream().map(s -> {
                try {
                    return Class.forName(s);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));

        } else {
            File file = new File(CommonInstance.getInterface().provideContext().getCacheDir(), TaskId.currScriptTask.script_md5 + ".dex");
            if (file.exists() && TaskId.currScriptTask.script_md5.equals(MD5Utills.getMd5Value(file))) {
                TaskId.currScriptTask.script_path = file.getAbsolutePath();
                Logger.d("file exist " + TaskId.currScriptTask.script_path);
            } else {
                // TODO: 2024/8/21 download
            }
        }
    }

}
