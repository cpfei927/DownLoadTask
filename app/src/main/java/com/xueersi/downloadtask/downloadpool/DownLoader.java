package com.xueersi.downloadtask.downloadpool;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件下载器
 *
 * @author shixiaoqiang on 2015/5/17.
 */

public class DownLoader {

    public static final String TAG = DownLoader.class.getSimpleName();
    /**
     * 下载线程数目
     */
    private int THREAD_COUNT = 1;
    /**
     * 下载需要信息
     */
    private DownLoadInfo mDownLoadInfo;
    /**
     * 下载使用的线程数目
     */
    private int mThreadCount = THREAD_COUNT;
    /**
     * 当前处于执行状态的线程数目
     */
    private int mRunningThreadCount = THREAD_COUNT;
    /**
     * 待下载文件所在文件夹
     */
    private File mDownloadFolder;
    /**
     * 待下载的文件
     */
    private File mDownloadFile;
    /**
     * MD5值保存文件
     */
    private File mMd5File;
    /**
     * 当前已下载的长度
     */
    private long mDownloadedLength = 0;
    /**
     * 文件总长度按字节算
     */
    private long mFileLength;
    /**
     * 下载状态
     */
    private int mDownloadStatus = DownloadStatus.READY;
    /**
     * 是否停止线程
     */
    private boolean mStopThread = false;
    /**
     * 结果通知 当下载失败或者暂停时，是否需要发出通知
     */
    private volatile boolean mNeedResultNofity = true;
    /**
     * 下载结果
     */
    private int mDownloadResult = DownloadResult.UNKNOWN;
    /**
     * 下载错误消息
     */
    private String mDownloadResultErrorMessage;
    /**
     * 线程同步锁
     */
    private final Object mLockObject = new Object();
    /**
     * 下载状态监听器
     */
    private List<DownloadListener> mDownloadListeners = new ArrayList<DownloadListener>();
    /**
     * 下载线程队列
     */
    private List<DownloadThread> mDownloadThreads = new ArrayList<DownloadThread>();
    /**
     * 下载链接
     */
    private List<HttpURLConnection> mConnections = new ArrayList<HttpURLConnection>();

    /**
     * 用来保存已下载长度 的文件
     */
    private List<File> mDownloadLengthInfoFileList = new ArrayList<File>();

    /**
     * 是否支持断点续传
     */
    private boolean mSupportBreakpointDownload = true;


    /**
     * 更新Handler
     */
    private DownLoadHandler mDownLoadHandler;
    /**
     * 临时文件夹（进度+MD5）
     */
    private File progressInfoFolder;

    /*
     * 私有默认构造函数
     */
    @SuppressWarnings("unused")
    private DownLoader() {
    }

    /**
     * 构造函数
     *
     * @param info 下载文件信息
     */
    public DownLoader(DownLoadInfo info) {
        this(null, info);
    }


    /**
     * 构造函数
     *
     * @param info 下载文件信息
     */
    public DownLoader(Context context, DownLoadInfo info) {
        if (info == null || TextUtils.isEmpty(info.getFileName())
                || TextUtils.isEmpty(info.getFolder())) {
            return;
        }
        mDownLoadInfo = info;
        mDownloadFolder = new File(mDownLoadInfo.getFolder());
        progressInfoFolder = new File(mDownloadFolder, mDownLoadInfo.getFileName() + "_progressInfoFolder");
        if (!progressInfoFolder.exists()) {
            progressInfoFolder.mkdir();
        }
        mMd5File = new File(progressInfoFolder, "." + mDownLoadInfo.getFileName() + ".md5");
        mDownloadFile = new File(mDownloadFolder, mDownLoadInfo.getFileName());
        mDownLoadHandler = new DownLoadHandler(context, Looper.getMainLooper());

    }


    /**
     * 设置下载线程数目
     */
    public void setDownloadThreadCount(int mThreadCount) {
        this.THREAD_COUNT = mThreadCount;
    }

