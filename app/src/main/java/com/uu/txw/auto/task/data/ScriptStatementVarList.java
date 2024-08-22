package com.uu.txw.auto.task.data;

import java.util.Arrays;

public class ScriptStatementVarList extends ScriptStatement {
    public ScriptStatementVarList(String varName, String varVal) {
        this.name = "新建变量-列表";
        this.args_count = 2;
        this.args = Arrays.asList(varName, varVal);
    }
}
