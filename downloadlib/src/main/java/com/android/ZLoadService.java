package com.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.downloadlib.entrance.ZDloader;
import com.android.downloadlib.processor.db.ZDBManager;

/**
 * Created by zhengshaorui
 * Time on 2018/12/28
 */

public class ZLoadService extends Service {
    private static final String TAG = "ZLoadService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZDloader.startDownload();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            String status = intent.getStringExtra(ZDloader.KEY_STATUS);
            Log.d(TAG, "zsr --> onStartCommand: "+status);
            switch(status){
                case ZDloader.PAUSE:
                    ZDloader.pauseDownload();
                    break;
                case ZDloader.RESTART:
                    ZDloader.reStartDownload();
                    break;
                case ZDloader.STOPSELF :
                    stopSelf();
                    break;
                default :
                    break;

            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
