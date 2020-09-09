package com.xueersi.downloadtask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xueersi.downloadtask.customprogressbar.ProgressBarActivity;
import com.xueersi.downloadtask.download.DownLoadListener;
import com.xueersi.downloadtask.download.DownLoadService;
import com.xueersi.downloadtask.downloader.DownloaderActivity;
import com.xueersi.downloadtask.downloadpool.ContextManager;
import com.xueersi.downloadtask.downloadpool.DownloaderPoolActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int WRITE_PERMISSION_CODE = 1000;
    //文件下载链接
    private String url = "http://10.170.78.13:8080/job/android/job/android_test/1296/artifact/app/build/outputs/apk/app-xesmarket-Debug.apk";

    private Context mContext;
    private TextView progress;


    private Button btnStartDownLoad, btnPauseDownLoad, btnCancelDownLoad;

    private DownLoadService.DownLoadBinder downLoadBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downLoadBinder = (DownLoadService.DownLoadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            Log.d("", "");
        }
    };


    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            progress.setText(msg.arg1 + "");

        }
    };

    private DownLoadListener downLoadListener = new DownLoadListener() {
        @Override
        public void onStart() {
            Toast.makeText(mContext, "下载开始", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgress(int progress) {
//            getNotificationManager().notify(1, getNotification("正在下载", progress));

            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.arg1 = progress;

            handler.sendMessage(obtain);
        }

        @Override
        public void onSuccess() {
//            downLoadTask = null;
//            stopForeground(true);
//            getNotificationManager().notify(1, getNotification("下载完成", -1));
            Toast.makeText(mContext, "下载完成！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
//            downLoadTask = null;
//            stopForeground(true);
//            getNotificationManager().notify(1, getNotification("下载失败", -1));
            Toast.makeText(mContext, "下载失败！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
//            downLoadTask = null;
            Toast.makeText(mContext, "下载暂停！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
//            downLoadTask = null;
//            stopForeground(true);
            Toast.makeText(mContext, "下载取消！", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        btnStartDownLoad = findViewById(R.id.Main_btnStartDownLoad);
        btnPauseDownLoad = findViewById(R.id.Main_btnPauseDownLoad);
        btnCancelDownLoad = findViewById(R.id.Main_btnCancelDownLoad);
        btnStartDownLoad.setOnClickListener(this);
        btnPauseDownLoad.setOnClickListener(this);
        btnCancelDownLoad.setOnClickListener(this);
        findViewById(R.id.Main_btnintent).setOnClickListener(this);
        findViewById(R.id.Main_btnintentpoor).setOnClickListener(this);
        findViewById(R.id.Main_btnintentprogress).setOnClickListener(this);
        progress = findViewById(R.id.progress);

        checkUserPermission();
        Intent intent = new Intent(mContext, DownLoadService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void onClick(View view) {
        if (downLoadBinder == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.Main_btnStartDownLoad: {
                downLoadBinder.startDownLoad(url, downLoadListener);
                break;
            }
            case R.id.Main_btnPauseDownLoad: {
                downLoadBinder.pauseDownLoad();
                break;
            }
            case R.id.Main_btnCancelDownLoad: {
                downLoadBinder.cancelDownLoad();
                break;
            }
            case R.id.Main_btnintent: {
                Intent intent = new Intent(this, DownloaderActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.Main_btnintentpoor: {
                Intent intent = new Intent(this, DownloaderPoolActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.Main_btnintentprogress: {
                Intent intent = new Intent(this, ProgressBarActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }


    /**
     * 检查用户权限
     */
    private void checkUserPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "拒绝权限将无法开启下载服务", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }
    }


}