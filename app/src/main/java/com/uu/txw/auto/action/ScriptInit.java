package com.uu.txw.auto.action;

public abstract class ScriptInit {
    private static boolean hasInit = false;

    public static void reset() {
        hasInit = false;
    }

    public static boolean hasInit() {
        return hasInit;
    }

    public void init() {
        if (!hasInit) {
            hasInit = true;
            run();
        }
    }

    public abstract void run();
}
