package com.android.downloadlib.entrance;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.android.ZLoadService;
import com.android.downloadlib.processor.callback.ZJsonListener;
import com.android.downloadlib.processor.callback.ZupdateListener;
import com.android.downloadlib.processor.entiry.ZLoadInfo;
import com.android.downloadlib.processor.task.ZDownloadManager;
import com.android.downloadlib.processor.task.ZJsonTask;
import com.android.downloadlib.widght.InvisiabelFragment;


/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class RequestManager {
    private static final String TAG = "RequestManager";
    private ZLoadInfo mInfo;

    public RequestManager() {
        mInfo = new ZLoadInfo();
    }

    public RequestManager with(Context context) {
        mInfo.context = context;
        return this;
    }

    public RequestManager url(String url) {
        mInfo.url = url;
        return this;
    }
    public RequestManager savePath(String savePath) {
        mInfo.filePath = savePath;
        return this;
    }
    public RequestManager fileName(String fileName) {
        mInfo.fileName = fileName;
        return this;
    }

    public RequestManager threadCount(int  threadCount) {
        mInfo.threadCount = threadCount;
        return this;
    }

    public RequestManager fileLength(long fileLength) {
        mInfo.fileLength = fileLength;
        return this;
    }





    public RequestManager reFreshTime(long reFreshTime) {
        mInfo.reFreshTime = reFreshTime;
        return this;
    }

    public RequestManager allowBackDownload(boolean allowBackDownload) {
        mInfo.allowBackDownload = allowBackDownload;
        return this;
    }


    public RequestManager listener(ZupdateListener listener) {
        mInfo.listener = listener;
        return this;
    }


    public void download(){
        mInfo = new CheckParams().check(mInfo);
        rigister(mInfo);
        Intent intent = new Intent(mInfo.context, ZLoadService.class);
        mInfo.context.stopService(intent);
        ZDownloadManager.getInstance().checkAndDownload(mInfo);
    }

    public RequestManager jsonUrl(String jsonurl){
        mInfo.jsonUrl = jsonurl;
        return this;
    }


    public <T> RequestManager jsonListener(ZJsonListener listener){
        mInfo.jsonListener = listener;
        return this;
    }
    public void parseJson(){
        mInfo = new CheckParams().checkJsonUrl(mInfo);
        //开始校验json
        new ZJsonTask(mInfo);
    }


    public ZLoadInfo getInfo(){
        return mInfo;
    }


    private void rigister(final ZLoadInfo info){
        if (info.context instanceof FragmentActivity){
           // Log.d(TAG, "zsr --> rigister: "+info.context);
            FragmentActivity activity = (FragmentActivity) info.context;
            if (activity.isDestroyed()){
                throw new IllegalArgumentException("You cannot start a load task for a destroyed activity");
            }
            //添加一个隐形的 fragment ，用来管理生命周期
            Fragment lifeFramgnet = activity.getSupportFragmentManager().findFragmentByTag(info.url);
            InvisiabelFragment fragment ;
            if (lifeFramgnet != null){
                fragment = (InvisiabelFragment) lifeFramgnet;
            }else{
                fragment = InvisiabelFragment.newInstance();
            }
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()){
                ft.add(fragment,info.url);
                ft.commit();
            }
            
            fragment.setLifecyleListener(new InvisiabelFragment.LifecyleListener() {
                @Override
                public void onResume() {
                    //Log.d(TAG, "zsr --> onResume: ");
                }

                @Override
                public void onStop() {
                    if (!info.allowBackDownload){
                        ZDownloadManager.getInstance().deleteDownload(true);
                    }
                }

                @Override
                public void onDestroy() {
                   // Log.d(TAG, "zsr --> onDestroy: ");
                }
            });

        }
    }

}
