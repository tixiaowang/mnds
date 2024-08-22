package com.uu.txw.auto.util;

import android.widget.Toast;

import com.uu.txw.auto.AppInstance;


public class ToastUtil {
    public static void show(String message) {
        Toast.makeText(AppInstance.getInstance().provideContext() ,message ,Toast.LENGTH_SHORT).show();
    }
}
