package com.uu.txw.auto.action;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.uu.txw.auto.AppInstance;
import com.uu.txw.auto.accessibility.AccessibilityHelper;
import com.uu.txw.auto.annotation.UI_CONDITION;
import com.uu.txw.auto.annotation.UI_CONDITION_NONE;
import com.uu.txw.auto.annotation.UI_TITLE;
import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.condition.Condition;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.util.WeUI;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AndroidCurrentActivity {

    private static final AndroidCurrentActivity ourInstance = new AndroidCurrentActivity();
    private static boolean mHasSu;
    private long mUpdateTIme;
    private boolean manualUpdate;//更新是否是手动更新
    private String mCurrentClassName;

    //通过title一个条件就能判断的页面<className ,title>
    private HashMap<String, String> mTitleUi = new HashMap<>();

    //通过condition一个条件就能判断的页面<className ,condition>
    private HashMap<String, Condition> mConditionUi = new HashMap<>();

    //没有特征的页面或者dialog
    private HashSet<String> mConditionNoneUi = new HashSet<>();


    public static AndroidCurrentActivity getInstance() {
        return ourInstance;
    }

    private AndroidCurrentActivity() {

    }

    public void initUi(Class<?> uiConfClass) {
        if (uiConfClass == null) {
            return;
        }
        Field[] fields = uiConfClass.getDeclaredFields();
        for (Field field : fields) {
            analysisAnnatation(field);
        }
    }

    private void analysisAnnatation(Field field) {
        try {
            UI_TITLE titleAnnotation = field.getAnnotation(UI_TITLE.class);
            if (titleAnnotation != null) {
                String titleValue = titleAnnotation.value();
                if (!TextUtils.isEmpty(titleValue)) {
                    mTitleUi.put((String) field.get(WeUI.class), titleValue);
                }
            } else {
                UI_CONDITION conditionAnnotation = field.getAnnotation(UI_CONDITION.class);
                if (conditionAnnotation != null) {
                    mConditionUi.put((String) field.get(WeUI.class), conditionAnnotation.value().newInstance());
                } else {
                    UI_CONDITION_NONE conditionNoneAnnotation = field.getAnnotation(UI_CONDITION_NONE.class);
                    if (conditionNoneAnnotation != null) {
                        mConditionNoneUi.add((String) field.get(WeUI.class));
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(Log.getStackTraceString(e));
        }
    }

    public void addUiConditionNone(String uiName) {
        mConditionNoneUi.add(uiName);
    }

    public long getUpdateTime() {
        return mUpdateTIme;
    }

    //上次更新是否是手动的
    public boolean isManualForTheLast() {
        return manualUpdate;
    }

    public void updateCurrent(String className) {
        manualUpdate = false;
        mUpdateTIme = STime.currenttimemillis();
        mCurrentClassName = className;
        AppInstance.getInstance().onWindowChange(className);
    }

    public void updateCurrentManually(String className) {
        updateCurrent(className);
        manualUpdate = true;
    }

    private HashMap<String, String> getTitleUiMap() {
        return mTitleUi;
    }

    private HashMap<String, Condition> getConditionUiMap() {
        return mConditionUi;
    }

    private HashSet<String> getConditionNoneUiSet() {
        return mConditionNoneUi;
    }

    public String getCurrentActivity() {
        String currentActivityReal = getCurrentActivityReal();
        Logger.v("getCurrentActivity: " + currentActivityReal);
        return currentActivityReal;
    }

    private String getCurrentActivityReal() {
//        优先从Accessibility触发的界面去判断，不行的话再去全局搜索
        String accessibilityUi = getAccessibilityUi();
        if (isCurrentActivity(accessibilityUi)) {
            return accessibilityUi;
        }
        if (TaskId.currScriptTask == null || TaskId.currScriptTask.bindUiTask()) {
            //不再采用系统触发的页面（不准确），而是根据特征获取当前页面
            AccessibilityNodeInfo nodeTitle = AccessibilityHelper.findNodeById(WeUI.ID_ACTIVITY_TITLE_BAR_TITLE);
            if (nodeTitle != null) {
                for (Map.Entry<String, String> titleEntry : mTitleUi.entrySet()) {
                    if (TextUtils.equals(titleEntry.getValue(), nodeTitle.getText())) {
                        return titleEntry.getKey();
                    }
                }
            }
            //通过标题没有筛选出来，开始从condition筛选
            for (Map.Entry<String, Condition> conditionEntry : mConditionUi.entrySet()) {
                if (conditionEntry.getValue().c()) {
                    return conditionEntry.getKey();
                }
            }
        }
        return null;
    }

    public static boolean isMainUi() {
        if (TaskId.currScriptTask != null) {
            return isCurrentActivity(TaskId.currScriptTask.main_package_main_ui);
        } else {
            return false;
        }
    }

    public static boolean isCurrentActivity(String className) {
        if (AndroidCurrentActivity.getInstance().getConditionNoneUiSet().contains(className)) {
            boolean equals = TextUtils.equals(className, getAccessibilityUi()) && isAppActivity();
            //和最后一个accessibility接收的页面相同，并且是App页面
            Logger.v("是否是当前页面[equal]：" + equals + " ," + className);
            return equals;
        }
        String title = AndroidCurrentActivity.getInstance().getTitleUiMap().get(className);
        if (!TextUtils.isEmpty(title)) {
            AccessibilityNodeInfo nodeTitle = AccessibilityHelper.findNodeById(WeUI.ID_ACTIVITY_TITLE_BAR_TITLE);
            boolean b = nodeTitle != null && TextUtils.equals(title, nodeTitle.getText());
            Logger.v("是否是当前页面[title]：" + b + " ," + className);
            return b;
        }
        Condition condition = AndroidCurrentActivity.getInstance().getConditionUiMap().get(className);
        if (condition != null) {
            boolean c = condition.c();
            Logger.v("是否是当前页面[condi]：" + c + " ," + className);
            return c;
        }
        String accessibilityUi = getAccessibilityUi();
        boolean b = TextUtils.equals(className, accessibilityUi) && isAppActivity();
        Logger.v("是否是当前页面[compare accessibility ui]: " + b + " ," + className + ", accessibility: " + accessibilityUi);
        return b;
    }

    /**
     * 获取由Accessibility触发的当前页面
     *
     * @return
     */
    public static String getAccessibilityUi() {
        String mCurrentClassName1 = AndroidCurrentActivity.getInstance().mCurrentClassName;
        Logger.v("getAccessibilityUi: " + mCurrentClassName1);
        return mCurrentClassName1;
    }

    public static boolean isAppActivity() {
        if (TaskId.currScriptTask != null) {
            return isAppActivity(TaskId.currScriptTask.main_package_name);
        } else {
            return isAppActivity(WeUI.MY_PACKAGE_NAME);
        }
    }

    public static boolean isAppActivity(String pkgName) {
        AccessibilityNodeInfo rootNodeInfo = AccessibilityHelper.findRootNodeInfo();
        if (rootNodeInfo == null) return false;
        CharSequence packageName = rootNodeInfo.getPackageName();
        Logger.v("rootNodeInfo.packageName: " + packageName);
        return TextUtils.equals(pkgName, packageName);
    }

    /**
     * 注意：shell会耗时
     */
    public static String updateCurrentByShell() {
        //没有root权限，那么不能通过shell获取到，目前只能通过rootInfo为空的判断
        if (AccessibilityHelper.findRootNodeInfo() == null) {
//            getInstance().updateCurrentManually(WeUI.UI_WEB_VIEW);
//            return WeUI.UI_WEB_VIEW;
        }
        return AndroidCurrentActivity.getAccessibilityUi();
    }
}
