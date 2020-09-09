package com.xueersi.downloadtask.downloadpool;

/**
 * @author shixiaoqiang on 2015/5/17.
 */

public class DownloadResult {
    /**
     * 未知
     */
    public static final int UNKNOWN = -1;
    /**
     * 成功
     */
    public static final int SUCCESS = 0;
    /**
     * MD5错误
     */
    public static final int FAILED_MD5 = 1;
    /**
     * 网络错误 (无法访问文件下载地址)
     */
    public static final int FAILED_NETWORK = 2;
    /**
     * 文件错误
     */
    public static final int FAILED_FILE = 3;

    /**
     * 暂停下载
     */
    public static final int PAUSE = 4;

}
