package com.android.downloadlib.processor.callback;


import com.android.downloadlib.NetErrorStatus;
import com.android.downloadlib.processor.entiry.ZDownloadBean;

/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public interface ZupdateListener {
    void onSuccess(String path,String md5Msg);
    void onError(NetErrorStatus errorStatus, String errorMsg);
    void onDownloading(ZDownloadBean bean);
}
