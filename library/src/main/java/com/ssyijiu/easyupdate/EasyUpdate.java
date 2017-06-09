package com.ssyijiu.easyupdate;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.ssyijiu.easyupdate.callback.OnProgressCallback;
import com.ssyijiu.easyupdate.tools.$;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by ssyijiu on 2017/6/2.
 * Github: ssyijiu
 * E-mail: lxmyijiu@163.com
 */

public class EasyUpdate {

    private DownloadService downService;
    private DownServiceConn downServiceConn;
    private boolean bind;

    private OnProgressCallback progressCallback;


    private EasyUpdate() {
    }


    private static Application sApp;
    private static int sIcon;


    public static void init(Application app, int icon) {
        sApp = app;
        sIcon = icon;
        // MLog.setLogLev(MLog.LogLev.NO_LOG);
    }


    private static final EasyUpdate instance = new EasyUpdate();


    public static EasyUpdate instance() {
        return instance;
    }


    public static Context context() {
        return sApp;
    }


    public static int icon() {
        return sIcon;
    }


    public void start(String downUrl) {
        if ($.isSDCardAvailable()) {
            start(downUrl, $.getSDCardPath());
        } else {
            $.toast("SDCard is not available");
        }
    }


    public void start(String downUrl, String savePath) {

        downServiceConn = new DownServiceConn();

        // 开启服务
        final Intent intent = DownloadService.newIntent(downUrl, savePath);
        DownloadService.start(intent);
        EasyUpdate.context().bindService(intent, downServiceConn, BIND_AUTO_CREATE);
    }


    public void pause() {
        if (isBind()) {
            downService.pause();
        }
    }


    public void cancel() {
        if (isBind()) {
            downService.cancel();
        }
    }


    public void setProgressCallback(OnProgressCallback callback) {
        this.progressCallback = callback;
    }


    public void destory() {
        if (downServiceConn != null && isBind()) {
            EasyUpdate.context().unbindService(downServiceConn);
            downServiceConn = null;
        }
    }


    private class DownServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bind = true;
            downService = ((DownloadService.DownBinderImpl) service).getService();
            // 服务绑定后，开始下载
            downService.start();
            if (progressCallback != null) {
                downService.setOnProgressCallback(progressCallback);
            }
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            bind = false;
        }
    }


    public boolean isBind() {
        return bind && downService != null;
    }
}
