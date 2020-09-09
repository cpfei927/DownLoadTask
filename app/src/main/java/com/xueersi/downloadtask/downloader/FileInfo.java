package com.xueersi.downloadtask.downloader;

/**
 * @Author: cpfei
 * @CreateDate: 2020/9/3
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/9/3
 * @description: 保存下载文件信息
 */
public class FileInfo {
    private String fileName;//文件名
    private String url;//下载地址
    private int length;//文件大小
    private int finished;//下载以已完成进度
    private boolean isStop = false;//是否暂停下载
    private boolean isDownLoading = false;//是否正在下载

    public FileInfo() {
    }

    public FileInfo(String fileName, String url) {
        this.fileName = fileName;
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }

    public boolean isDownLoading() {
        return isDownLoading;
    }

    public void setDownLoading(boolean downLoading) {
        isDownLoading = downLoading;
    }
}
