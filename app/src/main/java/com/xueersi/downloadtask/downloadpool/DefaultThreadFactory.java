package com.xueersi.downloadtask.downloadpool;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DefaultThreadFactory implements ThreadFactory {
    private static final String TAG = DefaultThreadFactory.class.getSimpleName();
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private static final String THREAD_SUFFIX = "IPL-";
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;

    public DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = THREAD_SUFFIX + poolNumber.getAndIncrement() + "-T";
    }

    public DefaultThreadFactory(String poolTag) {
        String tag;
        if (TextUtils.isEmpty(poolTag)) {
            tag = THREAD_SUFFIX;
        } else if (poolTag.length() > 3) {
            tag = poolTag.substring(0, 3);
        } else {
            tag = poolTag;
        }
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = tag + "_" + poolNumber.getAndIncrement() + "-T";
    }

    public Thread newThread(@NonNull Runnable runnable) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
//        ARouter.logger.info(Consts.TAG, "Thread production, name is [" + threadName + "]");
        Thread thread = new Thread(group, runnable, threadName, 0);
        if (thread.isDaemon()) {   //设为非后台线程
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) { //优先级为normal
            thread.setPriority(Thread.NORM_PRIORITY);
        }

        // 捕获多线程处理中的异常
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.w(TAG, "Running task appeared exception! Thread [" + thread.getName() + "], because [" + ex.getMessage() + "]");
            }
        });
        return thread;
    }
}