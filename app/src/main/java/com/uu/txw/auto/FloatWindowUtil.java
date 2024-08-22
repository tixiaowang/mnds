package com.uu.txw.auto;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static android.view.animation.Animation.RESTART;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEventExt;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.uu.txw.auto.action.AndroidCurrentActivity;
import com.uu.txw.auto.common.utils.DpUtil;
import com.uu.txw.auto.common.utils.Logger;
import com.uu.txw.auto.task.TaskController;
import com.uu.txw.auto.task.TaskId;
import com.uu.txw.auto.task.data.ObtainWindowStateChangeEvent;
import com.uu.txw.auto.util.WeUI;
import com.yhao.floatwindow.FloatWindow;
import com.yhao.floatwindow.MoveType;
import com.yhao.floatwindow.Screen;
import com.yhao.floatwindow.ViewStateListenerAdapter;

import java.util.LinkedList;


public class FloatWindowUtil {
    //    共提供 4 种 MoveType :
//
//    MoveType.slide : 可拖动，释放后自动贴边 （默认）
//
//    MoveType.back : 可拖动，释放后自动回到原位置
//
//    MoveType.active : 可拖动
//
//    MoveType.inactive : 不可拖动
    private static final FloatWindowUtil ourInstance = new FloatWindowUtil();
    private final Handler mHandler;
    private final Handler mShortNotifyHandler;
    private Context mContext;
    private String shortDesc;
    private String taskName;
    private Runnable runnable;
    private int taskId;
    private boolean taskState;
    private int[] mMenuCurLocation = new int[2];
    private int[] mMenuCurLocation2 = new int[2];
    private int controlBtnLastMoveY;

    public boolean isTryShowStartControl() {
        return mTryShowStartControl;
    }

    //点击开始任务btn 到App页面显示悬浮开始按钮之间
    private boolean mTryShowStartControl;

    public static FloatWindowUtil getInstance() {
        return ourInstance;
    }

    private FloatWindowUtil() {
        mHandler = new Handler(Looper.getMainLooper());
        mShortNotifyHandler = new Handler(Looper.getMainLooper());
    }

    public void startTask(int taskId, Runnable runnable) {
        this.mTryShowStartControl = true;
        this.runnable = runnable;
        this.taskId = taskId;
        if (FloatWindow.get() == null) {
            return;
        }
        mHandler.post(() -> {

            ((ImageView) FloatWindow.get().getView().findViewById(R.id.iv_task_status)).setImageResource(R.mipmap.icon_control_start);
            if (AppInstance.showStartControl(taskId)) {
                //只有需要用户主动点击 才主动显示
                showWindow("default_float_window_tag");
            }
        });
    }

    public void taskStarted(String shortDesc, int taskId) {
        this.logList.clear();
        this.taskName = shortDesc;
//        this.shortDesc = shortDesc;
        this.taskState = true;
        if (FloatWindow.get() == null) {
            Logger.e("FloatWindow.get() == null");
            return;
        }
        mHandler.post(() -> {

//            ((TextView) FloatWindow.get().getView().findViewById(R.id.tv_task_status_desc)).setText("结束".concat(shortDesc));
            ((ImageView) FloatWindow.get().getView().findViewById(R.id.iv_task_status)).setImageResource(R.mipmap.icon_control_stop);
            showWindow("default_float_window_tag");
            if (FloatWindow.get("short_toast") != null && !FloatWindow.get("short_toast").isShowing()) {
                showToast(null);
            }
        });
    }

    public void completeTask(String completeMessage) {
        taskState = false;
        if (FloatWindow.get() == null) {
            return;
        }
        mHandler.post(() -> {

            ((ImageView) FloatWindow.get().getView().findViewById(R.id.iv_task_status)).setImageResource(R.mipmap.icon_control_start);
            if (!TextUtils.isEmpty(completeMessage) && FloatWindow.get("toast") != null) {
                ((TextView) FloatWindow.get("toast").getView().findViewById(R.id.tv_message)).setText(completeMessage);
                ((TextView) FloatWindow.get("toast").getView().findViewById(R.id.tv_message_title)).setText("完成");
                FloatWindow.get("toast").getView().findViewById(R.id.tv_custom_menu).setVisibility(View.GONE);
                FloatWindow.get("toast").show();
            }
            if (!AppInstance.showStartControl(taskId)) {
                //主动显示开始任务按钮的 任务 任务结束 不去隐藏开始按钮
                hideWindow("default_float_window_tag");

                //主动显示开始任务按钮的 任务 任务结束 不去清空runnable
                runnable = null;
            }
            hideWindow("short_toast");
        });
    }

