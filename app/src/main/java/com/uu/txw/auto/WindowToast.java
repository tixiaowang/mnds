package com.uu.txw.auto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;



public class WindowToast extends FrameLayout {


    private View mContainer;

    public WindowToast( Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.window_task_toast, this);
        mContainer = findViewById(R.id.rl_container);

        findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FloatWindowUtil.getInstance().hideWindow("toast");
            }
        });

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setVisibility(VISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) {
            super.setVisibility(visibility);
        }
        Animation animation = AnimationUtils.loadAnimation(getContext(), visibility == VISIBLE ? R.anim.dialog_enter : R.anim.dialog_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (visibility == INVISIBLE) {
                    WindowToast.super.setVisibility(visibility);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mContainer.startAnimation(animation);
    }
}
