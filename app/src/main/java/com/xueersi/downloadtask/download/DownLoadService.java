package com.xueersi.downloadtask.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.xueersi.downloadtask.MainActivity;
import com.xueersi.downloadtask.R;

import java.io.File;

/**
 * @Author: cpfei
 * @CreateDate: 2020/9/3
 * @UpdateUser: 更新者：
 * @UpdateDate: 2020/9/3
 * @description:后台下载服务
 */
public class DownLoadService extends Service {




    private DownLoadTask downLoadTask;
    private String downLoadUrl;

    public DownLoadService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 自定义Binder类
     */
    public class DownLoadBinder extends Binder {

        /**
         * 开始下载
         *
         * @param url
         * @param downLoadListener
         */
        public void startDownLoad(String url, DownLoadListener downLoadListener) {
            downLoadUrl = url;
            downLoadTask = new DownLoadTask(downLoadListener);
            downLoadTask.execute(downLoadUrl);
//            if (downLoadTask == null) {
//                startForeground(1, getNotification("开始下载", 0));
//            }
        }

        /**
         * 暂停下载
         */
        public void pauseDownLoad() {
            if (downLoadTask != null) {
                downLoadTask.pauseDownLoad();
            }
        }

        /**
         * 取消下载
         */
        public void cancelDownLoad() {
            if (downLoadTask != null) {
                downLoadTask.cancelDownLoad();
            }
            //文件删除
            if (downLoadUrl != null) {
                String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory, fileName);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownLoadService.this, "下载取消！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private DownLoadBinder downLoadBinder = new DownLoadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return downLoadBinder;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * 显示通知
     *
     * @param title
     * @param progress
     * @return
     */
    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //创建渠道
            String id = "my_channel_01";
            String name = "channelName";
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            getNotificationManager().createNotificationChannel(mChannel);
            //设置图片,通知标题,发送时间,提示方式等属性
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, id);
            builder.setContentTitle(title)  //标题
                    .setWhen(System.currentTimeMillis())    //系统显示时间
                    .setSmallIcon(R.mipmap.ic_launcher)     //收到信息后状态栏显示的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//大图标
                    .setAutoCancel(true);       //设置点击后取消Notification
            builder.setContentIntent(pendingIntent);    //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        } else {
            //设置图片,通知标题,发送时间,提示方式等属性
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentTitle(title)  //标题
                    .setWhen(System.currentTimeMillis())    //系统显示时间
                    .setSmallIcon(R.mipmap.ic_launcher)     //收到信息后状态栏显示的小图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))//大图标
                    .setAutoCancel(true);       //设置点击后取消Notification
            builder.setContentIntent(pendingIntent);    //绑定PendingIntent对象
            if (progress >= 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        }
    }


}
