package com.ssyijiu.easyupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ssyijiu on 2017/6/7.
 * Github: ssyijiu
 * E-mail: lxmyijiu@163.com
 */

public class DownloadReceiver extends BroadcastReceiver {

    private final static String EXTRA_DOWNURL = "extra_downurl";
    private final static String EXTRA_SAVEPATH = "extra_savepath";
    private final static String EXTRA_FLAG = "extra_flag";

    // 开始
    final static int EXTRA_FLAG_START = 0;
    // 暂停
    final static int EXTRA_FLAG_PAUSE = 1;


    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int flag = intent.getIntExtra(EXTRA_FLAG, 0);
            if (flag == EXTRA_FLAG_START) {
                String downUrl = intent.getStringExtra(EXTRA_DOWNURL);
                String savePath = intent.getStringExtra(EXTRA_SAVEPATH);
                EasyUpdate.instance().start(downUrl, savePath);
            } else if (flag == EXTRA_FLAG_PAUSE) {
                EasyUpdate.instance().pause();
            }

        }
    }


    static Intent newIntent(final String downUrl, final String savePath, final int flag) {
        Intent intent = new Intent(EasyUpdate.context(), DownloadReceiver.class);
        intent.putExtra(EXTRA_DOWNURL, downUrl);
        intent.putExtra(EXTRA_SAVEPATH, savePath);
        intent.putExtra(EXTRA_FLAG, flag);
        return intent;
    }
}
