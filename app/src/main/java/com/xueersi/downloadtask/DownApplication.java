package com.xueersi.downloadtask;

import android.app.Application;

import com.xueersi.downloadtask.downloadpool.ContextManager;

/**
 * @Author: cpfei
 * @CreateDate: 2020/9/7
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/9/7
 * @description:
 */
public class DownApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ContextManager.initContext(this);
    }
}
