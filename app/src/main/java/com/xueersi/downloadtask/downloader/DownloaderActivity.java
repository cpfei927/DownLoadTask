package com.xueersi.downloadtask.downloader;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.xueersi.downloadtask.R;
import com.xueersi.downloadtask.downloader.db.DbHelper;
import com.xueersi.downloadtask.downloader.view.NumberProgressBar;
import com.xueersi.downloadtask.downloader.view.OnProgressListener;


public class DownloaderActivity extends AppCompatActivity implements OnProgressListener {

    private NumberProgressBar pb;//进度条
    private DownLoaderManger downLoader = null;
    private FileInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);
        pb = (NumberProgressBar) findViewById(R.id.pb);
        final Button start = (Button) findViewById(R.id.start);//开始下载
        final Button restart = (Button) findViewById(R.id.restart);//重新下载
        final DbHelper helper = new DbHelper(this);
        downLoader = DownLoaderManger.getInstance(helper, this);
        info = new FileInfo("Kuaiya482.apk", "http://downloadz.dewmobile.net/Official/Kuaiya482.apk");
        downLoader.addTask(info);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downLoader.getCurrentState(info.getUrl())) {
                    downLoader.stop(info.getUrl());
                    start.setText("开始下载");
                } else {
                    downLoader.start(info.getUrl());
                    start.setText("暂停下载");
                }
            }
        });
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downLoader.restart(info.getUrl());
                start.setText("暂停下载");
            }
        });
    }

    @Override
    public void updateProgress(final int max, final int progress) {
        pb.setMax(max);
        pb.setProgress(progress);
    }
}
