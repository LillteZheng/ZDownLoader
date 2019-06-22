package com.android.downloadlib.entrance;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.downloadlib.processor.entiry.ZloadInfo;

import java.io.File;

/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class CheckParams {
    private static final String TAG = "CheckParams";
    public CheckParams() {
    }

    public ZloadInfo check(ZloadInfo info){
        //url肯定是必须的
        if (TextUtils.isEmpty(info.url)){
            throw new RuntimeException("url can not be null");
        }

        //如果没写路径，则以默认路径 来
        if (TextUtils.isEmpty(info.filePath)){
            boolean isLake = lacksPermission(info.context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.d(TAG, "zsr --> check: "+isLake);
            if (Build.VERSION.SDK_INT >= 23 && isLake){
                throw new RuntimeException("you need reuqest WRITE_EXTERNAL_STORAGE");
            }else {
                info.filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "ZDloader";
                File file = new File(info.filePath);
                if (!file.exists()) {
                    file.mkdir();
                }
            }
        }
        //如果没写文件名，则以url的文件名来识别
        if (TextUtils.isEmpty(info.fileName)){
            info.fileName = info.url.substring(info.url.lastIndexOf("/")+1);
        }
        //默认刷新时间1s
        if (info.reFreshTime < 1000){
            info.reFreshTime = 1000;
        }
        if (info.listener == null){
            throw new RuntimeException("you need register listener to get network status");
        }
        return info;
    }

    public ZloadInfo checkJsonUrl(ZloadInfo info){
        //url肯定是必须的
        if (TextUtils.isEmpty(info.jsonUrl)){
            throw new RuntimeException("jsonUrl can not be null");
        }
        if (info.jsonListener == null){
            throw new RuntimeException("you need register jsonListener to get network status");
        }

        return info;
    }


    /**
     * 判断有该权限，true表示没有
     * @param mContexts
     * @param permission
     * @return
     */
    private  boolean lacksPermission(Context mContexts, String permission) {
        return ContextCompat.checkSelfPermission(mContexts, permission) !=
                PackageManager.PERMISSION_GRANTED;

    }
}
