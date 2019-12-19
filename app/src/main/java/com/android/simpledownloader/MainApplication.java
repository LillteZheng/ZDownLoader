package com.android.simpledownloader;

import android.app.Application;

import org.litepal.LitePal;

/**
 * @author by  zhengshaorui on 2019/10/8
 * Describe:
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }
}
