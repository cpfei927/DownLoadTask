package com.xueersi.downloadtask.download;

/**
 * @Author: cpfei
 * @CreateDate: 2020/9/3
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/9/3
 * @description:
 */
public interface DownLoadListener {

    void onStart();
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();


}
