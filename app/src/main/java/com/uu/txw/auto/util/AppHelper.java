package com.uu.txw.auto.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.uu.txw.auto.task.TaskId;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class AppHelper {


    private AppHelper() {

    }

    private static final AppHelper instance = new AppHelper();

    public static AppHelper init() {
        return instance;
    }

    public static String getFromXml(String xmlmsg, String node) throws XmlPullParserException, IOException {
        String xl = xmlmsg.substring(xmlmsg.indexOf("<msg>"));
        //nativeurl
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser pz = factory.newPullParser();
        pz.setInput(new StringReader(xl));
        int eventType = pz.getEventType();
        String result = "";
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (pz.getName().equals(node)) {
                    pz.nextToken();
                    result = pz.getText();
                    break;
                }
            }
            eventType = pz.next();
        }
        return result;
    }

    public static int getRandom(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    public static void openApp(Context context) {
        try {

            if (TaskId.currScriptTask != null) {
                Intent intent = new Intent();
                ComponentName cmp = new ComponentName(TaskId.currScriptTask.main_package_name, TaskId.currScriptTask.main_package_launcher_ui);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                if (!isAppRunning(context, TaskId.currScriptTask.main_package_name)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setComponent(cmp);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void openApp(Context context, String packageName, String launcherUi) {
        try {
            Intent intent = new Intent();
            ComponentName cmp = new ComponentName(packageName, launcherUi);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            if (!isAppRunning(context, packageName)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setComponent(cmp);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkAppInstall() {
//        List<PackageInfo> installedPackages = AppInstance.getInstance().getTaskListener().provideContext().getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
//        if (TaskId.currScriptTask == null) {
//            for (PackageInfo info : installedPackages) {
//                if (WeUI.MY_PACKAGE_NAME.equals(info.packageName)) {
//
//                    return true;
//                }
//            }
//            return false;
//        } else if (TaskId.currScriptTask.immediatelySingleThreadTask()) {
//            return true;
//        } else {
//            for (PackageInfo info : installedPackages) {
//                if (TaskId.currScriptTask.main_package_name.equals(info.packageName)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//        android 10以上需要权限 默认安装了
        return true;
    }

}