    public void onAppResume() {
        //不是retry阶段， 也就是点击开始任务按钮到悬浮开始按钮显示出来的阶段
        if (runnable != null && !mTryShowStartControl) {
            Logger.i("onAppResume...");
            //不去停止任务了
//            TaskId.onAppResume();
//            taskState = false;
//            runnable = null;
//            FloatWindow.hideAll();
        }
    }

    public void completeTask(String completeMessage, String leftMenuText, Runnable nextRunnable) {
        taskState = false;
        if (FloatWindow.get() == null) {
            return;
        }
        mHandler.post(() -> {

//            ((TextView) FloatWindow.get().getView().findViewById(R.id.tv_task_status_desc)).setText("开始".concat(shortDesc));
            ((ImageView) FloatWindow.get().getView().findViewById(R.id.iv_task_status)).setImageResource(R.mipmap.icon_control_start);
            if (!TextUtils.isEmpty(completeMessage)) {
                ((TextView) FloatWindow.get("toast").getView().findViewById(R.id.tv_message)).setText(completeMessage);
                ((TextView) FloatWindow.get("toast").getView().findViewById(R.id.tv_message_title)).setText(String.format("%s完成", taskName));
                if (!TextUtils.isEmpty(leftMenuText) && nextRunnable != null) {
                    TextView tvCustomMenu = (TextView) FloatWindow.get("toast").getView().findViewById(R.id.tv_custom_menu);
                    tvCustomMenu.setVisibility(View.VISIBLE);
                    tvCustomMenu.setText(leftMenuText);
                    tvCustomMenu.setOnClickListener(v -> {
                        FloatWindow.get("toast").hide();
                        showWindow("default_float_window_tag");
                        nextRunnable.run();
                        //主动点击开始window 按钮 开始任务
                        FloatWindow.get().getView().performClick();
                    });
                }
                FloatWindow.get("toast").show();
                runnable = null;
            }
            hideWindow("default_float_window_tag");
            hideWindow("short_toast");
        });
    }

    /**
     * 模拟去点击控制开关按钮
     */
    public void performClickControlBtn() {
        mHandler.post(() -> {
            showWindow("default_float_window_tag");
            //主动点击开始window 按钮 开始任务
            FloatWindow.get().getView().performClick();
        });
    }

    public boolean getTaskStatus() {
        return taskState;
    }

