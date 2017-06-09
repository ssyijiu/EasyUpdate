package com.ssyijiu.easyupdate;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.ssyijiu.easyupdate.callback.DownloadCallback;
import com.ssyijiu.easyupdate.callback.OnProgressCallback;
import com.ssyijiu.easyupdate.tools.$;
import com.ssyijiu.easyupdate.tools.BsPatch;
import com.ssyijiu.easyupdate.tools.MLog;
import com.ssyijiu.easyupdate.tools.NotificationUtil;
import java.io.File;

/**
 * Created by ssyijiu on 2017/5/26.
 * Github: ssyijiu
 * E-mail: lxmyijiu@163.com
 */

public class DownloadService extends Service {

    private final static String EXTRA_DOWNURL = "extra_downurl";
    private final static String EXTRA_SAVEPATH = "extra_savepath";

    private String downUrl;
    private String savePath;
    private File saveFile;

    // Notification
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;

    private boolean isDownloading = false;
    private OnProgressCallback progressCallback;

    // icon and appName
    private int icon = EasyUpdate.icon() <= 0 ? R.mipmap.ic_launcher : EasyUpdate.icon();
    private String appName = $.getAppName();


    @Override public void onCreate() {
        super.onCreate();
        MLog.i("Service onCreate");

        // 初始化通知
        notificationBuilder = new NotificationCompat.Builder(this);
        notification = NotificationUtil.easyNotification(
            notificationBuilder, icon,
            appName, $.getString(R.string.download_ready), false);

    }


    @Nullable @Override public IBinder onBind(Intent intent) {
        return new DownBinderImpl();
    }


    // 重复开启服务不会走 onCreate，会走 onStartCommand
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        MLog.i("Service onStartCommand");

        // 将服务设置为前台服务
        // 注意：这个 ID 不能为 0，为 0 的话这句代码无效
        // 因为后面通知会不断更新，注意和 NOTIFICATION_ID 一样，要不就是发出两个通知了
        startForeground(NotificationUtil.NOTIFICATION_ID, notification);

        if (intent != null) {
            downUrl = intent.getStringExtra(EXTRA_DOWNURL);
            savePath = intent.getStringExtra(EXTRA_SAVEPATH);

            if (!TextUtils.isEmpty(savePath)) {
                saveFile = new File(savePath, getFileName(downUrl));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    static Intent newIntent(final String downUrl, final String savePath) {
        Intent intent = new Intent(EasyUpdate.context(), DownloadService.class);
        intent.putExtra(EXTRA_DOWNURL, downUrl);
        intent.putExtra(EXTRA_SAVEPATH, savePath);
        return intent;
    }


    static void start(Intent intent) {
        EasyUpdate.context().startService(intent);
    }


    class DownBinderImpl extends Binder {

        DownloadService getService() {
            return DownloadService.this;
        }  // 这样 binder 就可以调用服务中的公共方法

    }


    /**
     * 开始下载
     */
    void start() {

        // 已经下载好直接安装
        // if ($.checkApk(saveFile.getAbsolutePath())) {
        //     $.install(saveFile);
        //     return;
        // }

        if (isDownloading) {
            $.toast(R.string.download_already);
            return;
        }
        isDownloading = true;

        // 开始下载
        HttpRequest.download(downUrl, savePath, new DownloadCallback() {

            @Override public void downloadSuccess(final File file) {
                isDownloading = false;

                // 停止服务
                // bindService 后，只有所有客户端 unbindService，stopSelf 才会真正结束服务
                DownloadService.this.stopSelf();

                // 增量升级
                if (isPatch(file)) {
                    patchUpdate(file);
                } else {
                    $.install(file);
                }

                // NotificationUtil.cancel();     // 前台通知使用 cancel 无法移除
                stopForeground(true);             // 移除前台通知
            }


            @Override
            public void downloadProgress(long currentProgress, long totalProgress) {

                int progress = (int) (currentProgress * 100.0 / totalProgress + 0.5);
                if (progressCallback != null) {
                    progressCallback.onProgress(progress);
                }

                notificationBuilder.setContentIntent(createPauseIntent());

                notification = NotificationUtil.downloadingNotification(
                    notificationBuilder,
                    (int) currentProgress, (int) totalProgress, icon,
                    appName,
                    EasyUpdate.context().getString(R.string.download_ongoing, progress + "%"),
                    false);

                NotificationUtil.show(notification);
            }


            @Override public void downloadFailure(String errorMsg) {
                isDownloading = false;

                Notification notification = NotificationUtil.downloadFailureNotification(
                    DownloadService.this,
                    createRetryIntent(),
                    icon,
                    appName,
                    $.getString(R.string.download_failure), true);

                NotificationUtil.show(notification);
                $.toast(errorMsg);
                stopSelf();
            }
        });
    }


    /**
     * 暂停下载
     */
    void pause() {
        if (isDownloading) {
            isDownloading = false;
            HttpRequest.interruptDownload();
            notificationBuilder.setContentIntent(createStartIntent());
            notification = NotificationUtil.easyNotification(
                notificationBuilder, icon,
                appName, $.getString(R.string.download_pause), true);
            NotificationUtil.show(notification);
        }
    }


    /**
     * 取消下载
     */
    void cancel() {
        isDownloading = false;
        HttpRequest.cancelDownload();
        DownloadService.this.stopSelf();
        stopForeground(true);
    }


    /**
     * 下载进度监听
     */
    public void setOnProgressCallback(OnProgressCallback callback) {
        this.progressCallback = callback;
    }


    private PendingIntent createRetryIntent() {
        Intent intent = DownloadReceiver.newIntent(downUrl, savePath,
            DownloadReceiver.EXTRA_FLAG_START);
        return PendingIntent.getBroadcast(DownloadService.this, 0,
            intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private PendingIntent createPauseIntent() {
        Intent intent = DownloadReceiver.newIntent(downUrl, savePath,
            DownloadReceiver.EXTRA_FLAG_PAUSE);
        return PendingIntent.getBroadcast(DownloadService.this, 0,
            intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private PendingIntent createStartIntent() {
        Intent intent = DownloadReceiver.newIntent(downUrl, savePath,
            DownloadReceiver.EXTRA_FLAG_START);
        return PendingIntent.getBroadcast(DownloadService.this, 0,
            intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private String getFileName(String path) {
        int index = path.lastIndexOf("/");
        return path.substring(index + 1);
    }


    private void patchUpdate(File file) {
        if ($.isSDCardAvailable()) {
            final File newApk = new File($.getSDCardPath(), "new.apk");
            BsPatch.patch($.source(),
                newApk.getAbsolutePath(),
                file.getAbsolutePath());
            if (newApk.exists()) {
                $.install(newApk);
            } else {
                $.toast(R.string.update_error);
            }

        } else {
            $.toast("SDCard is not available!");
        }
    }


    private boolean isPatch(File file) {
        return file.getAbsolutePath().toLowerCase().endsWith(".patch");
    }
}
