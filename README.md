# 么你大师
##  简介
本项目为android模拟点击框架，java原生开发，使用无障碍服务，以事件驱动为基础，脚本绑定页面为核心，支持二次开发，api封装友好，使用简单。
##  快速开始

**1、依赖**

```
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    implementation 'com.github.tixiaowang:mnds:1.0'
}
```

**2、初始化**

Application或者Activity中初始化一次

    AppInstance.getInstance().regist(this, null);

**3、新建一个脚本类**

    public class ActionScrollForwardInterval extends AccessibilityHelper implements Action {
        @Override
        public void run(AccessibilityEventExt event) {
    
            while (isRunning()) {
                sleepLLL();
                performGestureScrollForward();
                sleepLLL();
                sleepLLL();
            }
        }
    }

**4、执行脚本**

点击按钮后执行

    view.setOnClickListener(v ->
                TaskHub.start(this, CusScriptTask.TYPE_SINGLE_THREAD,
                        "连续上滑",
                        "",
                        "",
                        "",
                        null,
                        Arrays.asList(ActionScrollForwardInterval.class)
                )
        );

首次执行会提示打开**悬浮窗**和**无障碍**权限，请依次打开。

打开权限后，点击执行脚本，会出现**开始**悬浮按钮，切换页面到想要连续上滑的页面，点击**开始**。

## 详细文档

**术语**

- 任务Task，具体的功能，比如转群主、群发消息
- 脚本（Action），一个任务由很多脚本绑定
- 事件，页面切换、按钮点击，无障碍服务会回调对应的事件
- 页面，activity名，或者是自定义的名字，获取当前页面的方法通过上述事件的事件来源或者手动查找当前布局的特征，比如主页的特征是底部有固定的菜单

**一个脚本功能（任务）类型有三类**

```
public static final String TYPE_SINGLE_THREAD = "串行立即执行";
public static final String TYPE_SINGLE_THREAD_MAIN_UI = "串行执行[指定应用主页面]";
public static final String TYPE_BIND_UI = "并行执行[匹配页面]";
```

**串行立即执行**

该类型将在一条线程中依次执行所有的脚本（Action），执行完毕后，任务结束。

并且该类型任务启动后，需要手动切换到需要执行脚本的页面，手动点击开始悬浮按钮，开始执行。

**串行执行[指定应用主页面]**

该类型将在一条线程中依次执行所有的脚本（Action），执行完毕后，任务结束。

该类型任务启动后，会自动前往第三方应用的主页面，自动开始执行。

该类型启动任务，需要指定第三方应用的包名、启动页、主页

**并行执行[匹配页面]**

该类型将在一条线程中按照事件的类型、脚本绑定的页面从脚本列表中筛选出对应的脚本，然后执行一次，页面切换会触发新的事件、或者由守护服务触发，如此循环 筛选脚本来执行，根据自定义的条件，自定义任务结束的时机。

该类型任务启动后，会自动前往第三方应用的主页面，自动开始执行。

该类型启动任务，需要指定第三方应用的包名、启动页、主页。

并且该类型任务绑定的所有脚本必须绑定对应的事件类型和页面名称。具体可参考[DEMO项目](https://github.com/tixiaowang/mnds-demo)。

```
@EVENT_TYPE
@EVENT_CLASS("com.xingin.xhs.index.v2.IndexActivityV2")
public class Xhs8380ActionClickMessageMenuWatch extends AccessibilityHelper implements Action
```

**待补充。。。**

## 其他项目

- [android布局dump前端](https://github.com/tixiaowang/AndroidLD-Fe)
- [android布局dump后端](https://github.com/tixiaowang/AndroidLD-Shell)
- [功能DEMO](https://github.com/tixiaowang/mnds-demo)



## 交流 

Q群 1005865891