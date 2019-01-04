package com.android.downloadlib.processor.task;

import android.util.Log;

import com.android.downloadlib.NetErrorStatus;
import com.android.downloadlib.entrance.ZDloader;
import com.android.downloadlib.processor.db.ZDBManager;
import com.android.downloadlib.processor.entiry.ZDownloadBean;
import com.android.downloadlib.processor.entiry.ZThreadBean;
import com.android.downloadlib.processor.entiry.ZloadInfo;
import com.android.downloadlib.processor.server.ZHttpCreate;
import com.android.downloadlib.utils.ZRxTimeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class ZDownloadTask {
    private static final String TAG = "ZDownloadTask";
    private long mFileDownloadSize = 0; //方便显示多线程的进度
    private volatile boolean isPause = false;
    //private List<DownloadThread> mDownloadTasks = new ArrayList<>();//方便检测多线程是否下载完成
    private ExecutorService mExecutorService ;
    private volatile boolean isDelete = false;
    private ZloadInfo mZloadInfo;

    private List<DownloadThread> mDownloadTasks = new ArrayList<>();//方便检测多线程是否下载完成
    private DownloadListener mListener;
    public ZDownloadTask(ZloadInfo info,DownloadListener listener){
        mZloadInfo = info;
        mListener = listener;
        mExecutorService = Executors.newFixedThreadPool(info.threadCount);
        long blocksize = info.fileLength / info.threadCount;

        List<ZThreadBean> threadBeans = ZDBManager.getInstance().getAllInfo();
        if (threadBeans != null && threadBeans.size() > 0){
            mFileDownloadSize = 0;
            for (int i = 0; i < threadBeans.size(); i++) {
                long end = (i + 1) * blocksize - 1;

                if (i == info.threadCount - 1) { //最后一个除不尽，用文件长度代替
                    end = info.fileLength;
                }
                ZThreadBean bean = threadBeans.get(i);
                //Log.d(TAG, "zsr --> ZDownloadTask: "+info.fileLength + bean.toString());
                bean.startPos += bean.threadLength;
                bean.endPos = end;
                mFileDownloadSize += bean.threadLength;
                DownloadThread downloadthread = new DownloadThread(bean,info);
                mExecutorService.execute(downloadthread);
                mDownloadTasks.add(downloadthread);
            }
        }else {
            for (int i = 0; i < info.threadCount; i++) {
                long start = i * blocksize;
                long end = (i + 1) * blocksize - 1;

                if (i == info.threadCount - 1) { //最后一个除不尽，用文件长度代替
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
                mExecutorService.execute(downloadThread);
                mDownloadTasks.add(downloadThread);
            }
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
        for (DownloadThread downloadThread : mDownloadTasks) {
            downloadThread.isSave = true;
        }
    }

    public void delete() {
        isDelete = true;
        isPause = true;
        mFileDownloadSize = 0;
        ZRxTimeUtils.timer(400, new ZRxTimeUtils.onRxTimeListener() {
            @Override
            public void onNext() {
                mFileDownloadSize = 0;
                isDelete = false;
                ZDBManager.getInstance().deleteAll();
                ZDownloadManager.getInstance().sendDownloadStatus(ZDloader.STOPSELF);
                File file = new File(mZloadInfo.filePath,mZloadInfo.fileName);
                if (file.exists()){
                    file.delete();
                }

            }
        });
        
        
    }

    /**
     * 实际下载类
     */
    class DownloadThread extends Thread{

        boolean isTheadFinished = false;
        boolean isSave = false;
        ZThreadBean bean;
        ZloadInfo info;
        public DownloadThread(ZThreadBean bean, ZloadInfo info){
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
                    is = response.body().byteStream();
                    //设置本地的存储
                    File file = new File(info.filePath,info.fileName);
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(bean.startPos);
                    byte[] bytes = new byte[4 * 1024];
                    int len;
                    while ((len = is.read(bytes)) != -1) {
                        raf.write(bytes, 0, len);
                        mFileDownloadSize += len;

                        if (isDelete){
                            return;
                        }
                        int progress = (int) (mFileDownloadSize * 100.0f / info.fileLength);
                        ZDownloadBean downloadBean = new ZDownloadBean();
                        downloadBean.progress = progress;
                        downloadBean.downloadSize = mFileDownloadSize;
                        downloadBean.totalSize = info.fileLength;
                        mListener.progress(downloadBean);
                        //记录每个线程的结束点的值
                        bean.threadLength += len;
                        if (isPause){
                            //保存到数据库
                          //  Log.d(TAG, "zsr --> 保存啦: "+bean.toString());
                            ZDBManager.getInstance().saveOrUpdate(bean);
                            return;
                        }
                        if (isSave){
                          //  Log.d(TAG, "zsr --> 我保存啦: "+bean.toString());
                            ZDBManager.getInstance().saveOrUpdate(bean);
                            isSave = false;
                        }



                    }
                    isTheadFinished = true;
                }else{
                    mFileDownloadSize = 0;
                    mListener.error(NetErrorStatus.RESPONSE_IS_NULL,"response.body() == null");
                    delete();
                }

                checkFinish(info);

            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                String errorMsg = e.toString();
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


    private synchronized void checkFinish(ZloadInfo info){
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
            isPause = true;
            File file = new File(info.filePath,info.fileName);
            if (file.exists()){
                Log.d(TAG, "zsr --> 本地文件大小: "+file.length()+" 服务器文件大小: "+info.fileLength);
                if (file.length() == info.fileLength){
                    mListener.success(info.filePath+File.separator+info.fileName);
                    //删除线程
                    ZDBManager.getInstance().deleteAll();

                }else{
                    mListener.error(NetErrorStatus.FILE_LENGTH_NOT_SAME,"file length not same");
                    delete();
                }
            }
        }
    }


    /**
     * 监听 listener
     */
    interface DownloadListener {
        void success(String path);
        void progress(ZDownloadBean bean);
        void error(NetErrorStatus netErrorStatus, String fail);

    }

}
