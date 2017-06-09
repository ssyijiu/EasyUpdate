package com.ssyijiu.demo;

import android.app.Application;
import com.ssyijiu.easyupdate.EasyUpdate;

/**
 * Created by ssyijiu on 2017/6/7.
 * Github: ssyijiu
 * E-mail: lxmyijiu@163.com
 */

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        EasyUpdate.init(this, R.mipmap.ic_beach);
    }
}
