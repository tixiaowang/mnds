package com.uu.txw.auto.action;


public class ActionEntity {
    private int eventType;
    private String className;
    private String[] classNames;
    private Action action;

    public ActionEntity className(String className) {
        this.className = className;
        return this;
    }

    public int getEventType() {
        return eventType;
    }

    public ActionEntity classNames(String[] classNames) {
        this.classNames = classNames;
        return this;
    }

    public ActionEntity action(Action action) {
        this.action = action;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public String[] getClassNames() {
        return classNames;
    }

    public Action getAction() {
        return action;
    }

    public ActionEntity eventType(int eventType) {
        this.eventType = eventType;
        return this;
    }
}
