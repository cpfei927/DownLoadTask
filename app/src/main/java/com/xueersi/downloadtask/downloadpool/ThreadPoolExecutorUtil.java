package com.xueersi.downloadtask.downloadpool;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池处理类
 * 将线程池分为轻量级线程 和 长时间线程。
 * 轻量级将以创建线程为主，降低加入任务列表的概率;
 * 长时间线程池是Cache线程池，主要运行较长时间的任务，比如下载。或者临时的低频任务;
 *
 */
public class ThreadPoolExecutorUtil {
    private static final String TAG = ThreadPoolExecutorUtil.class.getSimpleName();

    /**
     * 轻量级线程池，主要处理短时间（10s）内的请求。比如http请求，执行某个简单人物;
     */
    private static class LightPoolExecutor extends ThreadPoolExecutor {
        //    Thread args
        private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        private static final int INIT_THREAD_COUNT = Math.min((CPU_COUNT * 2) + 1, 20);
        private static final int MAX_THREAD_COUNT = 128;
        private static final long SURPLUS_THREAD_LIFE = 30L;

        private volatile static LightPoolExecutor instance;

        public static LightPoolExecutor getInstance() {
            if (null == instance) {
                synchronized (LightPoolExecutor.class) {
                    if (null == instance) {
                        instance = new LightPoolExecutor(INIT_THREAD_COUNT,  //
                                MAX_THREAD_COUNT, //
                                SURPLUS_THREAD_LIFE, //
                                TimeUnit.SECONDS,  //
                                new SynchronousQueue<Runnable>(true), //
                                new DefaultThreadFactory(), defaultHandler);
                    }
                }
            }
            return instance;
        }

        /**
         * The default rejected execution handler.
         */
        private static final RejectedExecutionHandler defaultHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.e("TAG",  " too many task, task will exec imme.!");
                if (!executor.isShutdown()) { //直接run task，block other task.
                    r.run();
                }
            }
        };

        private LightPoolExecutor(int corePoolSize, int maximumPoolSize, //
                                  long keepAliveTime, TimeUnit unit, //
                                  BlockingQueue<Runnable> workQueue, //
                                  ThreadFactory threadFactory,
                                  RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        /*
         *  线程执行结束，顺便看一下有么有什么乱七八糟的异常
         *
         * @param r the runnable that has completed
         * @param t the exception that caused termination, or null if
         */
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t == null && r instanceof Future<?>) {
                try {
                    ((Future<?>) r).get();
                } catch (CancellationException ce) {
                    t = ce;
                } catch (ExecutionException ee) {
                    t = ee.getCause();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }
            }
            if (t != null) {
                Log.i(TAG, "Running task appeared exception! Thread [" + Thread.currentThread().getName() + "], because [" + t.getMessage() + "]\n" + formatStackTrace(t.getStackTrace()));
            }
        }

        /*
         * Print thread stack
         */
        private String formatStackTrace(StackTraceElement[] stackTrace) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                sb.append("    at ").append(element.toString());
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * 全局线程池
     */
    private static ThreadPoolExecutor sThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new DefaultThreadFactory());

    /**
     * 轻量级线程
     * @return
     */
    public static ThreadPoolExecutor getLightThreadPoolExecutor() {
        return LightPoolExecutor.getInstance();
    }

    /**
     * 获取ThreadExecutor， 默认为轻量级的线程池，为简单人物(10s)提供服务.
     * @return
     */
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return LightPoolExecutor.getInstance();
    }

    /**
     * 获取ThreadExecutor， 默认为轻量级的线程池，为(大于10s) 的任务提供服务.
     * @return
     */
    public static ThreadPoolExecutor getWeightThreadPoolExecutor() {
        return getCacheThreadPoolExecutor();
    }

    /**
     * 获取ThreadExecutor， Cache 线程池，核心线程为0，自动缩容
     * @return
     */
    public static ThreadPoolExecutor getCacheThreadPoolExecutor() {
        return sThreadPool;
    }
}
