package com.yhao.floatwindow;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class MoveType {
    static final int fixed = 0;
    public static final int inactive = 1;
    public static final int active = 2;
    public static final int slide = 3;
    public static final int back = 4;

    @Retention(RetentionPolicy.SOURCE)
    @interface MOVE_TYPE {
    }
}
