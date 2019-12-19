package com.android.downloadlib.processor.task;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;

import com.android.ZLoadService;
import com.android.downloadlib.NetErrorStatus;
import com.android.downloadlib.entrance.ZDloader;
import com.android.downloadlib.processor.callback.ZupdateListener;
import com.android.downloadlib.processor.entiry.ZDownloadBean;
import com.android.downloadlib.processor.entiry.ZLoadInfo;
import com.android.downloadlib.processor.server.ZHttpCreate;
import com.android.downloadlib.processor.server.ZHttpServer;
import com.android.downloadlib.utils.ZRxUtils;
import com.android.downloadlib.utils.ZStorageUtils;

import io.reactivex.observers.ResourceObserver;
import okhttp3.ResponseBody;


/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class ZDownloadManager  {
    private static final String TAG = "ZDownloadManager";
    private ZHttpServer mServer;
    private long mLastTime;
    private long mLastSize;
    private boolean isRefresh = false;
    private ZDownloadTask mDownloadTask;
    private ZLoadInfo mZloadInfo;
    static Handler sHandler = new Handler(Looper.getMainLooper());



    private static class Holder {
        static final ZDownloadManager INSTANCE = new ZDownloadManager();
    }

    public static ZDownloadManager getInstance() {
        return Holder.INSTANCE;
    }

    private ZDownloadManager() {
        mServer = ZHttpCreate.getService();
    }

    public  void updateListener(ZupdateListener listener){
        if (mZloadInfo == null){
            listener.onError(NetErrorStatus.TASK_DELETE_ABNORMAL,"task has been delete abnormal");
        }else {
            mZloadInfo.listener = listener;
        }
    }
    /**
     * 检查是否可以更新
     *
     * @param info
     */
    public void checkAndDownload(ZLoadInfo info) {
        mZloadInfo = info;
        long deviceSize = ZStorageUtils.getAvailDiskSize(mZloadInfo.filePath);
        if (mZloadInfo.fileLength != -1) {
            if (mZloadInfo.fileLength > deviceSize) {
                mZloadInfo.listener.onError(NetErrorStatus.CACHE_NOT_ENOUGH, "Cache not enough");
            }else {
                sendDownloadStatus(ZDloader.START);
            }
        } else {
            getFileLengthAndStart();
        }
    }

    /**
     * 拿到文件长度和开始下载
     */
    @SuppressLint("CheckResult")
    public void getFileLengthAndStart(){
        mServer.getFileLength(mZloadInfo.url)
                .compose(ZRxUtils.<ResponseBody>rxScheduers())
                .subscribeWith(new ResourceObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        long fileLength = responseBody.contentLength();
                      //  Log.d(TAG, "zsr 文件长度2 "+fileLength);
                        if (fileLength > ZStorageUtils.getAvailDiskSize(mZloadInfo.filePath)) {
                            mZloadInfo.listener.onError(NetErrorStatus.CACHE_NOT_ENOUGH, "Cache not enough");
                        } else {
                            mZloadInfo.fileLength = fileLength;
                            if (mZloadInfo.fileLength != -1) {
                                sendDownloadStatus(ZDloader.START);
                            }else{
                                mZloadInfo.listener.onError(NetErrorStatus.GET_LENGTH_FAIL,"content length -1");
                            }

                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mZloadInfo.listener != null) {
                            mZloadInfo.listener.onError(NetErrorStatus.FAIL_TO_CONNECT, throwable.toString());
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void startDownload() {
        mDownloadTask = new ZDownloadTask(mZloadInfo, new ZDownloadTask.DownloadListener() {
            @Override
            public void success(final String path, final String mdMsg) {
                //放到UI线程里
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mZloadInfo.listener.onSuccess(path,mdMsg);
                        stopService();
                    }
                });
            }

            @Override
            public void progress(final ZDownloadBean bean) {
                final long currentTime = System.currentTimeMillis();
                if (currentTime - mLastTime >= mZloadInfo.reFreshTime) {
                    isRefresh = true;
                    //放到UI线程里
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isRefresh){
                                mLastTime = currentTime;
                                isRefresh = false;
                                long size = bean.downloadSize - mLastSize;
                                bean.speed = Formatter.formatFileSize(mZloadInfo.context,size)+"/s";
                                mZloadInfo.listener.onDownloading(bean);
                                mLastSize = bean.downloadSize;
                            }
                        }
                    });

                }
            }


            @Override
            public void error(final NetErrorStatus netErrorStatus, final String fail) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mZloadInfo.listener.onError(netErrorStatus, fail);
                    }
                });

            }
        });

    }

    public void pauseDownload() {
        if (mDownloadTask != null) {
            mDownloadTask.pause();
        }
    }

    public void reStartDownload() {
        if (mDownloadTask != null) {
            mDownloadTask.reStart();
            if (mZloadInfo != null) {
                startDownload();
            }
        }
    }

    public boolean isDownloading() {
        if (mDownloadTask != null) {
            return mDownloadTask.isRunning();
        }

        return false;
    }

    public void deleteDownload(boolean deleteAll) {
        if (mDownloadTask != null) {
             mDownloadTask.delete(deleteAll);
        }
    }

    /**
     * 发送不同的数据去服务实现不同的东西
     * @param status
     */
    public void sendDownloadStatus(String status){
        Intent intent = new Intent(mZloadInfo.context, ZLoadService.class);
        intent.putExtra(ZDloader.KEY_STATUS, status);
        mZloadInfo.context.startService(intent);
    }

    public void stopService() {
        Intent intent = new Intent(mZloadInfo.context, ZLoadService.class);
        mZloadInfo.context.stopService(intent);
    }
}
