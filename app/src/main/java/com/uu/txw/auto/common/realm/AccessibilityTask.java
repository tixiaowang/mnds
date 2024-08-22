package com.uu.txw.auto.common.realm;

import com.uu.txw.auto.common.STime;
import com.uu.txw.auto.task.data.CusScriptTask;

import java.util.Arrays;
import java.util.List;

public class AccessibilityTask {

    public static final String F_N_TARGET_NAME = "targetName";
    public static final String F_N_CONTENT = "content";
    public static final String F_N_TARGET_NAME_LIST = "targetNameList";
    public static final String F_N_EXT = "ext";

    private String id; //任务Id，需要服务器下发唯一
    private String type; // 任务类型
    private long createTime;  // 服务器任务创建时间

    private String targetName; // 目标名：
    private String content; // 内容：消息，备注,修改后的群名，群昵称
    private List<String> targetNameList; // 目标列表
    private String ext; //存储额外信息，用于拓展
    private CusScriptTask cusScriptTask; //存储额外信息，用于拓展
    private long timer; //定时
    private int delete; //0未删除，1删除

    public CusScriptTask getCusScriptTask() {
        return cusScriptTask;
    }

    private int fileNeedDownload; //文件是否需要下载1需要 0不需要

    private String localId;
    private int priority;  //优先级 -100 - 100
    private long localTime;  //接收到任务的时间
    private int taskResult;//任务结束的结果 1成功 0失败（由于控件找不到或者配置未打开或者超时任务结束等异常）
    private String taskLog;//任务执行过程中的日志
    private long timeCost;//任务执行耗时
    private long taskStartTime;//任务开始执行时间
    private long taskEndTime;//任务结束执行时间
    private int processStatus;//执行进度 1执行完成 0未执行
    private long processCompleteTime;//任务完成时间（可能因为配置异常直接完成）

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getDelete() {
        return delete;
    }

    public long getLocalTime() {
        return localTime;
    }

    public String getTargetName() {
        return targetName;
    }

    public List<String> getTargetNameList() {
        return targetNameList;
    }

    public String getContent() {
        return content;
    }

    public String getLocalId() {
        return localId;
    }

    public AccessibilityTask localId(String localId) {
        this.localId = localId;
        return this;
    }

    public AccessibilityTask processComplete() {
        this.processStatus = 1;
        this.processCompleteTime = STime.currenttimemillis();
        return this;
    }

    public AccessibilityTask processReset() {
        this.processStatus = 0;
        this.processCompleteTime = 0;
        return this;
    }

    public void resetTaskStatus() {
        taskResult(false)
                .taskLog("")
                .startTime(0)
                .endTime(0)
                .timeCost(0)
                .processReset();
    }

    public boolean isProcessComplete() {
        return this.processStatus == 1;
    }

    public AccessibilityTask localTime(long localTime) {
        this.localTime = localTime;
        return this;
    }

    public AccessibilityTask timeCost(long timeCost) {
        this.timeCost = timeCost;
        return this;
    }

    public AccessibilityTask taskResult(boolean taskResult) {
        this.taskResult = taskResult ? 1 : 0;
        return this;
    }

    public AccessibilityTask type(String type) {
        this.type = type;
        return this;
    }

    public AccessibilityTask id(String id) {
        this.id = id;
        return this;
    }

    public AccessibilityTask startTime(long startTime) {
        this.taskStartTime = startTime;
        return this;
    }

    public AccessibilityTask endTime(long endTime) {
        this.taskEndTime = endTime;
        return this;
    }

    public AccessibilityTask taskLog(String taskLog) {
        this.taskLog = taskLog;
        return this;
    }

    public AccessibilityTask content(String content) {
        this.content = content;
        return this;
    }

    public AccessibilityTask targetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public AccessibilityTask targetNameList(String... items) {
        this.targetNameList = Arrays.asList(items);
        return this;
    }

    public AccessibilityTask ext(String ext) {
        this.ext = ext;
        return this;
    }

    public AccessibilityTask fileNeedDownload() {
        this.fileNeedDownload = 1;
        return this;
    }

    public AccessibilityTask fileDownloadComplete() {
        this.fileNeedDownload = 0;
        return this;
    }

    public AccessibilityTask priority(int priority) {
        this.priority = priority;
        return this;
    }

    public long getProcessCompleteTime() {
        return processCompleteTime;
    }

    public int getTaskResult() {
        return taskResult;
    }

    public String getTaskLog() {
        return taskLog;
    }

    public long getTimer() {
        return timer;
    }

    public long getTimeCost() {
        return timeCost;
    }

    public String getExt() {
        return ext;
    }

    public long getTaskStartTime() {
        return taskStartTime;
    }

    public long getTaskEndTime() {
        return taskEndTime;
    }

    @Override
    public String toString() {
        return "AccessibilityTask{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", createTime=" + createTime +
                ", targetName='" + targetName + '\'' +
                ", content='" + content + '\'' +
                ", targetNameList=" + targetNameList +
                ", ext='" + ext + '\'' +
                ", fileNeedDownload=" + fileNeedDownload +
                ", localId='" + localId + '\'' +
                ", priority=" + priority +
                ", localTime=" + localTime +
                '}';
    }

    public AccessibilityTask cusScriptTask(CusScriptTask cusScriptTask) {
        this.cusScriptTask = cusScriptTask;
        return this;
    }
}
