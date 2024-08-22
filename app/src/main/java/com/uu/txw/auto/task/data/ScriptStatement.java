package com.uu.txw.auto.task.data;


import com.uu.txw.auto.UcEntity;

import java.util.List;

public class ScriptStatement implements UcEntity {
    public String name;
    public List<Object> args;
    public int args_count;


    public ScriptStatement name(String name) {
        this.name = name;
        return this;
    }

    public ScriptStatement args(List<Object> args) {
        this.args = args;
        return this;
    }

    public ScriptStatement args_count(int args_count) {
        this.args_count = args_count;
        return this;
    }
}
