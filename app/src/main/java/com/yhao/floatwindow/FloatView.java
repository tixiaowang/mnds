package com.yhao.floatwindow;

import android.view.View;

/**
 * Created by yhao on 17-11-14.
 * https://github.com/yhaolpz
 */

abstract class FloatView {

    abstract void setSize(int width, int height);

    abstract void recoverSize(int width, int height);

    abstract int getWidth();

    abstract void updateSizeToFullScreen(int width, int height, int x, int y);

    abstract void setView(View view);

    abstract void setGravity(int gravity, int xOffset, int yOffset);

    abstract void init();

    abstract void dismiss();

    abstract void setNotTouchble();

    void updateXY(int x, int y) {
    }

    void updateX(int x) {
    }

    void updateY(int y) {
    }

    int getX() {
        return 0;
    }

    int getY() {
        return 0;
    }
}