    public void init(Context mainActivity) {
        this.mContext = mainActivity;
        if (FloatWindow.get() == null) {
            Logger.d("float window init");
            View taskControl = LayoutInflater.from(this.mContext).inflate(R.layout.window_task_control, null);

//            taskControl.setOnDragListener((v, event) -> {
//                L.d("control button onDrag...");
////                if (!taskState) {
////                    //任务没开始，隐藏按钮
////                    hideWindow("default_float_window_tag");
////                }
//                return true;
//            });
            taskControl.setOnClickListener(v -> {
                Logger.d("control button onclick...");
                if (taskState) {
                    //任务是执行状态
                    TaskId.e("手动结束了任务");
                }
                initTaskData();
                if (!taskState) {
                    //任务是停止状态，提示有1分钟的操作时间
                    showShortNotifyToast("手动结束了任务");
//                    showShortNotifyToast("手动结束了任务，您有1分钟的操作时间");
                }
            });

            FloatWindow
                    .with(this.mContext)
                    .setView(taskControl)
                    .setWidth(DpUtil.dp2px(136))                               //设置控件宽高
                    .setHeight(DpUtil.dp2px(95))
                    .setX(1)                                   //设置控件初始位置
//                    .setX(DpUtil.getScreenWidth(UcApp.mContext) - DpUtil.dp2px(UcApp.mContext, 102))                                   //设置控件初始位置
                    .setY(Screen.height, 0.9f)
                    .setDesktopShow(false)                        //桌面显示
                    .setViewStateListener(new ViewStateListenerAdapter() {
                        @Override
                        public void onPositionUpdate(int action, int x, int y) {
//                            MotionEvent.ACTION_MOVE
//                            MotionEvent.ACTION_UP
                            if (!taskState) {
                                View hiddenControlPanel = FloatWindow.get("hidden_control_panel").getView();
                                float panelBottomY = hiddenControlPanel.getY() + hiddenControlPanel.getHeight();
                                if (action == MotionEvent.ACTION_MOVE) {
                                    controlBtnLastMoveY = y;
                                    showWindow("hidden_control_panel");
//                                18sp/24sp
//                                white/black
                                    int size = 18;
                                    int color = Color.WHITE;

                                    String text = "拖到此处隐藏";
                                    TextView tv = hiddenControlPanel.findViewById(R.id.tv_message);
                                    if (y < panelBottomY) {
                                        //达到区域
                                        size = 24;
                                        color = Color.BLACK;
                                        text = "松手隐藏";
                                    }
                                    tv.setTextSize(COMPLEX_UNIT_SP, size);
                                    tv.setTextColor(color);
                                    tv.setText(text);
                                } else if (action == MotionEvent.ACTION_UP) {
                                    hideWindow("hidden_control_panel");
                                    if (controlBtnLastMoveY < panelBottomY) {
                                        //达到区域
                                        hideWindow("default_float_window_tag");
                                    }
                                }
                            } else {
                                hideWindow("hidden_control_panel");
                            }
                        }
                    })
                    .build();
        }

        if (FloatWindow.get("short_toast") == null) {

            View toast = LayoutInflater.from(this.mContext).inflate(R.layout.window_task_short_toast, null);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 720, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setRepeatMode(RESTART);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            rotateAnimation.setDuration(2000);
            toast.findViewById(R.id.iv_cycle).startAnimation(rotateAnimation);
            FloatWindow
                    .with(this.mContext)
                    .setView(toast)
                    .setWidth(Screen.width, 1f)                               //设置控件宽高
                    .setHeight(Screen.height, 0.3f)
                    .setX(Screen.width, 0f)                                   //设置控件初始位置
                    .setY(Screen.height, 0.83f)
                    .setDesktopShow(false)                        //桌面显示
                    .notTouchable(true)
                    .setTag("short_toast")
                    .setMoveType(MoveType.inactive)
                    .build();
        }

        if (FloatWindow.get("hidden_control_panel") == null) {
            View toast = LayoutInflater.from(this.mContext).inflate(R.layout.window_hidden_control_panel, null);
            FloatWindow
                    .with(this.mContext)
                    .setView(toast)
                    .setWidth(Screen.width, 1f)                               //设置控件宽高
                    .setHeight(Screen.height, 0.1f)
                    .setX(Screen.width, 0f)                                   //设置控件初始位置
                    .setY(Screen.height, 0f)
                    .setDesktopShow(false)                        //桌面显示
                    .notTouchable(true)
                    .setTag("hidden_control_panel")
                    .setMoveType(MoveType.inactive)
                    .build();
        }

        if (FloatWindow.get("window_toast") == null) {

            View toast = LayoutInflater.from(this.mContext).inflate(R.layout.window_toast, null);
            FloatWindow
                    .with(this.mContext)
                    .setView(toast)
                    .setWidth(Screen.width, 1f)                               //设置控件宽高
                    .setHeight(Screen.height, 0.15f)
                    .setX(Screen.width, 0f)                                   //设置控件初始位置
                    .setY(Screen.height, 0.75f)
                    .setDesktopShow(false)                        //桌面显示
                    .notTouchable(true)
                    .setTag("window_toast")
                    .setMoveType(MoveType.inactive)
                    .build();
        }

        if (FloatWindow.get("toast") == null) {
            FloatWindow
                    .with(this.mContext)
                    .setView(new WindowToast(AppInstance.getInstance().provideContext()))
                    .setWidth(DpUtil.getScreenWidth())                               //设置控件宽高
                    .setHeight(DpUtil.getScreenHeight())
                    .setX(0)                                   //设置控件初始位置
                    .setY(0)
                    .setDesktopShow(true)                        //桌面显示
                    .setTag("toast")
                    .setMoveType(MoveType.inactive)
                    .build();
        }

    }