    /**
     * 开始下载
     *
     * @param downloadListener 下载监听器
     */
    public void start(final DownloadListener downloadListener) {
        if (downloadListener == null || mDownLoadInfo == null) {
            return;
        }
        mStopThread = false;
        //错误消息清空
        mDownloadResultErrorMessage = null;
        // mDownloadStatus状态加锁
        switch (mDownloadStatus) {
            case DownloadStatus.READY:
                // 下载状态为等待：添加监听器，开始下载
                addDownloadListener(downloadListener);
                downloadListener.onStart(mDownLoadInfo.getUrl());
                download();
                break;
            case DownloadStatus.RUNNING:
                // 下载状态为下载中：添加监听器，对外通知开始事件
                addDownloadListener(downloadListener);
                downloadListener.onStart(mDownLoadInfo.getUrl());
                break;
            case DownloadStatus.FINISHED:
                downloadListener.onStart(mDownLoadInfo.getUrl());
                mDownLoadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 下载状态为下载完： 对外通知开始事件，以及下载结果
                        if (mDownloadResult == DownloadResult.SUCCESS) {
                            downloadListener.onSuccess(mDownLoadInfo.getFolder(),
                                    mDownLoadInfo.getFileName());
                        } else {
                            downloadListener.onFail(mDownloadResult);
                        }
                        downloadListener.onFinish();
                    }
                });


                break;
            default:
                break;
        }
    }

    public void setmDownloadStatus(int mDownloadStatus) {
        this.mDownloadStatus = mDownloadStatus;
    }

    /**
     * 停止下载
     *
     * @param needNofify 是否需要抛出通知
     */
    public void stop(boolean needNofify) {
        mStopThread = true;
        mNeedResultNofity = needNofify;
    }

    /**
     * 下载线程分块
     */
    private void download() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                // 下载线程开始运行
                mDownloadStatus = DownloadStatus.RUNNING;
                HttpURLConnection conn = null;
                try {
                    if (checkFile()) {
                        // 下载目标文件已完毕
                        downloadFinished(DownloadResult.SUCCESS);
                        return;
                    }
                    conn = initConnection(conn);
                    if (conn == null || mFileLength <= 0) {
                        // 下载连接初始化失败
                        downloadFinished(DownloadResult.FAILED_NETWORK);
                        return;
                    }
                    initDownloadThreads();
                    startThread();
                } catch (Exception e) {
                    downloadFinished(DownloadResult.FAILED_FILE);
                } finally {
                    if (conn != null) {
                        try {
                            conn.disconnect();
                            conn = null;
                        } catch (Exception e2) {
                        }
                    }
                }
            }
        };
        ThreadPoolExecutorUtil.getCacheThreadPoolExecutor().execute(runnable);
    }

    /**
     * 初始化下载线程 确定每个线程下载的开始结束位
     */
    private void initDownloadThreads() {

     /*   //矫正线程数（使用上次下载线程数，以利用上次下载进度临时文件）
         if(progressInfoFolder.exists()){
            int c=progressInfoFolder.listFiles().length;
            if((c>1)&&(mThreadCount!=c-1)) {
                mThreadCount = c - 1;
            }
        }*/
        // 设置已下载长度为0
        mDownloadedLength = 0;
        // 确定每个线程下载的开始结束位
        long blockSize = mFileLength / mThreadCount;
        for (int threadId = 1; threadId <= mThreadCount; threadId++) {
            long startIndex = (threadId - 1) * blockSize;
            long endIndex = threadId * blockSize - 1;
            if (threadId == mThreadCount) {
                // 最后一个线程下载到末尾
                endIndex = mFileLength - 1;
            }
            mDownloadThreads.add(new DownloadThread(threadId, startIndex,
                    endIndex));

            mDownloadLengthInfoFileList
                    .add(new File(progressInfoFolder, "."
                            + mDownLoadInfo.getFileName() + "_" + threadId
                            + ".length"));
        }
    }

    /**
     * 启动线程开始下载
     */
    private void startThread() {
        for (int threadId = 1; threadId <= mThreadCount; threadId++) {
            mDownloadThreads.get(threadId - 1).start();
        }
    }

    /**
     * 执行下载线程
     */
    private class DownloadThread extends Thread {
        /**
         * 每次读取流的长度
         */
        private static final int DOWNLOADFILE_BUFFSIZE = 1024 * 100;
        /**
         * 线程ID
         */
        private int mThreadId;
        /**
         * 下载文件开始位置
         */
        private long mStartIndex;
        /**
         * 下载文件结束位置
         */
        private long mEndIndex;
        /**
         * 是否是首次执行线程下载
         */
        private boolean mIsFirst = true;

        /**
         * @param id
         * @param start
         * @param end
         */
        public DownloadThread(int id, long start, long end) {
            mThreadId = id;
            mStartIndex = start;
            mEndIndex = end;
        }

        @Override
        public void run() {
            // 重试三次下载
            for (int i = 0; i < 3; i++) {
                boolean needRetry = downloadFile();
                if (!needRetry || mStopThread) {
                    // 只有在需要重试，且线程为运行状态时，执行重试
                    break;
                }
            }
            // 验证下载的文件
            verifyDownloadedFile();
        }

        /**
         * 计算已经下载的文件长度
         */
        private long calcDownloadedFileLength() {
            FileInputStream tempFile = null;
            long fileLength = 0;
            try {
                File downloadLengthInfoFile = mDownloadLengthInfoFileList
                        .get(mThreadId - 1);
                if (downloadLengthInfoFile.exists()
                        && downloadLengthInfoFile.length() > 0) {
                    tempFile = new FileInputStream(downloadLengthInfoFile);
                    byte[] temp = new byte[1024];
                    int len = tempFile.read(temp);
                    // 已下载的长度
                    fileLength = Integer.parseInt(new String(temp, 0, len));
                    if (mIsFirst) {
                        // 第一次进入下载线程，将已经下载的长度通知到外界。
                        synchronized (mLockObject) {
                            mDownloadedLength += fileLength;
                        }
                        notifyProcessing(mDownloadedLength);
                    }
                }
                mIsFirst = false;
            } catch (Exception e) {

            } finally {
                if (tempFile != null) {
                    try {
                        tempFile.close();
                    } catch (Exception e) {
                    }
                }
            }
            return fileLength;
        }

        /**
         * 下载文件
         *
         * @return 是否需要重试
         */
        public boolean downloadFile() {
            boolean needRetry = false;
            // 下载的文件流
            InputStream downloadFileStream = null;
            // 下载的文件
            RandomAccessFile downloadFileRaf = null;
            // 记录文件下载长度的文件
            RandomAccessFile downloadLengthFileRaf = null;
            // 上一次下载到的位置 (默认为当前线程下载开始位置)
            long downloadIndexOfLast = mStartIndex;
            HttpURLConnection conn = null;
            try {
                // 获取记录当前线程下载长度的文件
                File downloadLengthFile = mDownloadLengthInfoFileList
                        .get(mThreadId - 1);
                long downloadedFileLength = 0;
                if (mSupportBreakpointDownload) {
                    // 获取已下载文件长度
                    downloadedFileLength = calcDownloadedFileLength();
                    // 线程开始位置 + 已下载文件长度
                    downloadIndexOfLast += downloadedFileLength;
                } else {
                    // 不支持断点续传，每次启动下载，都是从头开始下载（单线程）
                    mDownloadedLength = 0;
                }

                // 文件当前下载长度大于文件结束位置，说明已经下载完
                if (downloadIndexOfLast >= mEndIndex) {
                    return false;
                }

                // 线程在外部停止了，不需要进行重试下载
                if (!isThreadRunning()) {
                    return false;
                }

                // 初始化连接失败 返回需要重试
                conn = initConnection(conn, downloadIndexOfLast, mEndIndex);
                if (conn == null) {
                    return true;
                }
                mConnections.add(conn);

                // 定位随机写文件时候在那个位置开始写
                downloadFileRaf = new RandomAccessFile(mDownloadFile, "rwd");
                downloadFileRaf.seek(downloadIndexOfLast);
                int downLoadBuffLength = 0;
                byte[] downLoadBuff = new byte[DOWNLOADFILE_BUFFSIZE];

                downloadFileStream = conn.getInputStream();
                downloadLengthFileRaf = new RandomAccessFile(
                        downloadLengthFile, "rwd");
                while (isThreadRunning()) {
                    // 读取的流长度为0 退出循环
                    downLoadBuffLength = downloadFileStream.read(downLoadBuff);
                    if (downLoadBuffLength <= 0) {
                        break;
                    }

                    // 将下载的文件流写入文件
                    downloadFileRaf.write(downLoadBuff, 0, downLoadBuffLength);

                    if (mSupportBreakpointDownload) {
                        downloadedFileLength += downLoadBuffLength;

                        // 将下载的文件的长度 写入记录文件长度的文件
                        downloadLengthFileRaf.seek(0);
                        downloadLengthFileRaf.write(String.valueOf(
                                downloadedFileLength).getBytes());
                    }

                    if (isThreadRunning()) {
                        // 当写入操作完成后，发现如果线程已经停止，不通知
                        synchronized (mLockObject) {
                            mDownloadedLength += downLoadBuffLength;
                        }
                        notifyProcessing(mDownloadedLength);
                    }
                }
            } catch (Exception e) {
                needRetry = true;
                mDownloadResultErrorMessage = e.getMessage();
            } finally {
                mConnections.remove(conn);
                if (downloadFileStream != null) {
                    try {
                        downloadFileStream.close();
                    } catch (IOException e) {
                    }
                }
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception e2) {
                    }
                }
                if (downloadFileRaf != null) {
                    try {
                        downloadFileRaf.close();
                    } catch (IOException e) {
                    }
                }
                if (downloadLengthFileRaf != null) {
                    try {
                        downloadLengthFileRaf.close();
                    } catch (Exception e) {
                    }
                }
            }
            return needRetry;
        }

        /**
         * 线程正在执行
         */
        private boolean isThreadRunning() {
            return !isInterrupted() && !mStopThread;
        }
    }

    /**
     * 校验下载完成的文件
     */
    private void verifyDownloadedFile() {
        synchronized (mLockObject) {
            mRunningThreadCount--;
            // 等待线程全部停止
            if (mRunningThreadCount > 0) {
                return;
            }
            // mIsDownloadAlive = false;

            // 下载长度小于文件长度，说明下载未完成（失败）
            if (mDownloadedLength < mFileLength) {
                if (mStopThread) {
                    downloadFinished(DownloadResult.PAUSE);
                } else {
                    downloadFinished(DownloadResult.FAILED_FILE);
                }
                //文件长度不匹配，删除已经下载的文件
                //clearDownLoadFile(mDownloadFile.getAbsolutePath());
                return;
            }
            //下载完成，删除临时文件
            if (progressInfoFolder != null) {
                FileHelper.deleteDir(progressInfoFolder.getAbsolutePath());
                progressInfoFolder.delete();
            }
            // mac存在 且mac校验失败
            if (!TextUtils.isEmpty(mDownLoadInfo.getFileMac())
                    && !mDownLoadInfo.getFileMac().equals(
                    MD5Utils.md5sum(mDownloadFile.getAbsolutePath()))) {
                clearDownLoadFile(mDownloadFile.getAbsolutePath());
                downloadFinished(DownloadResult.FAILED_MD5);
                return;
            }

            downloadFinished(DownloadResult.SUCCESS);
        }
    }

    /**
     * 发送 下载进度改变时的通知
     *
     * @param downloadedLength 已下载文件长度
     */
    private void notifyProcessing(final long downloadedLength) {

      /*Message m = Message.obtain();
        m.what = 1;
        m.obj = downloadedLength;
        mDownLoadHandler.sendMessage(m);*/

        mDownLoadHandler.post(new Runnable() {
            @Override
            public void run() {
                updateProgress(downloadedLength);
            }
        });

    }

    /**
     * 下载进度改变时的通知
     *
     * @param downloadedLength
     */
    private void updateProgress(long downloadedLength) {

        for (int i = 0; i < mDownloadListeners.size(); i++) {
            mDownloadListeners.get(i).onProgressChange(downloadedLength,
                    mFileLength);
        }
    }

    /**
     * 下载完成
     *
     * @param downloadResult 是否下载成功
     */
    private void downloadFinished(int downloadResult) {
        // 清空下载所用资源
        disposeDownloader();
        // 设置下载是否成功
        mDownloadResult = downloadResult;
        // 下载状态设置为完成
        mDownloadStatus = DownloadStatus.FINISHED;
        // 向外界通知下载结果
        mDownLoadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyDownloadResult();
            }
        });

    }

    /**
     * 向外界通知下载结果
     */
    private void notifyDownloadResult() {
        if (mNeedResultNofity) {
            // 需要通知成功或失败的结果
            if (mDownloadResult == DownloadResult.SUCCESS) {
                for (int i = 0; i < mDownloadListeners.size(); i++) {
                    mDownloadListeners.get(i).onSuccess(
                            mDownLoadInfo.getFolder(),
                            mDownLoadInfo.getFileName());
                }
            } else if (mDownloadResult == DownloadResult.PAUSE) {
                for (int i = 0; i < mDownloadListeners.size(); i++) {
                    mDownloadListeners.get(i).onPause();
                }
            } else  {
                for (int i = 0; i < mDownloadListeners.size(); i++) {
                    mDownloadListeners.get(i).onFail(mDownloadResult);
                }
            }
        }
//        for (int i = 0; i < mDownloadListeners.size(); i++) {
//            mDownloadListeners.get(i).onFinish();
//        }
    }

    /**
     * 清空下载资源
     */
    private void disposeDownloader() {
        // 在正在下载池中删除自己
        DownloadPool.removeDownloader(mDownLoadInfo.getUrl());
        // 停止并清空下载线程
        clearDownloadThreads();
        // 清空网络连接队列
        clearHttpConnections();
    }

    /**
     * 清空下载线程
     */
    private void clearDownloadThreads() {
        if (ListUtil.isEmpty(mDownloadThreads)) {
            return;
        }
        for (int i = 0; i < mDownloadThreads.size(); i++) {
            try {
                mDownloadThreads.get(i).interrupt();
                mDownloadThreads.get(i).join();
            } catch (Exception e) {

            }
        }
        mDownloadThreads.clear();
    }

    /**
     * 清空网络请求队列
     */
    private void clearHttpConnections() {
        for (int i = 0; i < mConnections.size(); i++) {
            if (mConnections.get(i) != null) {
                try {
                    mConnections.get(i).disconnect();
                } catch (Exception e) {
                }
            }
        }
        mConnections.clear();
    }

    /**
     * 添加监听器
     */
    private void addDownloadListener(DownloadListener downloadListener) {
        synchronized (mLockObject) {
            if (downloadListener != null) {
                mDownloadListeners.add(downloadListener);
            }
        }
    }

    /**
     * 删除下载监听器
     */
    public void deleteDownloadListener(DownloadListener downloadListener) {
        synchronized (mLockObject) {
            if (downloadListener != null) {
                mDownloadListeners.remove(downloadListener);
            }
        }
    }

    /**
     * 初始化连接
     */
    private HttpURLConnection initConnection(HttpURLConnection conn) {
        return initConnection(conn, -1, -1);
    }

    /**
     * 初始化连接
     *
     * @param start 下载开始位置
     * @param end   下载结束位置
     */
    private HttpURLConnection initConnection(HttpURLConnection conn,
                                             long start, long end) {

        long fileLength = 0;
        int size = 0;
        try {
            while (fileLength <= 0 && size <= 10) {
                if (conn != null) {
                    conn.disconnect();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                size++;
                URL url = new URL(mDownLoadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setUseCaches(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("Cache-Control", "no-cache");
                conn.setRequestProperty("Pragma", "no-cache");
                if (mDownLoadInfo.getHost() != null) {
                    conn.setRequestProperty("Host", mDownLoadInfo.getHost());
                }
                if (start > -1 && mSupportBreakpointDownload) {
                    conn.setRequestProperty("Range", "bytes=" + start + "-"
                            + end);
                }
                // 连接
                conn.connect();

                fileLength = conn.getContentLength();

                if (start == -1) {
                    mFileLength = fileLength;
                    if (mFileLength < 0 && size >= 4) {
                        mSupportBreakpointDownload = false;
                    }

                    if (!mSupportBreakpointDownload) {
                        // 不支持断点续传，不能启动多线程分块下载
                        mThreadCount = 1;
                        mRunningThreadCount = 1;
                    }
                }

                //	mlog("threadid = " + Thread.currentThread().getName() + " fileLength = " + fileLength);
            }
        } catch (Exception e) {
            //获取异常信息
            mDownloadResultErrorMessage = e.getMessage();
            if (conn != null) {
                try {
                    conn.disconnect();
                    conn = null;
                } catch (Exception e2) {
                    mDownloadResultErrorMessage = e2.getMessage();
                }
            }
        }
        return conn;
    }

    /**
     * 初始化MD5记录
     */
    private void initMD5File(String fileMac) {
        if (mMd5File.exists()) {
            return;
        }

        FileWriter fileWriter = null;
        try {
            mMd5File.createNewFile();

            fileWriter = new FileWriter(mMd5File);
            fileWriter.write(fileMac);
            fileWriter.flush();
        } catch (Exception e) {
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 检查文件 <p> 校验文件完整性 、删除不正确的下载的垃圾文件、能否使用assets/plugins/目录下的缓存文件，或本地已存在符合条件的文件
     *
     * @return true：可以使用； false:不可以使用，需要下载
     */
    private boolean checkFile() {
        if (TextUtils.isEmpty(mDownLoadInfo.getFileMac())) {
            // @FixMe 没有md5值,下载新的文件时，删除以前的下载文件
            clearDownLoadFile(mDownloadFile.getAbsolutePath());
            return false;
        }

        String mac = null;

        // 确认下载路径中已存在的文件是否符合条件
        if (mDownloadFile.exists()) {
            mac = MD5Utils.md5sum(mDownloadFile.getAbsolutePath());
        } else {
            clearDownLoadFile(mDownloadFile.getAbsolutePath());
        }
        if (mac != null && (mDownLoadInfo.getFileMac().toLowerCase()).equals(mac)) {
            return true;
        }

        // 校验md5文件中的md5值
        if (mMd5File.exists()) {
            FileInputStream tempFile = null;
            try {
                tempFile = new FileInputStream(mMd5File);
                byte[] temp = new byte[mDownLoadInfo.getFileMac().length() + 1];
                int len = tempFile.read(temp);
                mac = new String(temp, 0, len);
            } catch (Exception e) {
            } finally {
                if (tempFile != null) {
                    try {
                        tempFile.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (mac == null || !mDownLoadInfo.getFileMac().equals(mac)) {
            clearDownLoadFile(mDownloadFile.getAbsolutePath());
        }

        initMD5File(mDownLoadInfo.getFileMac());

        //下载路径中的文件不符合预期，module和assets中文件做对比
        try {
            // asset文件缓存规则：目录是assets/cache/ ；文件命名：cache_文件名.后缀名.jpg
            StringBuilder assetsFileName = new StringBuilder("plugins/cache_");
            assetsFileName.append(mDownLoadInfo.getFileName()).append(".apk");
            InputStream ins = ContextManager.getApplication().getResources()
                    .getAssets().open(assetsFileName.toString());
            String md5 = MD5Utils.md5Stream(ins);
            if (mDownLoadInfo.getFileMac().equals(md5)) {
                // 如果assets文件符合条件
                if (mDownloadFile.exists()) {
                    mDownloadFile.delete();
                }
                FileHelper.copyAssestFile(ContextManager.getApplication(),
                        assetsFileName.toString(), mDownLoadInfo.getFolder(),
                        mDownLoadInfo.getFileName());
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 清除下载文件目录
     */
    private void clearDownLoadFolder() {
        if (mDownLoadInfo.getFolder() == null) {
            return;
        }
        FileHelper.deleteDir(mDownLoadInfo.getFolder());
    }


    /**
     * 清除下载文件及附属临时文件
     */
    private void clearDownLoadFile(String path) {
        if (mDownLoadInfo.getFolder() == null) {
            return;
        }
        File file = new File(path);
        if (file != null && file.exists()) {
            file.delete();
        }

        if (progressInfoFolder != null && progressInfoFolder.exists()) {
            FileHelper.deleteDir(progressInfoFolder.getAbsolutePath());
        }
    }

    /**
     * 返回下载错误异常信息
     *
     * @return
     */
    public String getDownloadResultErrorMessage() {
        return mDownloadResultErrorMessage;
    }


    /**
     * 下载handler
     */
    private class DownLoadHandler extends Handler {

        WeakReference<Context> mWeakReference;

        public DownLoadHandler(Context context, Looper looper) {
            super(looper);
            if (context != null) {
                mWeakReference = new WeakReference<Context>(context);
            }
        }


        public DownLoadHandler(Context context) {
            if (context != null) {
                mWeakReference = new WeakReference<Context>(context);
            }
        }

        @Override
        public void handleMessage(Message msg) {

            if (mWeakReference == null || mWeakReference.get() != null) {
                super.handleMessage(msg);
                if (msg != null && msg.what == 1) {
                    updateProgress((Long) msg.obj);
                }
            }

        }
    }
}
