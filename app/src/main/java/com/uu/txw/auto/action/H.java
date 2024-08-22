package com.uu.txw.auto.action;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEventExt;

import com.uu.txw.auto.annotation.EVENT_CLASS;
import com.uu.txw.auto.annotation.EVENT_CLASSES;
import com.uu.txw.auto.annotation.EVENT_TYPE;
import com.uu.txw.auto.annotation.RUN;
import com.uu.txw.auto.annotation.TASK_IDS;
import com.uu.txw.auto.common.CommonInstance;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.common.utils.MD5Utills;
import com.uu.txw.auto.task.GlobalVar;
import com.uu.txw.auto.task.TaskController;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.task.data.ObtainWindowStateChangeEvent;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import dalvik.system.DexClassLoader;


public class H {

    private ActionService service;
    private SparseArray<List<Method>> mTaskMethods = new SparseArray<>();
    private HashMap<Method, ActionEntity> mActions = new HashMap<>();
    private HashMap<String, ActionEntity> mScriptActions = new HashMap<>();
    //是否匹配到当前真正的ui
    private boolean mMatchCurrentRealUi;

    private static final H ourInstance = new H();
    private long mUpdateTime;

    public static H getInstance() {
        return ourInstance;
    }

    private H() {

    }

    public ActionService createService() {
        if (service == null) {
            service = (ActionService) Proxy.newProxyInstance(ActionService.class.getClassLoader(), new Class<?>[]{ActionService.class},
                    new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable {
                            AccessibilityEventExt event = (AccessibilityEventExt) args[0];
                            String currentUi = event.getClassName().toString();
                            ActionEntity actionAssertEntity = mActions.get(method);
                            if (actionAssertEntity == null) {
                                int type = -1;
                                String className = null;
                                String[] classNames = null;
                                Class<? extends Action> run = method.getAnnotation(RUN.class).value();

                                Annotation[] annotations1 = run.getAnnotations();
                                for (Annotation annotation : annotations1) {
                                    if (annotation instanceof EVENT_TYPE) {
                                        type = ((EVENT_TYPE) annotation).value();
                                    } else if (annotation instanceof EVENT_CLASS) {
                                        className = ((EVENT_CLASS) annotation).value();
                                    } else if (annotation instanceof EVENT_CLASSES) {
                                        classNames = ((EVENT_CLASSES) annotation).value();
                                    }
                                }
                                if (type < 0) {
                                    throw new IllegalArgumentException("类 " + run.getName() + "必须指定一个触发事件的类型 EVENT_TYPE 来过滤触发的事件");
                                }
                                if (TextUtils.isEmpty(className) && (classNames == null || classNames.length == 0)) {
                                    throw new IllegalArgumentException("类 " + run.getName() + "必须指定一个类名 EVENT_CLASS 来过滤触发的事件");
                                }

                                mActions.put(method, actionAssertEntity = new ActionEntity().eventType(type).className(className).classNames(classNames).action(run.newInstance()));

                            }
                            if (actionAssertEntity.getEventType() == event.getEventType() &&
                                    actionAssertEntity.getClassNames() != null ? Arrays.asList(actionAssertEntity.getClassNames()).contains(currentUi) : TextUtils.equals(actionAssertEntity.getClassName(), currentUi)) {
                                Logger.d((actionAssertEntity.getClassNames() != null ? Arrays.toString(actionAssertEntity.getClassNames()) : actionAssertEntity.getClassName()) + "-" + actionAssertEntity.getAction().getClass().getSimpleName());
                                actionAssertEntity.getAction().run(event);
                            }

                            return null;
                        }
                    });
        }
        return service;
    }

    public void excuteServiceMethods(int taskId, AccessibilityEventExt event) {
        try {
            List<Method> methods = mTaskMethods.get(taskId);
            if (methods == null && TaskId.currScriptTask == null) {
                List<Method> taskMethods = new ArrayList<>();
                for (Method method : ActionService.class.getDeclaredMethods()) {
                    int[] task_ids = method.getAnnotation(TASK_IDS.class).value();
                    for (int task_id : task_ids) {
                        if (task_id == taskId) {
                            taskMethods.add(method);
                            break;
                        }
                    }
                }
                mTaskMethods.put(taskId, methods = taskMethods);
            }
            AccessibilityEventExt targetEvent = null;
            if (event.getEventType() == AccessibilityEventExt.TYPE_WINDOW_STATE_CHANGED) {
                //页面切换，用页面特征去指明当前界面
                String currentActivity = AndroidCurrentActivity.getInstance().getCurrentActivity();
                if (!TextUtils.isEmpty(currentActivity)) {
                    if (!TextUtils.equals(currentActivity, event.getClassName())) {
                        if (!mMatchCurrentRealUi) {
                            //连续两次页面不匹配，则放弃第二次 ,并且置标记为true
                            Logger.v("准备执行Actions,识别页面和原始不同" + " ,原始页面：" + event.getClassName() + " ,实际页面：" + currentActivity + " ,这是连续第二次不同，放弃执行这次...");
                            mMatchCurrentRealUi = true;
                        } else {
                            //不是这个event事件中的class，重新生成新的正确的
                            Logger.v("准备执行Actions,识别页面和原始不同" + " ,原始页面：" + event.getClassName() + " ,实际页面：" + currentActivity);
                            mMatchCurrentRealUi = false;
                            targetEvent = ObtainWindowStateChangeEvent.obtainEvent(currentActivity);
                        }
                    } else {
                        //和原来的event事件一样，用原来的
                        Logger.v("准备执行Actions,识别页面和原始相同" + " ,实际页面：" + currentActivity);
                        mMatchCurrentRealUi = true;
                        targetEvent = event;
                    }
                } else {
                    Logger.v("准备执行Actions,识别页面为 null" + " ,原始页面：" + event.getClassName());
                }
            } else {
                //页面切换之外的其他事件，用原来的
                targetEvent = event;
            }
            if (targetEvent != null || (TaskId.currScriptTask != null && TaskId.currScriptTask.singleThreadTask())) {

                long l = STime.currenttimemillis();
                mUpdateTime = Math.max(l, mUpdateTime);
                //只有获取到当前页面才去执行，否则不执行
                if (TaskId.currScriptTask == null) {
                    for (Method method : methods) {
                        method.invoke(H.getInstance().createService(), targetEvent);
                    }
                } else {
                    executeScript(targetEvent);
                }
            }
            //update lastest oprate timestrap
            long l = STime.currenttimemillis();
            mUpdateTime = Math.max(l, mUpdateTime);
        } catch (Exception e) {
            Logger.e(Log.getStackTraceString(e));
            TaskId.stop();
        }
    }

    private void executeScript(AccessibilityEventExt event) throws Exception {
        List<Class<?>> o = null;
        if (TaskId.currScriptTask.script_classes != null && !TaskId.currScriptTask.script_classes.isEmpty()) {
            o = TaskId.currScriptTask.script_classes;
        } else if (TextUtils.isEmpty(TaskId.currScriptTask.script_path)) {
            File file = new File(CommonInstance.getInterface().provideContext().getCacheDir(), TaskId.currScriptTask.script_md5 + ".dex");
            if (file.exists() && TaskId.currScriptTask.script_md5.equals(MD5Utills.getMd5Value(file))) {
                TaskId.currScriptTask.script_path = file.getAbsolutePath();
            } else {
                Logger.e("dexPath empty return");
                return;
            }
        }
        if (o == null) {
            DexClassLoader dexClassLoader = new DexClassLoader(
                    TaskId.currScriptTask.script_path,
                    CommonInstance.getInterface().provideContext().getCacheDir().getAbsolutePath(),
                    null,
                    TaskController.class.getClassLoader()
            );
            String className = "com.uc.android.ScriptImpl";
            Class<?> dynamicClass = dexClassLoader.loadClass(className);
            Method scriptList = dynamicClass.getDeclaredMethod("getScriptList");
            scriptList.setAccessible(true);
            o = (List<Class<?>>) scriptList.invoke(dynamicClass);
            Logger.d("invoke getScriptList " + o);
            TaskId.currScriptTask.script_classes = o;
        }
        if (o != null) {
            for (Object o1 : o) {
                Class<?> clazz = (Class<?>) o1;
                if (ScriptInit.class.isAssignableFrom(clazz)) {
                    if (!ScriptInit.hasInit()) {
                        ScriptInit scriptInit;
                        if (!TextUtils.isEmpty(TaskId.currScriptTask.sequence) && (scriptInit = GlobalVar.localTaskInitMap.get(TaskId.currScriptTask.sequence)) != null) {
                            scriptInit.init();
                        } else {
                            ScriptInit o2 = (ScriptInit) clazz.newInstance();
                            o2.init();
                        }
                    }
                } else {
                    runScript(event, (Class<? extends Action>) o1);
                }
            }
            if (TaskId.currScriptTask.singleThreadTask()) {
                //串行，最后 补充停止任务
                TaskId.stop();
            }
        }
    }

    private void runScript(AccessibilityEventExt event, Class<? extends Action> run) throws Exception {
        String actionClassName = run.getName();
        ActionEntity actionAssertEntity = mScriptActions.get(actionClassName);
        if (actionAssertEntity == null) {
            int type = -1;
            String className = null;
            String[] classNames = null;

            Annotation[] annotations1 = run.getAnnotations();
            for (Annotation annotation : annotations1) {
                if (annotation instanceof EVENT_TYPE) {
                    type = ((EVENT_TYPE) annotation).value();
                } else if (annotation instanceof EVENT_CLASS) {
                    className = ((EVENT_CLASS) annotation).value();
                } else if (annotation instanceof EVENT_CLASSES) {
                    classNames = ((EVENT_CLASSES) annotation).value();
                }
            }
            if (TaskId.currScriptTask.bindUiTask()) {
                if (type < 0) {
                    throw new IllegalArgumentException("类 " + run.getName() + "必须指定一个触发事件的类型 EVENT_TYPE 来过滤触发的事件");
                }
                if (TextUtils.isEmpty(className) && (classNames == null || classNames.length == 0)) {
                    throw new IllegalArgumentException("类 " + run.getName() + "必须指定一个类名 EVENT_CLASS 来过滤触发的事件");
                }
            }


            mScriptActions.put(actionClassName, actionAssertEntity = new ActionEntity().eventType(type).className(className).classNames(classNames).action(run.newInstance()));

        }
        if (TaskId.currScriptTask.bindUiTask()) {
            String currentUi = event.getClassName().toString();
            if (actionAssertEntity.getEventType() == event.getEventType() &&
                    actionAssertEntity.getClassNames() != null ? Arrays.asList(actionAssertEntity.getClassNames()).contains(currentUi) : TextUtils.equals(actionAssertEntity.getClassName(), currentUi)) {
                Logger.d((actionAssertEntity.getClassNames() != null ? Arrays.toString(actionAssertEntity.getClassNames()) : actionAssertEntity.getClassName()) + "-" + actionAssertEntity.getAction().getClass().getSimpleName());
                actionAssertEntity.getAction().run(event);
            }
        } else {
            actionAssertEntity.getAction().run(event);
        }

    }

    public void releaseActions() {
        mActions.clear();
        mScriptActions.clear();
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void updateOpreateTime() {
        long l = STime.currenttimemillis();
        mUpdateTime = Math.max(l, mUpdateTime);
    }

    public void updateOpreateTimeForward(long forwardRange) {
        mUpdateTime = Math.max(STime.currenttimemillis() + forwardRange, mUpdateTime);
    }
}
