package com.android.downloadlib.entrance;

import android.content.Context;

import com.android.downloadlib.processor.callback.ZupdateListener;
import com.android.downloadlib.processor.db.ZDBManager;
import com.android.downloadlib.processor.task.ZDownloadManager;

import org.litepal.LitePal;


/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class  ZDloader {
    public static final String KEY_STATUS = "KEY_STATUS";
    public static final String START = "START";
    public static final String PAUSE = "PAUSE";
    public static final String RESTART = "RESTART";
    private static final String TAG = "ZDloader";
    private static RequestManager mRequestManager;
    public static RequestManager with(Context context){
        mRequestManager = new RequestManager().with(context);
        //初始化数据库
        ZDBManager.getInstance()
                .config(context.getApplicationContext())
                .useLitePal(true);
        return new RequestManager().with(context);
    }
    


    public static void pauseDownload(){
       ZDownloadManager.getInstance().pauseDownload();
    }


    public static void reStartDownload(){
        ZDownloadManager.getInstance().reStartDownload();
    }

    public static void startDownload(){
        ZDownloadManager.getInstance().startDownload();
    }


    public static void deleteDownload(boolean deleteAll){
        ZDownloadManager.getInstance().deleteDownload(deleteAll);
    }
    public static boolean isDownloading(){
       return ZDownloadManager.getInstance().isDownloading();
    }

    public static void updateListener(ZupdateListener listener){
        ZDownloadManager.getInstance().updateListener(listener);
    }

}
