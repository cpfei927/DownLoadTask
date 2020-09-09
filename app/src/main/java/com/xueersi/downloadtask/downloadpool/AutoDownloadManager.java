package com.xueersi.downloadtask.downloadpool;

import android.text.TextUtils;


import java.util.HashMap;
import java.util.Iterator;

/**
 * 自动下载管理器
 *
 * @author shixiaoqiang on 2015/5/17.
 */

public class AutoDownloadManager {
    /**
     * 当前正在下载的文件url
     */
    private static String sDownUrl = null;
    /**
     * 自动下载池
     */
    private static HashMap<String, DownLoadInfo> sAutoDownloaderPool =
            new HashMap<String, DownLoadInfo>();
    /**
     * 同步锁
     */
    private static Object sLockObject = new Object();

    /**
     * 启动后台自动下载-Wifi连接时调用
     */
    public static void startAutoDownload() {

//        //非Wi-Fi 不能自动下载
//        if (!NetWorkHelper.isWifiDataEnable(RunningEnvironment.sAppContext)) {
//            sDownUrl = null;
//            return;
//        }
        synchronized (sLockObject) {
            Iterator<String> iterator = sAutoDownloaderPool.keySet().iterator();
            if (!iterator.hasNext()) {
                return;
            }
            DownLoadInfo info = sAutoDownloaderPool.get(iterator.next());
            if (info == null || TextUtils.isEmpty(info.getUrl())) {
                return;
            }
            // 如果有已经在下载的任务则不下载
            if (!TextUtils.isEmpty(sDownUrl)) {
                return;
            }

            if (DownLoadInfo.DownloadType.FILE.equals(info.getDownloadType())) {
                // 下载文件
                DownloadListener downloadListener = new DownloadListener() {

                    @Override
                    public void onStart(String url) {
                        synchronized (sLockObject) {
                            sDownUrl = url;
                        }
                    }

                    /**
                     * 下载完成
                     */
                    @Override
                    public void onFinish() {
                        //从下载队列中移除
                        removeDownloaderFromPool(sDownUrl);
                        synchronized (sLockObject) {
                            sDownUrl = null;
                        }
                        startAutoDownload();
                    }

                    /**
                     *
                     */
                    @Override
                    public void onSuccess(String folderPath, String fileName) {

                    }

                    /**
                     *
                     */
                    @Override
                    public void onFail(int errorCode) {
                    }

                    @Override
                    public void onPause() {

                    }

                    @Override
                    public void onProgressChange(long currentLength,
                                                 long fileLength) {

                    }
                };
                DownloadPool.getDownLoader(info).start(downloadListener);
            } else if (DownLoadInfo.DownloadType.IMG.equals(info.getDownloadType())) {
                // 下载图片
                // @Fixme
                //sImageManager.loadImage(info.getUrl(), null);
                sDownUrl = null;
                removeDownloaderFromPool(info.getUrl());
                startAutoDownload();
            }
        }
    }

    /**
     * 添加到自动下载池，并启动下载
     */
    public static void addToAutoDownloadPool(final DownLoadInfo info) {
        if (info == null || TextUtils.isEmpty(info.getUrl())) {
            return;
        }
        // 下载的是文件
        if (DownLoadInfo.DownloadType.FILE.equals(info.getDownloadType())) {
            // 文件名或文件夹为空，返回
            if (TextUtils.isEmpty(info.getFolder())
                    || TextUtils.isEmpty(info.getFileName())) {
                return;
            }
            // 将下载器添加到下载池
            DownloadPool.addDownloader(info.getUrl(), new DownLoader(info));
        }
        addDownloaderToPool(info.getUrl(), info);
        startAutoDownload();
    }

    /**
     * 从自动下载池中删除任务
     */
    private static void removeDownloaderFromPool(String key) {
        synchronized (sLockObject) {
            if (sAutoDownloaderPool.containsKey(key)) {
                sAutoDownloaderPool.remove(key);
            }
        }
    }

    /**
     * 添加任务到自动下载池
     */
    private static void addDownloaderToPool(String key,
                                            DownLoadInfo downLoadInfo) {
        synchronized (sLockObject) {
            if (!sAutoDownloaderPool.containsKey(key)) {
                sAutoDownloaderPool.put(key, downLoadInfo);
            }
        }
    }
}
