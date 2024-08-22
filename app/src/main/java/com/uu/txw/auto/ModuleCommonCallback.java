package com.uu.txw.auto;

import android.content.Context;

import com.uu.txw.auto.common.CommonInstance;
import com.uu.txw.auto.common.realm.AccessibilityTask;
import com.uu.txw.auto.task.TaskId;


public class ModuleCommonCallback implements CommonInstance.LibInterface {
    private Context context;

    public ModuleCommonCallback(Context context) {
        this.context = context;
    }

    @Override
    public Context provideContext() {
        return this.context;
    }

    @Override
    public AccessibilityTask getCurrTaskEntity() {
        return TaskId.get().mTaskEntity;
    }

    @Override
    public void log2FloatWindow(String msg) {
        FloatWindowUtil.getInstance().updateToastLog(msg);
    }

}
