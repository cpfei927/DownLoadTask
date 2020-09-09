package com.xueersi.downloadtask.downloadpool;

import java.io.Serializable;

/**
 * 下载所需信息
 *
 * @author shixiaoqiang on 2015/5/17.
 */

public class DownLoadInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 下载地址
     */
    private String mUrl;
    /**
     * 文件夹地址
     */
    private String mFolder;
    /**
     * 文件名
     */
    private String mFileName;
    /**
     * 文件验签md5编码， md5Sum算法（如：9ac572bb2698cd238b0821beee468da4）
     */
    private String mFileMac;
    /**
     * 下载类型
     */
    private DownloadType mDownloadType;
    /**
     * host
     */
    private String mHost;

    /**
     * 私有化默认构造函数
     */
    private DownLoadInfo() {
    }

    /**
     * 创建下载文件的信息
     */
    public static DownLoadInfo createFileInfo(String url, String folder,
                                              String fileName, String fileMac) {
        return new DownLoadInfo(url, folder, fileName, fileMac);
    }

    /**
     * 创建下载文件的信息
     */
    public static DownLoadInfo createImgInfo(String url) {
        return new DownLoadInfo(url);
    }

    /**
     * 构造函数 构造文件下载信息
     */
    private DownLoadInfo(String url, String folder, String fileName,
                         String fileMac) {
        mUrl = url;
        mFolder = folder;
        mFileName = fileName;
        mFileMac = fileMac;
        mDownloadType = DownloadType.FILE;
    }

    /**
     * 构造函数 构造图片下载信息
     */
    private DownLoadInfo(String url) {
        mUrl = url;
        mDownloadType = DownloadType.IMG;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getFolder() {
        return mFolder;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFileMac() {
        return mFileMac;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String mHost) {
        this.mHost = mHost;
    }

    /**
     * 下载类型
     *
     * @author xingtongju
     */
    public static class DownloadType {

        /**
         * 下载类型：文件
         */
        public static final DownloadType FILE = new DownloadType();
        /**
         * 下载类型：图片
         */
        public static final DownloadType IMG = new DownloadType();

        private DownloadType() {

        }
    }

    /**
     * 获取下载类型
     */
    public DownloadType getDownloadType() {
        return mDownloadType;
    }

    /**
     * 设置下载类型
     *
     * @see DownloadType , DownLoadType.FILE 下载文件； DownloadType.IMG:图片
     */
    public void setDownloadType(DownloadType mDownloadType) {
        this.mDownloadType = mDownloadType;
    }
}

