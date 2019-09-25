package com.android.downloadlib.processor.entiry;

import android.content.Context;

import com.android.downloadlib.processor.callback.ZJsonListener;
import com.android.downloadlib.processor.callback.ZupdateListener;

import java.util.Map;


/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class ZLoadInfo {
    public Context context;
    public String url;
    public String jsonUrl;
    public int threadCount;
    public String filePath;
    public String fileName;
    public long reFreshTime = 100;
    public long fileLength = -1;
    public Map<String,String> paramsMap;
    public boolean allowBackDownload = false;
    public ZupdateListener listener = null;
    public ZJsonListener jsonListener = null;


    @Override
    public String toString() {
        return "ZLoadInfo{" +
                "context=" + context +
                ", url='" + url + '\'' +
                ", jsonUrl='" + jsonUrl + '\'' +
                ", threadCount=" + threadCount +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", reFreshTime=" + reFreshTime +
                ", fileLength=" + fileLength +
                ", allowBackDownload=" + allowBackDownload +
                ", listener=" + listener +
                ", jsonListener=" + jsonListener +
                '}';
    }
}
