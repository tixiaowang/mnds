package com.uu.txw.auto.task.data;

import java.util.Arrays;

public class ScriptStatementVar extends ScriptStatement {
    public ScriptStatementVar(String varName, Object varVal) {
        this.name = "新建变量";
        this.args_count = 2;
        this.args = Arrays.asList(varName, varVal);
    }
}
