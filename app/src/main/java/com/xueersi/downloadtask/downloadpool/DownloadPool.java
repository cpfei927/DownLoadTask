package com.xueersi.downloadtask.downloadpool;

import java.util.HashMap;
import java.util.Iterator;

/**
 * 下载池-所有的下载任务，都存在这个池中
 *
 * @author shixiaoqiang
 */
public class DownloadPool {
    /**
     * 正在下载的队列
     */
    private static HashMap<String, DownLoader> sDownloaders = new HashMap<String, DownLoader>();
    /**
     *
     */
    private static Object sLockObject = new Object();

    /**
     * 任务是否已经在下载池中，在的话，返回下载器，没有的话则创建，增加到下载池
     */
    public static DownLoader getDownLoader(DownLoadInfo info) {
        synchronized (sLockObject) {
            if (sDownloaders.containsKey(info.getUrl())) {
                return sDownloaders.get(info.getUrl());
            }
            DownLoader loader = new DownLoader(info);
            sDownloaders.put(info.getUrl(), loader);
            return loader;
        }
    }

    /**
     * 从下载池中删除任务下载器
     */
    public static void removeDownloader(String key) {
        synchronized (sLockObject) {
            if (sDownloaders.containsKey(key)) {
                sDownloaders.remove(key);
            }
        }
    }


    /**
     * 从下载池中检查
     */
    public static boolean containsDownloader(String key) {
        synchronized (sLockObject) {
            if (sDownloaders.containsKey(key)) {
                return true;
            }
            return false;
        }
    }


    /**
     * 将任务下载器添加到下载池
     */
    public static void addDownloader(String key, DownLoader downLoader) {
        synchronized (sLockObject) {
            sDownloaders.put(key, downLoader);
        }
    }

    /**
     * 暂停下载
     */
    public static void pauseDownLoad() {
        synchronized (sLockObject) {
            Iterator<String> iterator = sDownloaders.keySet().iterator();
            while (iterator.hasNext()) {
                DownLoader downLoader = sDownloaders.get(iterator.next());
                downLoader.stop(true);
            }
        }
    }
}