package com.yhao.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by yhao on 17-11-14.
 * https://github.com/yhaolpz
 */

class FloatPhone extends FloatView {

    private final Context mContext;

    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private View mView;
    private int mX, mY;
    private int mXTmp, mYTmp;
    private boolean isRemove = false;
    private PermissionListener mPermissionListener;

    FloatPhone(Context applicationContext, PermissionListener permissionListener ,boolean needFocus) {
        mContext = applicationContext;
        mPermissionListener = permissionListener;
        mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        if (!needFocus) {
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        mLayoutParams.windowAnimations = 0;
        try {
            int currentFlags = (Integer) mLayoutParams.getClass().getField("privateFlags").get(mLayoutParams);
            mLayoutParams.getClass().getField("privateFlags").set(mLayoutParams, currentFlags|0x00000040);
        } catch (Exception e) {

        }
    }

    @Override
    public void setSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
    }

    @Override
    void recoverSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        mLayoutParams.x  = mX  = mXTmp;
        mLayoutParams.y  = mY  = mYTmp;
        mWindowManager.updateViewLayout(mView, mLayoutParams);

    }

    @Override
    int getWidth() {
        return mLayoutParams.width;
    }

    @Override
    void updateSizeToFullScreen(int width, int height ,int x ,int y) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        mXTmp = x;
        mYTmp = y;
        mLayoutParams.x  = mX = 0;
        mLayoutParams.y  = mY = 0;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    public void setView(View view) {
        mView = view;
    }

    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mLayoutParams.gravity = gravity;
        mLayoutParams.x = mX = xOffset;
        mLayoutParams.y = mY = yOffset;
    }


    @Override
    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            req();
        } else if (Miui.rom()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                req();
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                Miui.req(mContext, new PermissionListener() {
                    @Override
                    public void onSuccess() {
                        mWindowManager.addView(mView, mLayoutParams);
                        if (mPermissionListener != null) {
                            mPermissionListener.onSuccess();
                        }
                    }

                    @Override
                    public void onFail() {
                        if (mPermissionListener != null) {
                            mPermissionListener.onFail();
                        }
                    }
                });
            }
        } else {
            try {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                mWindowManager.addView(mView, mLayoutParams);
            } catch (Exception e) {
                mWindowManager.removeView(mView);
                LogUtil.e("TYPE_TOAST 失败");
                req();
            }
        }
    }

    private void req() {
//        if (!SP.getBoolean("dev_mode", false) && AccessibilityHelper.isServiceRunning(AccessibilityHelper.mService)) {
//            mLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
//
//            //直接显示，不需要请求权限
//            mView.setTag(mWindowManager);
//            mWindowManager.addView(mView, mLayoutParams);
//            if (mPermissionListener != null) {
//                mPermissionListener.onSuccess();
//            }
//            return;
//        } else
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        FloatActivity.request(mContext, new PermissionListener() {
            @Override
            public void onSuccess() {
                mWindowManager.addView(mView, mLayoutParams);
                if (mPermissionListener != null) {
                    mPermissionListener.onSuccess();
                }
            }

            @Override
            public void onFail() {
                if (mPermissionListener != null) {
                    mPermissionListener.onFail();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        isRemove = true;
        mWindowManager.removeView(mView);
    }

    @Override
    void setNotTouchble() {
        mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    @Override
    public void updateXY(int x, int y) {
        if (isRemove) return;
        mLayoutParams.x = mX = x;
        mLayoutParams.y = mY = y;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    void updateX(int x) {
        if (isRemove) return;
        mLayoutParams.x = mX = x;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    void updateY(int y) {
        if (isRemove) return;
        mLayoutParams.y = mY = y;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    @Override
    int getX() {
        return mX;
    }

    @Override
    int getY() {
        return mY;
    }


}
