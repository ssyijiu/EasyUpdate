package com.ssyijiu.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.ssyijiu.easyupdate.EasyUpdate;
import com.ssyijiu.easyupdate.callback.OnProgressCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private String downUrl
        = "http://shouji.360tpcdn.com/160914/c5164dfbbf98a443f72f32da936e1379/com.tencent.mobileqq_410.apk";
    private EditText etPathUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        EasyUpdate.instance().setProgressCallback(new OnProgressCallback() {
            @Override public void onProgress(int progress) {
                progressBar.setProgress(progress);
            }
        });

        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.bspatch).setOnClickListener(this);
        etPathUrl = (EditText) findViewById(R.id.patchUrl);

    }


    @Override protected void onDestroy() {
        super.onDestroy();
        EasyUpdate.instance().destory();
    }


    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                EasyUpdate.instance().start(downUrl);
                break;
            case R.id.pause:
                EasyUpdate.instance().pause();
                break;
            case R.id.cancel:
                EasyUpdate.instance().cancel();
                progressBar.setProgress(0);
                break;
            case R.id.bspatch:
                EasyUpdate.instance().start(etPathUrl.getText().toString().trim());
                break;
        }
    }
}
