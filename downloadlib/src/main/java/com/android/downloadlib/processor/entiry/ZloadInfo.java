package com.android.downloadlib.processor.entiry;

import android.content.Context;

import com.android.downloadlib.processor.callback.ZupdateListener;


/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class ZloadInfo {
    public Context context;
    public String url;
    public int threadCount;
    public String filePath;
    public String fileName;
    public long reFreshTime = 1000;
    public long fileLength = -1;
    public boolean allowBackDownload = false;
    public ZupdateListener listener = null;


    @Override
    public String toString() {
        return "ZloadInfo{" +
                "context=" + context +
                ", url='" + url + '\'' +
                ", threadCount=" + threadCount +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", reFreshTime=" + reFreshTime +
                ", fileLength=" + fileLength +
                ", allowBackDownload=" + allowBackDownload +
                ", listener=" + listener +
                '}';
    }
}
