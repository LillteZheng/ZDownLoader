package com.android.downloadlib.processor.task;

import android.util.Log;
import android.webkit.DownloadListener;

import com.android.downloadlib.NetErrorStatus;
import com.android.downloadlib.processor.db.ZDBManager;
import com.android.downloadlib.processor.entiry.ZDownloadBean;
import com.android.downloadlib.processor.entiry.ZLoadInfo;
import com.android.downloadlib.processor.entiry.ZThreadBean;
import com.android.downloadlib.processor.server.ZHttpCreate;
import com.android.downloadlib.utils.MD5Utils;
import com.android.downloadlib.utils.ZRxTimeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class ZDownloadTask {
    private static final String TAG = "ZDownloadTask";
    //方便显示多线程的进度
    private volatile long mFileDownloadSize = 0;
    private ExecutorService mExecutorService ;
    private ZLoadInfo mZloadInfo;
    //方便检测多线程是否下载完成
    private volatile boolean isClose;
    private volatile boolean isPause;
    private List<DownloadThread> mDownloadTasks = new ArrayList<>();
    private DownloadListener mListener;
    public ZDownloadTask(ZLoadInfo info, DownloadListener listener){
        //初始化数据
        mZloadInfo = info;
        if (mZloadInfo != null) {
            if (mZloadInfo.fileLength == -1) {
                return;
            }
            mListener = listener;
            mExecutorService = Executors.newFixedThreadPool(info.threadCount);
            mDownloadTasks.clear();
            mFileDownloadSize = 0;
            configTask(info);
        }

    }

    /**
     * 配置初始任务
     * @param info
     */
    private void configTask(ZLoadInfo info) {
        long blocksize = info.fileLength / info.threadCount;
        List<ZThreadBean> threadBeans = ZDBManager.getInstance().getAllInfo();

        if (info.useDp) {
            //数据库中，已经保存了数据
            if (threadBeans != null && threadBeans.size() > 0) {
                for (int i = 0; i < threadBeans.size(); i++) {
                    long end = (i + 1) * blocksize - 1;
                    //最后一个除不尽，用文件长度代替
                    if (i == info.threadCount - 1) {
                        end = info.fileLength;
                    }
                    ZThreadBean bean = threadBeans.get(i);
                    bean.startPos += bean.threadLength;
                    bean.endPos = end;
                    // Log.d(TAG, "zsr 数据库存在: "+info.fileLength+" "+info.threadCount+" "+bean.startPos+" "+bean.endPos);
                    mFileDownloadSize += bean.threadLength;
                    DownloadThread downloadthread = new DownloadThread(bean, info);
                    mDownloadTasks.add(downloadthread);
                    mExecutorService.execute(downloadthread);
                }
            } else {
                //新任务，先删除数据库和本地文件
                deleteCache();
                for (int i = 0; i < info.threadCount; i++) {
                    long start = i * blocksize;
                    long end = (i + 1) * blocksize - 1;
                    //最后一个除不尽，用文件长度代替
                    if (i == info.threadCount - 1) {
                        end = info.fileLength;
                    }
                    ZThreadBean bean = new ZThreadBean();
                    bean.url = info.url;
                    bean.startPos = start;
                    bean.endPos = end;
                    bean.threadId = i;
                    bean.name = info.fileName;
                    //先保存数据库
                    ZDBManager.getInstance().saveOrUpdate(bean);
                    DownloadThread downloadThread = new DownloadThread(bean, info);
                    mDownloadTasks.add(downloadThread);
                    mExecutorService.execute(downloadThread);
                }
            }
        }else {
            //不适用数据库
            mFileDownloadSize = 0;
            for (int i = 0; i < info.threadCount; i++) {
                long start = i * blocksize;
                long end = (i + 1) * blocksize - 1;
                //最后一个除不尽，用文件长度代替
                if (i == info.threadCount - 1) {
                    end = info.fileLength;
                }
                ZThreadBean bean = new ZThreadBean();
                bean.url = info.url;
                bean.startPos = start;
                bean.endPos = end;
                bean.threadId = i;
                bean.name = info.fileName;

                DownloadThread downloadThread = new DownloadThread(bean, info);
                mDownloadTasks.add(downloadThread);
                mExecutorService.execute(downloadThread);
            }
        }
    }

    /**
     * 删除本地文件和数据库
     */
    private void deleteCache() {
        ZDBManager.getInstance().deleteAll();
        File file = new File(mZloadInfo.filePath,mZloadInfo.fileName);
        if (file.exists()){
            file.delete();
        }
    }

    public void pause(){
       isPause = true;
    }
    public void reStart(){
       isPause = false;
    }
    public boolean isRunning(){
       return !isPause;
    }


    public void saveDb(){
       /* for (DownloadThread downloadThread : mDownloadTasks) {
            downloadThread.isSave = true;
        }*/
    }
    public void  delete(final boolean deleteall) {
        isClose = true;
        isPause = true;

        final CountDownLatch downLatch = new CountDownLatch(1);

        //停止线程需要时间
        long last = System.currentTimeMillis();
        ZRxTimeUtils.timer(400, new ZRxTimeUtils.onRxTimeListener() {
            @Override
            public void onNext() {
                mFileDownloadSize = 0;
                isClose = false;
                ZDownloadManager.getInstance().stopService();
                deleteCache();
            }
        });
        try {
            downLatch.await(500,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    /**
     * 实际下载类
     */
    class DownloadThread extends Thread{

        boolean isTheadFinished = false;
      //  boolean isSave = false;
        ZThreadBean bean;
        ZLoadInfo info;
        public DownloadThread(ZThreadBean bean, ZLoadInfo info){
            this.bean = bean;
            this.info = info;
        }

        @Override
        public void run() {
            super.run();
            InputStream is = null;
            RandomAccessFile raf = null;
            try {

                Call<ResponseBody> call = ZHttpCreate.getService().download(bean.url,"bytes=" + bean.startPos + "-" + bean.endPos);
                Response<ResponseBody> response = call.execute();
                if (response != null && response.isSuccessful()){
                    ResponseBody body = response.body();
                    if (body == null){
                        mFileDownloadSize = 0;
                        mListener.error(NetErrorStatus.RESPONSE_IS_NULL,"file break");
                        //异常说明下载有问题，需要把后台 的server先停止
                        ZDownloadManager.getInstance().stopService();
                        deleteCache();
                        return;
                    }
                    is = body.byteStream();
                    //设置本地的存储
                    File file = new File(info.filePath,info.fileName);
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(bean.startPos);
                    byte[] bytes = new byte[4 * 1024];
                    int len;
                    while ((len = is.read(bytes)) != -1) {
                        if (isClose){
                            return;
                        }

                        if (isPause){
                            //保存到数据库
                            ZDBManager.getInstance().saveOrUpdate(bean);
                            return;
                        }


                        raf.write(bytes, 0, len);
                        //记录每个线程的结束点的值
                        bean.threadLength += len;
                        //这个过程我们希望是线程同步的
                        synchronized (ZDownloadTask.class) {
                            mFileDownloadSize += len;

                            float progress = mFileDownloadSize * 100.0f / info.fileLength;
                            ZDownloadBean downloadBean = new ZDownloadBean();
                            downloadBean.progress = progress;
                            downloadBean.downloadSize = mFileDownloadSize;
                            downloadBean.totalSize = info.fileLength;
                            mListener.progress(downloadBean);
                        }

                        //不自动保存
                       /* if (isSave){
                          //  Log.d(TAG, "zsr --> 我保存啦: "+bean.toString());
                            ZDBManager.getInstance().saveOrUpdate(bean);
                            isSave = false;
                        }*/
                    }
                    isTheadFinished = true;
                }else{
                    mFileDownloadSize = 0;
                    mListener.error(NetErrorStatus.RESPONSE_IS_NULL,"file break");
                    //异常说明下载有问题，需要把后台 的server先停止
                    ZDownloadManager.getInstance().stopService();
                    deleteCache();
                    return;
                }

                checkFinish(info);

            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                String errorMsg = e.toString();
                //异常之前先保存状态
                ZDBManager.getInstance().saveOrUpdate(bean);

                //异常说明下载有问题，需要把后台 的server先停止
                ZDownloadManager.getInstance().stopService();
                if (errorMsg.contains("Connection timed out")){
                    mListener.error(NetErrorStatus.TIME_OUT,e.toString());
                }else {
                    mListener.error(NetErrorStatus.OTHERS, e.toString());
                }

            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (raf != null) {
                        raf.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }


    private synchronized void checkFinish(ZLoadInfo info){
        boolean isFinish = true;
        for (DownloadThread downloadThread : mDownloadTasks) {
            if (!downloadThread.isTheadFinished){
                isFinish = false;
                break;
            }
        }
        if (isFinish){
            mFileDownloadSize = 0;
            mDownloadTasks.clear();
            File file = new File(info.filePath,info.fileName);
            if (file.exists()){
                Log.d(TAG, "zsr --> 本地文件大小: "+file.length()+" 服务器文件大小: "+info.fileLength);
                if (file.length() == info.fileLength){
                    String mdMsg = MD5Utils.getFileMD5(file);
                    mListener.success(info.filePath+File.separator+info.fileName,mdMsg);
                    //删除线程
                    ZDBManager.getInstance().deleteAll();
                    mExecutorService.shutdownNow();

                }else{
                    mListener.error(NetErrorStatus.FILE_LENGTH_NOT_SAME,"file length not same");

                }
            }
        }
    }


    /**
     * 监听 listener
     */
    interface DownloadListener {
        void success(String path,String md5Msg);
        void progress(ZDownloadBean bean);
        void error(NetErrorStatus netErrorStatus, String fail);

    }

}
