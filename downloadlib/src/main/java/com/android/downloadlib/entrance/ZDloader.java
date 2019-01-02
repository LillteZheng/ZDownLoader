package com.android.downloadlib.entrance;

import android.content.Context;

import com.android.downloadlib.processor.db.ZDBManager;
import com.android.downloadlib.processor.task.ZDownloadManager;


/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class  ZDloader {
    public static final String KEY_STATUS = "KEY_STATUS";
    public static final String START = "START";
    public static final String PAUSE = "PAUSE";
    public static final String RESTART = "RESTART";
    public static final String STOPSELF = "STOPSELF";
    private static final String TAG = "ZDloader";
    private static RequestManager mRequestManager;
    public static RequestManager with(Context context){
        mRequestManager = new RequestManager().with(context);
        ZDBManager.getInstance().config(context);
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

    public static void deleteDownload(){
        ZDownloadManager.getInstance().deleteDownload();
    }
    public static boolean isDownloading(){
       return ZDownloadManager.getInstance().isDownloading();
    }



}
