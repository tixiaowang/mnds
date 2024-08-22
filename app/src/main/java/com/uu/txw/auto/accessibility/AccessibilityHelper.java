package com.uu.txw.auto.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.uu.txw.auto.AppInstance;
import com.uu.txw.auto.DelayKit;
import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.common.utils.DpUtil;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.GlobalVar;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.util.ScreenCaptureUtil;
import com.uu.txw.auto.util.ImageUtils;
import com.uu.txw.auto.util.WeUI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


public class AccessibilityHelper extends DelayKit {

    public static AccessibilityService mService;

    /**
     * 判断辅助服务是否正在运行
     */
    public static boolean isServiceRunning(AccessibilityService service) {
        if (service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if (info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if (!isConnect) {
            return false;
        }
        return true;
    }

    /**
     * 打开辅助服务的设置
     */
    public static void openAccessibilityServiceSettings(Activity context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取text
     */
    public static String getNodeText(String id) {
        List<AccessibilityNodeInfo> unintall_nodes = mService.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(id);
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            return unintall_nodes.get(0).getText().toString().trim();
        }
        return null;
    }

    /**
     * 获取text
     */
    public static String getNodeText(AccessibilityNodeInfo nodeInfo, String id) {
        List<AccessibilityNodeInfo> unintall_nodes = nodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (unintall_nodes != null && !unintall_nodes.isEmpty()) {
            return unintall_nodes.get(0).getText().toString().trim();
        }
        return null;
    }


    //获取根节点
    public static AccessibilityNodeInfo findRootNodeInfo() {
        return mService.getRootInActiveWindow();
    }

    //通过id查找
    public static AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo nodeInfo, String resId) {
        if (nodeInfo == null) return null;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resId);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    //通过id查找
    public static AccessibilityNodeInfo findNodeById(String resId) {
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public static void recycle(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            nodeInfo.recycle();
        }
    }

    //通过id查找
    public static List<AccessibilityNodeInfo> findNodeListById(String resId) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId);
            if (list != null) {
                result.addAll(list);
            }
        }
        return result;
    }

    public static boolean clickOrSelectWhenFoundOnlyOne(String resId) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && list.size() == 1) {
                return performClick(list.get(0));
            }
        }
        return false;
    }

    //通过id查找 ,第i个组件
    public static AccessibilityNodeInfo findNodeById(String resId, int index) {
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId);
            if (list != null && list.size() > index) {
                return list.get(index);
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findNodeByIdText(String resId, String text) {
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByViewId(resId);
            if (list != null) {
                for (AccessibilityNodeInfo accessibilityNodeInfo : list) {
                    if (text.equals(accessibilityNodeInfo.getText().toString()) || text.equals(accessibilityNodeInfo.getContentDescription().toString())) {
                        return accessibilityNodeInfo;
                    }
                }
            }
        }
        return null;
    }

    //通过某个文本查找text或content-desc
    public static AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) return null;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            for (AccessibilityNodeInfo item : list) {
                if (TextUtils.equals(item.getText(), text) || TextUtils.equals(item.getContentDescription(), text)) {
                    return item;
                }
            }
        }
        return null;
    }

    //通过某个文本查找text或content-desc
    public static AccessibilityNodeInfo findNodeByText(String text) {
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByText(text);
            if (list == null || list.isEmpty()) {
                return null;
            } else {
                for (AccessibilityNodeInfo item : list) {
                    if (TextUtils.equals(item.getText(), text) || TextUtils.equals(item.getContentDescription(), text)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    //通过某个文本和className查找 text或content-desc
    public static AccessibilityNodeInfo findNodeByTextAndClassName(String text, String className) {
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByText(text);
            if (list == null || list.isEmpty()) {
                return null;
            } else {
                for (AccessibilityNodeInfo item : list) {
                    if ((TextUtils.equals(item.getText(), text) || TextUtils.equals(item.getContentDescription(), text)) && TextUtils.equals(item.getClassName(), className)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    //通过某个文本查找
    public static AccessibilityNodeInfo findEditTextByHint(String hint) {
        List<AccessibilityNodeInfo> nodeEditTextList = findNodeListDeeplyByClassName(mService.getRootInActiveWindow(), WeUI.CLASS_NAME_EDITVIEW);
        for (AccessibilityNodeInfo nodeEditText : nodeEditTextList) {
            if (TextUtils.equals(nodeEditText.getText(), hint)) {
                return nodeEditText;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findEditText() {
        return AccessibilityHelper.findNodeByClassName(WeUI.CLASS_NAME_EDITVIEW);
    }

    //通过某个文本查找 text或content-desc
    public static List<AccessibilityNodeInfo> findNodeListByText(String text) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        AccessibilityNodeInfo rootInActiveWindow = mService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            List<AccessibilityNodeInfo> list = rootInActiveWindow.findAccessibilityNodeInfosByText(text);
            if (list != null) {
                for (AccessibilityNodeInfo item : list) {
                    if (TextUtils.equals(Optional.ofNullable(item.getText()).orElse("").toString().trim(), text) || TextUtils.equals(Optional.ofNullable(item.getContentDescription()).orElse("").toString().trim(), text)) {
                        result.add(item);
                    }
                }
            }
        }
        return result;
    }

    //通过ClassName查找
    public static AccessibilityNodeInfo findNodeByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        for (int i = 0; nodeInfo != null && i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (node != null) {
                if (className.equals(node.getClassName().toString())) {
                    return node;
                } else if (node.getChildCount() > 0) {
                    AccessibilityNodeInfo nodeInfosByClassName = findNodeByClassName(node, className);
                    if (nodeInfosByClassName != null) {
                        return nodeInfosByClassName;
                    }
                }
            }
        }
        return null;
    }

    //通过ClassName查找 在nodeInfo的下方
    public static AccessibilityNodeInfo findNodeByClassNameBelowNode(AccessibilityNodeInfo nodeInfo, String className) {
        List<AccessibilityNodeInfo> nodeList = findNodeListDeeplyByClassName(mService.getRootInActiveWindow(), className);
        Rect nodeRect = new Rect();
        Rect itemRect = new Rect();
        nodeInfo.getBoundsInScreen(nodeRect);
        int minY = -1;
        AccessibilityNodeInfo finalNode = null;
        for (AccessibilityNodeInfo node : nodeList) {
            node.getBoundsInScreen(itemRect);
            if (nodeRect.bottom <= itemRect.top) {
                //满足在node的下方的条件
                if (minY < 0 || itemRect.top < minY) {
                    minY = itemRect.top;
                    finalNode = node;
                }
            }
        }
        return finalNode;
    }

    //通过ClassName查找 在nodeInfo的上方
    public static AccessibilityNodeInfo findNodeByClassNameAboveNode(AccessibilityNodeInfo nodeInfo, String className) {
        List<AccessibilityNodeInfo> nodeList = findNodeListDeeplyByClassName(mService.getRootInActiveWindow(), className);
        Rect nodeRect = new Rect();
        Rect itemRect = new Rect();
        nodeInfo.getBoundsInScreen(nodeRect);
        int maxY = -1;
        AccessibilityNodeInfo finalNode = null;
        for (AccessibilityNodeInfo node : nodeList) {
            node.getBoundsInScreen(itemRect);
            if (nodeRect.top >= itemRect.bottom) {
                //满足在node的下方的条件
                if (maxY < 0 || itemRect.bottom > maxY) {
                    maxY = itemRect.bottom;
                    finalNode = node;
                }
            }
        }
        return finalNode;
    }

    //通过ClassName查找
    public static List<AccessibilityNodeInfo> findNodeListByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return Collections.EMPTY_LIST;
        }
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for (int i = 0; nodeInfo != null && i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (node != null && className.equals(node.getClassName())) {
                result.add(node);
            }
        }
        return result;
    }

    //通过ClassName查找
    public static List<AccessibilityNodeInfo> findNodeListDeeplyByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (TextUtils.isEmpty(className)) {
            return Collections.EMPTY_LIST;
        }
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        for (int i = 0; nodeInfo != null && i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            if (node != null) {
                if (className.equals(node.getClassName())) {
                    result.add(node);
                }
                if (node.getChildCount() > 0) {
                    result.addAll(findNodeListDeeplyByClassName(node, className));
                }
            }
        }
        return result;
    }

    //通过node寻找 ListView node
    public static AccessibilityNodeInfo findListViewNodeByChild(AccessibilityNodeInfo nodeChild) {
        if (nodeChild == null) {
            return null;
        }
        AccessibilityNodeInfo parent = nodeChild.getParent();
        if (parent != null) {
            if (parent.getClassName().equals(WeUI.CLASS_NAME_LIST_VIEW)) {
                return parent;
            } else {
                return findListViewNodeByChild(parent);
            }
        } else {
            return null;
        }
    }

    //通过ClassName查找
    public static AccessibilityNodeInfo findNodeByClassName(String className) {
        return findNodeByClassName(mService.getRootInActiveWindow(), className);
    }

    /**
     * 找父组件
     */
    public static AccessibilityNodeInfo findParentNodeByClassName(AccessibilityNodeInfo nodeInfo, String className) {
        if (nodeInfo == null) {
            return null;
        }
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        if (className.equals(nodeInfo.getClassName())) {
            return nodeInfo;
        }
        return findParentNodeByClassName(nodeInfo.getParent(), className);
    }

    private static final Field sSourceNodeField;

    static {
        Field field = null;
        try {
            field = AccessibilityNodeInfo.class.getDeclaredField("mSourceNodeId");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sSourceNodeField = field;
    }

    public static long getSourceNodeId(AccessibilityNodeInfo nodeInfo) {
        if (sSourceNodeField == null) {
            return -1;
        }
        try {
            return sSourceNodeField.getLong(nodeInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getViewIdResourceName(AccessibilityNodeInfo nodeInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return nodeInfo.getViewIdResourceName();
        }
        return null;
    }

    //返回HOME界面
    public static boolean performHome() {
        if (AccessibilityHelper.mService == null) {
            return false;
        }
        return AccessibilityHelper.mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }


    //返回
    public static void performBack(AccessibilityService service) {
        if (service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    //返回
    public static boolean performBack() {
        //默认使用MainService
        if (AccessibilityHelper.mService == null) {
            return false;
        }
        return AccessibilityHelper.mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    public static boolean performBackAppHome() {
        return performBackAppHome(30);
    }

    public static boolean performBackAppHome(int count) {
        if (AndroidCurrentActivity.isMainUi()) {
            return true;
        } else if (count > 0 && TaskId.get().mCurrentId > 0) {
            performBack();
            DelayKit.sleepLL();
            return performBackAppHome(--count);
        } else {
            return false;
        }
    }

    //返回
    public static boolean performBack(String uiClassName) {
        if (AndroidCurrentActivity.isCurrentActivity(uiClassName)) {
            return performBack();
        }
        return false;
    }

    /**
     * 点击事件
     */
    public static boolean performClick(AccessibilityNodeInfo nodeInfo) {
        String tag = "点击";
        if (nodeInfo == null) {
            Logger.scriptE(tag, "节点未找到");
            return false;
        }
        if (nodeInfo.isClickable()) {
            boolean b = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!b) {
                Logger.scriptE(tag, "点击原始节点失败");
                b = performGestureClick(nodeInfo);
            }
            return b;
        } else {
            boolean b = performClick(nodeInfo.getParent());
            if (!b) {
                Logger.scriptE(tag, "点击父节点失败");
            }
            return b;
        }
    }

    public static boolean performGestureClick(String id, String text) {
        return performGestureClick(findNodeByIdText(id, text));
    }

    /**
     * 点击事件
     */
    public static boolean performGestureClick(AccessibilityNodeInfo nodeInfo) {
        try {
            String tag = "手势点击";
            if (nodeInfo == null || mService == null) {
                Logger.scriptE(tag, "节点未找到");
                return false;
            }
            Rect outBounds = new Rect();
            nodeInfo.getBoundsInScreen(outBounds);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path p = new Path();
            float x = outBounds.left + outBounds.width() / 2f + new Random().nextFloat() + new Random().nextInt(5);
            float y = outBounds.top + outBounds.height() / 2f + new Random().nextFloat() + new Random().nextInt(3);
            p.moveTo(x, y);
            builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 100L + new Random().nextInt(100)));
            GestureDescription gesture = builder.build();
            return mService.dispatchGesture(gesture, null, null);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean performGestureClick(String id) {
        return performGestureClick(findNodeById(id));
    }

    public static boolean performGestureClickForeachKeyWithText(String setName, String id) {
        List<String> textList = GlobalVar.getTextList(setName);
        List<AccessibilityNodeInfo> nodeListInfosById = AccessibilityHelper.findNodeListById(id);
        for (AccessibilityNodeInfo accessibilityNodeInfo : nodeListInfosById) {
            String nodeText = getNodeText(accessibilityNodeInfo);
            if (!TextUtils.isEmpty(nodeText)) {
                if (!textList.contains(nodeText)) {
                    AccessibilityHelper.performGestureClick(accessibilityNodeInfo);
                    textList.add(nodeText);
                    return true;
                }
            }
        }
        return false;
    }

    public static String getNodeText(AccessibilityNodeInfo nodeInfo) {
        CharSequence text = nodeInfo.getText();
        CharSequence description = nodeInfo.getContentDescription();
        if (text != null && !TextUtils.isEmpty(text.toString())) {
            return text.toString();
        }
        if (description != null && !TextUtils.isEmpty(description.toString())) {
            return description.toString();
        }
        return "";
    }

    public static boolean performGestureClickButtonByText(String buttonText) {
        return performGestureClick(findNodeByTextAndClassName(buttonText, "android.widget.Button"));
    }

    public static boolean performGestureClickByText(String text) {
        List<AccessibilityNodeInfo> nodeInfosListByText = findNodeListByText(text);
        for (AccessibilityNodeInfo accessibilityNodeInfo : nodeInfosListByText) {
            if (!"android.widget.EditText".equals(accessibilityNodeInfo.getClassName().toString())) {
                return performGestureClick(accessibilityNodeInfo);
            }
        }
        return false;
    }

    public static boolean performGestureClickByIdAndText(String id, String text) {
        List<AccessibilityNodeInfo> nodeListInfosById = findNodeListById(id);
        for (AccessibilityNodeInfo accessibilityNodeInfo : nodeListInfosById) {
            if (TextUtils.equals(text, accessibilityNodeInfo.getText()) || TextUtils.equals(text, accessibilityNodeInfo.getContentDescription())) {
                return performGestureClick(accessibilityNodeInfo);
            }
        }
        return false;
    }

    public static boolean performGestureClickEditText() {
        return performGestureClick(findNodeByClassName("android.widget.EditText"));

    }

    /**
     * 通过node的坐标长按
     */
    public static void performGestureLongClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null || mService == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Rect outBounds = new Rect();
            nodeInfo.getBoundsInScreen(outBounds);

            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(outBounds.left + outBounds.width() / 2f, outBounds.top + outBounds.height() / 2f);
            builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 1000L));
            GestureDescription gesture = builder.build();
            mService.dispatchGesture(gesture, null, null);
        }
    }

    public static void performGestureLongClick(int x, int y) {
        if (mService == null) {
            return;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(x, y);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 2000L));
        GestureDescription gesture = builder.build();
        mService.dispatchGesture(gesture, null, null);
    }

    /**
     * 通过xy坐标滑动屏幕
     */
    public static boolean performGestureScrollForward(AccessibilityNodeInfo nodeInfo) {
        if (mService == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(200, 1500);
            p.lineTo(200, 500);
//            p.moveTo(outBounds.left + outBounds.width() / 2f, outBounds.top + outBounds.height() / 2f);
//            p.lineTo(outBounds1.left + outBounds.width() / 2f, outBounds1.top + outBounds.height() / 2f);
            builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 200L));
            GestureDescription gesture = builder.build();
            return mService.dispatchGesture(gesture, null, null);
        } else {
            return perform_scroll_forward(nodeInfo);
        }

    }

    /**
     * 通过xy坐标滑动屏幕
     */
    public static boolean performGestureScrollForward() {
        if (mService == null) {
            return false;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
//        Path p = new Path();
        Random random = new Random();
        float startX = 520f + random.nextInt(200) + random.nextFloat();
        float endX = 620f + random.nextInt(100) + random.nextFloat();

        float startY = DpUtil.getScreenHeight() * 7f / 9 - 300 + random.nextInt(200) + random.nextFloat();
        float endY = DpUtil.getScreenHeight() / 5f - 200 + random.nextInt(200) + random.nextFloat();
        int pointCount = (int) (30 + Math.random() * 30); // 随机点的数量

        Path p = createRandomPath(startX, startY, endX, endY, pointCount);

//        int count = 50;
//        p.moveTo(startX, startY);
//        float diffX = (endX - startX) / count;
//        float diffY = (endY - startY) / count;
//        for (int i = 1; i < count; i++) {
//            p.rLineTo(diffX + random.nextFloat(), diffY + random.nextFloat());
//        }

//            p.moveTo(outBounds.left + outBounds.width() / 2f, outBounds.top + outBounds.height() / 2f);
//            p.lineTo(outBounds1.left + outBounds.width() / 2f, outBounds1.top + outBounds.height() / 2f);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, pointCount * 4L));
        GestureDescription gesture = builder.build();
        return mService.dispatchGesture(gesture, null, null);
    }



    public static boolean performGestureScrollLeft() {
        if (mService == null) {
            return false;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
//        Path p = new Path();
        Random random = new Random();
        float startX = DpUtil.getScreenWidth() * 9/10f - 20 + random.nextInt(20) + random.nextFloat();
        float endX = DpUtil.getScreenWidth()/10f - 50 + random.nextInt(50) + random.nextFloat();

        float startY = DpUtil.getScreenHeight() / 3f - 50 + random.nextInt(100) + random.nextFloat();
        float endY = DpUtil.getScreenHeight() / 3f - 50 + random.nextInt(100) + random.nextFloat();
        int pointCount = (int) (50 + Math.random() * 30); // 随机点的数量

        Path p = createRandomPath(startX, startY, endX, endY, pointCount);

//        int count = 50;
//        p.moveTo(startX, startY);
//        float diffX = (endX - startX) / count;
//        float diffY = (endY - startY) / count;
//        for (int i = 1; i < count; i++) {
//            p.rLineTo(diffX + random.nextFloat(), diffY + random.nextFloat());
//        }

//            p.moveTo(outBounds.left + outBounds.width() / 2f, outBounds.top + outBounds.height() / 2f);
//            p.lineTo(outBounds1.left + outBounds.width() / 2f, outBounds1.top + outBounds.height() / 2f);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, pointCount * 15L));
        GestureDescription gesture = builder.build();
        return mService.dispatchGesture(gesture, null, null);
    }

    private static Path createRandomPath(float startX, float startY, float endX, float endY, int pointCount) {
        Path path = new Path();
        path.moveTo(startX, startY);

        for (int i = 1; i < pointCount; i++) {
            float randomX = startX + (endX - startX) * i / (float) pointCount + (float) Math.random() * 20 - 10;
            float randomY = startY + (endY - startY) * i / (float) pointCount + (float) Math.random() * 20 - 10;
            if (i > pointCount * 7 / 8 && i % 2 == 0) {
                path.lineTo(randomX, randomY);
            }
        }
//        path.lineTo(endX, endY);
        return path;
    }


    public static boolean performGestureScrollBack() {
        if (mService == null) {
            return false;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Random random = new Random();
        float endX = 320f + random.nextInt(200) + random.nextFloat();
        float startX = 520f + random.nextInt(200) + random.nextFloat();
        float endY = 1400f + random.nextInt(200) + random.nextFloat();
        float startY = 600f + random.nextInt(200) + random.nextFloat();
        int pointCount = (int) (30 + Math.random() * 50); // 随机点的数量
        Path p = createRandomPath(startX, startY, endX, endY, pointCount);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, pointCount * 5L));
        GestureDescription gesture = builder.build();
        return mService.dispatchGesture(gesture, null, null);
    }

    public static boolean performGestureScrollRandom() {
        if (mService == null) {
            return false;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Random random = new Random();
        float startX = 420f + random.nextInt(200) + random.nextFloat();
        float startY = 1000f + random.nextInt(200) + random.nextFloat();
        int pointCount = (int) (30 + Math.random() * 50); // 随机点的数量

        Path path = new Path();
        path.moveTo(startX, startY);

        for (int i = 1; i < pointCount; i++) {
            float randomX = (float) Math.random() * 20 - 10;
            float randomY = (float) Math.random() * 20 - 10;
            path.rLineTo(randomX, randomY);
        }

        builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, pointCount * 5L));
        GestureDescription gesture = builder.build();
        return mService.dispatchGesture(gesture, null, null);
    }

    /**
     * 点击事件
     */
    public static void performGestureClick(float x, float y) {
        if (mService == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path p = new Path();
            p.moveTo(x, y);
            builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 100L + new Random().nextInt(50)));
            GestureDescription gesture = builder.build();
            mService.dispatchGesture(gesture, null, null);
        }
    }

    public static void performGestureClickScreenRate(float offXRate, float offYRate) {
        int width = DpUtil.getScreenWidth();
        int height = DpUtil.getScreenHeight();
        performGestureClick(width * offXRate + new Random().nextFloat() * 2, height * offYRate + new Random().nextFloat() * 2);
    }

    public static void performGestureClickByOffSetXY(String offX, float x, String offY, float y) {
        if (mService == null) {
            return;
        }
        //left right / top bottom
        float realX;
        if (TextUtils.isEmpty(offX) || "left".equals(offX)) {
            realX = x;
        } else {
            realX = DpUtil.getScreenWidth() - x;
        }

        float realY;
        if (TextUtils.isEmpty(offY) || "top".equals(offY)) {
            realY = y;
        } else {
            realY = DpUtil.getScreenHeight() - y;
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        p.moveTo(realX, realY);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 100L));
        GestureDescription gesture = builder.build();
        mService.dispatchGesture(gesture, null, null);
    }

    public static void performGesture(List<float[]> points, long duration) {
        if (mService == null) {
            Logger.e("performGesture fail mService == null");
            return;
        }
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        boolean hasMove = false;
        for (float[] point : points) {
            if (!hasMove) {
                hasMove = true;
                p.moveTo(point[0], point[1]);
            } else {
                p.lineTo(point[0], point[1]);
            }
        }
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, duration));
        GestureDescription gesture = builder.build();
        mService.dispatchGesture(gesture, null, null);
        Logger.i("performGesture " + points.stream().map(floats -> String.format("[%s, %s]", floats[0], floats[1])).collect(Collectors.joining("")) + " " + duration);
    }

    /**
     * 点击事件
     */
    public static boolean performClickResultBoolean(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        if (nodeInfo.isClickable()) {
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            return performClickResultBoolean(nodeInfo.getParent());
        }
    }

    /**
     * 点击事件
     */
    public static boolean performClick(String id) {
        return performClick(findNodeById(id));
    }

    public static boolean performClick(String id, String text) {
        return performClick(findNodeByIdText(id, text));
    }

    //长按事件
    public static void performLongClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
    }

    //move 事件
    public static void performMoveDown(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.getId());
    }


    //ACTION_SCROLL_FORWARD 事件
    public static boolean perform_scroll_forward(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    //ACTION_SCROLL_BACKWARD 后退事件
    public static boolean perform_scroll_backward(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return false;
        }
        return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    //粘贴
    @TargetApi(18)
    public static void performPaste(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
    }

    //设置editview text
    public static boolean performSetText(AccessibilityNodeInfo nodeInfo, String text) {
        String tag = "设置文本";
        if (nodeInfo == null) {
            Logger.scriptE(tag, "EditText节点未找到");
            return false;
        }

        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        boolean b = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        if (!b) {
            Logger.scriptE(tag, "失败");
        }
        return b;
    }

    public static boolean performSetText(String id, String text) {
        List<AccessibilityNodeInfo> nodeInfoList = findNodeListById(id);
        AccessibilityNodeInfo nodeInfo = null;
        for (AccessibilityNodeInfo accessibilityNodeInfo : nodeInfoList) {
            if ("android.widget.EditText".equals(accessibilityNodeInfo.getClassName().toString())) {
                nodeInfo = accessibilityNodeInfo;
                break;
            }
        }
        return performSetText(nodeInfo, text);
    }

    public static boolean performSetText(String text) {
        AccessibilityNodeInfo nodeInfosByClassName = findNodeByClassName("android.widget.EditText");
        return performSetText(nodeInfosByClassName, text);
    }

    //设置editview text
    public static boolean performSetTextReturnBoolean(AccessibilityNodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return false;
        }
        CharSequence className = nodeInfo.getClassName();
        if ("android.widget.EditText".equals(className)) {//||"android.widget.TextView".equals(className)
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
        return false;
    }


    public static boolean performClickImg(int[] bound, String base64String) {
        try {
            if (base64String.startsWith("data:")) {
                base64String = base64String.substring(base64String.indexOf("base64,") + "base64,".length());
            }
            Bitmap screenBitmap = ScreenCaptureUtil.getInstance().captureScreen(AppInstance.getInstance().provideContext());
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Rect rect = ImageUtils.imageCompareByPix(bound, screenBitmap, BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));
            if (rect != null) {
                AccessibilityHelper.performGestureClick(rect.centerX(), rect.centerY());
                Logger.scriptD("点击图片成功", String.valueOf(rect.centerX()), String.valueOf(rect.centerY()));
                return true;
            }
        } catch (Exception e) {
            Logger.e(Log.getStackTraceString(e));
            e.printStackTrace();
        }
        Logger.scriptE("点击图片失败");
        return false;
    }

    public static boolean performClickImg(String base64String) {
        return performClickImg(null, base64String);
    }

    public static void stop() {
        TaskId.stop();
    }


    public static void showNotifyToast(String s) {
        AppInstance.getInstance().showNotifyToast(s);
    }

    public static boolean isRunning() {
        return TaskId.isRunning();
    }

    public static int random(String start, String end) {
        try {
            if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
                int bound = Integer.parseInt(end);
                int i = Integer.parseInt(start);
                return i + new Random().nextInt(bound - i);
            }
        } catch (Exception e) {

        }
        return 0;
    }

    public static int random(String period) {
        try {
            if (!TextUtils.isEmpty(period) && period.contains("-")) {
                String[] split = period.split("-");
                if (split.length == 2) {
                    int bound = Integer.parseInt(split[1]);
                    int i = Integer.parseInt(split[0]);
                    return i + new Random().nextInt(bound - i);
                }
            }

        } catch (Exception e) {

        }
        return 0;
    }

    public static String random(List<String> list) {
        if (list != null && list.size() > 0) {
            return list.get(new Random().nextInt(list.size()));
        }
        return "";
    }
}
