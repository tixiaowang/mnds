package com.uu.txw.auto.task.data;

import java.util.List;

public class CusScriptTask {
    public static final String TYPE_SINGLE_THREAD = "串行立即执行";
    public static final String TYPE_SINGLE_THREAD_MAIN_UI = "串行执行[指定应用主页面]";
    public static final String TYPE_BIND_UI = "并行执行[匹配页面]";
    public String type;
    public String script_id;
    public String task_name;
    public String task_short_name;
    public String main_package_name;
    public String main_package_launcher_ui;
    public String main_package_main_ui;
    public String script_url;
    public String script_path;
    public String script_md5;
    public String sequence;
    public List<ScriptStatement> initStatementList;

    public List<Class<?>> script_classes;
    public List<String> script_clazz;



    public boolean immediatelySingleThreadTask() {
        return TYPE_SINGLE_THREAD.equals(this.type);
    }
    public boolean singleThreadTask() {
        return !bindUiTask();
    }
    public boolean bindUiTask() {
        return TYPE_BIND_UI.equals(this.type);
    }


    public CusScriptTask type(String type) {
        this.type = type;
        return this;
    }

    public CusScriptTask script_id(String script_id) {
        this.script_id = script_id;
        return this;
    }

    public CusScriptTask task_name(String task_name) {
        this.task_name = task_name;
        return this;
    }

    public CusScriptTask task_short_name(String task_short_name) {
        this.task_short_name = task_short_name;
        return this;
    }

    public CusScriptTask main_package_name(String main_package_name) {
        this.main_package_name = main_package_name;
        return this;
    }

    public CusScriptTask main_package_launcher_ui(String main_package_launcher_ui) {
        this.main_package_launcher_ui = main_package_launcher_ui;
        return this;
    }

    public CusScriptTask main_package_main_ui(String main_package_main_ui) {
        this.main_package_main_ui = main_package_main_ui;
        return this;
    }

    public CusScriptTask script_url(String script_url) {
        this.script_url = script_url;
        return this;
    }

    public CusScriptTask script_path(String script_path) {
        this.script_path = script_path;
        return this;
    }

    public CusScriptTask script_md5(String script_md5) {
        this.script_md5 = script_md5;
        return this;
    }

    public CusScriptTask sequence(String sequence) {
        this.sequence = sequence;
        return this;
    }

    public CusScriptTask script_classes(List<Class<?>> script_classes) {
        this.script_classes = script_classes;
        return this;
    }


    public CusScriptTask script_clazz(List<String> script_clazz) {
        this.script_clazz = script_clazz;
        return this;
    }


    public CusScriptTask initStatementList(List<ScriptStatement> initStatementList) {
        this.initStatementList = initStatementList;
        return this;
    }
}
