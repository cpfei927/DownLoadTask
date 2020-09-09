package com.xueersi.downloadtask.downloadpool;

import android.app.Application;
import android.content.Context;

/**
 * Created by limin on 2017/3/2.
 * <p>
 * 上下文管理类，主要用户获取工程的上下文
 * 在Application中初始化后可以在任意处单例调用
 */
public class ContextManager {

    private Application context;

    /**
     * 当持有activity的context时会有内存溢出的问题，
     * 持有Application应该没有问题，Application生命周期贯穿整个工程
     */
    private static ContextManager sInstance;

    private ContextManager(Application context) {
        this.context = context;
    }

    /**
     * 初始化Context对象，在Application初始化时调用
     *
     * @param context
     */
    public static void initContext(Application context) {
        sInstance = new ContextManager(context);
    }

    /**
     * 获取Application的上下文对象
     *
     * @return
     */
    public static synchronized Context getContext() {
        return sInstance.context;
    }

    public static synchronized Application getApplication() {
        return sInstance.context;
    }
}
