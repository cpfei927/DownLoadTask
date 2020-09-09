package com.xueersi.downloadtask.downloader.view;

/**
 * @Author: cpfei
 * @CreateDate: 2020/9/3
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/9/3
 * 进度更新
 */
public interface OnProgressListener {

    void updateProgress(int max, int progress);
}
