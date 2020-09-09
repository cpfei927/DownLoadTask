package com.xueersi.downloadtask.downloadpool;

/**
 * 下载进度通知器
 *
 * @author shixiaoqiang
 */
public interface DownloadListener {
    /**
     * 开始下载
     */
    void onStart(String url);

    /**
     * 下载进度变化
     *
     * @param currentLength 下载进度
     * @param fileLength    文件总长度（按字节算）
     */
    void onProgressChange(long currentLength, long fileLength);

    /**
     * 下载成功
     *
     * @param folderPath 下载文件夹路径
     * @param fileName   下载文件名
     */
    void onSuccess(String folderPath, String fileName);

    /**
     * 下载失败
     *
     * @param errorCode 失败code
     */
    void onFail(int errorCode);

    /**
     * 暂停
     *
     */
    void onPause();

    /**
     * 下载完成
     */
    void onFinish();
}
