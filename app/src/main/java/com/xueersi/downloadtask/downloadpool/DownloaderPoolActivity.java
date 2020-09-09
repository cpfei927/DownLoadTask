package com.xueersi.downloadtask.downloadpool;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xueersi.downloadtask.R;
import com.xueersi.downloadtask.downloader.DownLoaderManger;
import com.xueersi.downloadtask.downloader.FileInfo;
import com.xueersi.downloadtask.downloader.db.DbHelper;
import com.xueersi.downloadtask.downloader.view.NumberProgressBar;
import com.xueersi.downloadtask.downloader.view.OnProgressListener;

import java.util.HashMap;
import java.util.Map;


public class DownloaderPoolActivity extends AppCompatActivity {

    private NumberProgressBar pb, pbtwo;//进度条


    String url1 = "http://downloadz.dewmobile.net/Official/Kuaiya482.apk";
    String url2 = "http://10.170.78.13:8080/job/android/job/android_test/1296/artifact/app/build/outputs/apk/app-xesmarket-Debug.apk";


    Map<String, DownLoader> downLoaderMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        pb = (NumberProgressBar) findViewById(R.id.pb);
        pbtwo = (NumberProgressBar) findViewById(R.id.pbtwo);
        final Button start = (Button) findViewById(R.id.start);//开始下载
        final Button restart = (Button) findViewById(R.id.restart);//重新下载
        final Button starttwo = (Button) findViewById(R.id.starttwo);//重新下载

        if (downLoaderMap == null) {
            downLoaderMap = new HashMap<>();
        }

        String filename = url1.substring(url1.lastIndexOf("/"));
        DownLoadInfo imgInfo = DownLoadInfo.createFileInfo(url1, FILE_PATH, filename, "ddfq");
        DownLoader downLoader = new DownLoader(imgInfo);
        downLoaderMap.put(url1, downLoader);


        String filename2 = url2.substring(url2.lastIndexOf("/"));
        DownLoadInfo imgInfo2 = DownLoadInfo.createFileInfo(url2, FILE_PATH, filename2, "ddfq");
        DownLoader downLoader2 = new DownLoader(imgInfo2);
        downLoaderMap.put(url2, downLoader2);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toString = start.getText().toString();
                if (toString.equals("开始下载")) {
                    startLoad(downLoaderMap.get(url1), pb);
                    start.setText("暂停下载");
                } else {
                    start.setText("开始下载");
                    DownLoader downLoader = downLoaderMap.get(url1);
                    if (downLoader != null) {
                        downLoader.stop(true);
                    }
                }
            }
        });

        starttwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toString = starttwo.getText().toString();
                if (toString.equals("开始下载")) {
                    startLoad(downLoaderMap.get(url2), pbtwo);
                    starttwo.setText("暂停下载");
                } else {
                    starttwo.setText("开始下载");
                    DownLoader downLoader = downLoaderMap.get(url2);
                    if (downLoader != null) {
                        downLoader.stop(true);
                    }
                }
            }
        });

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DownloaderPoolActivity.this.downLoader.restart(info.getUrl());
//                start.setText("暂停下载");
            }
        });
    }

    public static String FILE_PATH = Environment.getExternalStorageDirectory() + "/abcxes";

    private void startLoad(DownLoader downLoader, final NumberProgressBar pb) {




        downLoader.setmDownloadStatus(DownloadStatus.READY);

        downLoader.start(new DownloadListener() {
            @Override
            public void onStart(String url) {
                Toast.makeText(DownloaderPoolActivity.this, "onStart", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgressChange(long currentLength, long fileLength) {
                pb.setMax((int) fileLength);
                pb.setProgress((int) currentLength);
            }

            @Override
            public void onSuccess(String folderPath, String fileName) {
                Toast.makeText(DownloaderPoolActivity.this, "onSuccess", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFail(int errorCode) {
                Toast.makeText(DownloaderPoolActivity.this, "onFail", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onPause() {
                Toast.makeText(DownloaderPoolActivity.this, "onPause", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinish() {
                Toast.makeText(DownloaderPoolActivity.this, "onFinish", Toast.LENGTH_LONG).show();

            }
        });

    }

}