    public void initTaskData() {
        if (runnable != null) {
            //首先判断任务开始的界面对不对
            if (!taskState) {
                if (AppInstance.showStartControl(taskId)) {
                    switch (taskId) {
                        case TaskId.TASK_CHAT_WINDOW_GROUP:
                            if (!AndroidCurrentActivity.isCurrentActivity(WeUI.UI_CHATTING_UI)) {
                                showToast("请前往【好友聊天界面】，再点开始");
                                return;
                            }
                            break;
                    }
                } else if (!TaskId.get().mStartFromWindow && AndroidCurrentActivity.isAppActivity() && !AndroidCurrentActivity.isMainUi() && !Back2AppHomeService.concludeFilter(AndroidCurrentActivity.getInstance().getCurrentActivity())) {
                    //不是从float window开始的
                    //任务没有开始
                    //必须在主界面
                    //不是主界面 给提示 不执行任务
                    Back2AppHomeService.start(mContext);
                    return;
                }
            }
            if (mTryShowStartControl) {
                //不再是尝试显示开始按钮阶段
                mTryShowStartControl = false;
            }
            taskState = !taskState;
            if (taskState) {
                TaskId.get().mTaskToggle = true;
                runnable.run();
                TaskId.get().mStartFromWindow = false;
                //执行一个当前界面的event
                AccessibilityEventExt obtain = ObtainWindowStateChangeEvent.obtainEvent(AndroidCurrentActivity.getInstance().getCurrentActivity());
                if (obtain != null) {
                    if (mContext != null) {
                        Logger.d("start run task");
                        HandleEventService.startWithEvent(mContext, obtain);
                    }
                }
            } else {
                TaskController.stopTask();
            }
            if (FloatWindow.get() != null) {
                ((ImageView) FloatWindow.get().getView().findViewById(R.id.iv_task_status)).setImageResource(taskState ? R.mipmap.icon_control_stop : R.mipmap.icon_control_start);
            }
        } else {
            TaskId.stop();
        }
    }

    public void showToast(String message) {
        if (FloatWindow.get("short_toast") != null) {
            mHandler.post(() -> {
                String name;
                String toast;
                if (!TextUtils.isEmpty(message)) {
                    name = "";
                    toast = message;
                } else {
                    name = taskName;
                    toast = ">> 执行中";
                }
                ((TextView) FloatWindow.get("short_toast").getView().findViewById(R.id.tv_task_name)).setText(name);
                ((TextView) FloatWindow.get("short_toast").getView().findViewById(R.id.tv_message)).setText(toast);
                FloatWindow.get("short_toast").show();
            });
        }
    }

    private final LinkedList<String> logList = new LinkedList<>();

    public void updateToastLog(String log) {
        if (FloatWindow.get("short_toast") != null) {
            mHandler.post(() -> {
                if (logList.size() == 5) {
                    logList.removeFirst();
                }
                ((TextView) FloatWindow.get("short_toast").getView().findViewById(R.id.tv_log)).setText(String.join("\n", logList));
                ((TextView) FloatWindow.get("short_toast").getView().findViewById(R.id.tv_log_current)).setText(log);
                logList.add(log);
            });
        }
    }

    public void showShortNotifyToast(String message) {
        if (FloatWindow.get("window_toast") != null) {
            mShortNotifyHandler.post(() -> {
                mShortNotifyHandler.removeCallbacksAndMessages(null);
                ((TextView) FloatWindow.get("window_toast").getView().findViewById(R.id.tv_message)).setText(message);
                FloatWindow.get("window_toast").show();

                mShortNotifyHandler.postDelayed(() -> {
                    if (FloatWindow.get("window_toast") != null) {
                        FloatWindow.get("window_toast").hide();
                    }
                }, 3000);
            });
        } else {
            Logger.e("window_toast is null");
        }
    }

    public void startFromWindow() {
        TaskId.get().mStartFromWindow = true;
    }

    public void showOnAppActivity(String activityName) {
        if (mTryShowStartControl) {
            //不再是尝试显示开始按钮阶段
            mTryShowStartControl = false;
        }
    }

    public void showWindow(String tag) {
        if (FloatWindow.get(tag) != null && !FloatWindow.get(tag).isShowing()) {
            FloatWindow.get(tag).show();
            if ("default_float_window_tag".equals(tag)) {
                FloatWindow.get(tag).updateY(Screen.height, 0.7f);
            }
        }
    }

    public void hideWindow(String tag) {
        if (FloatWindow.get(tag) != null && FloatWindow.get(tag).isShowing()) {
            FloatWindow.get(tag).hide();
        }
    }

    public void hideToastAndControlWindow() {
        mHandler.post(() -> {
            hideWindow("default_float_window_tag");
            hideWindow("short_toast");
        });
    }


    public void showToastAndControlWindow() {
        mHandler.post(() -> {
            showWindow("default_float_window_tag");
            showWindow("short_toast");
        });
    }


    public void terminate() {
        try {
            FloatWindow.detechAllWindow();
            Logger.i("float window terminate");
        } catch (Exception e) {
            Logger.e(Log.getStackTraceString(e));
        }
    }
}
