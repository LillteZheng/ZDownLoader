package com.android.simpledownloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.downloadlib.NetErrorStatus;
import com.android.downloadlib.entrance.ZDloader;
import com.android.downloadlib.processor.callback.ZJsonListener;
import com.android.downloadlib.processor.callback.ZupdateListener;
import com.android.downloadlib.processor.entiry.ZDownloadBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements ZupdateListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String URL = "https://img-blog.csdnimg.cn/20190104091200408.gif";
    private static final String JSONURL = "http://192.168.0.1:3389/OTA";
    private TextView mTextView;
    private Button mDownloadBtn;
    private TextView mDownloadTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.download_tv);
        mDownloadBtn = findViewById(R.id.download_btn);
        mDownloadBtn.setOnClickListener(this);
        mDownloadTv = findViewById(R.id.download_info);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            download();
        }

        /**
         * 解析json
         */

        Map<String,String> params = new HashMap<>();
        params.put("useId", "7a57a5a743894a0e");
        params.put("sys_version", "1.1.1.10");
        params.put("sys_type", "65");
        params.put("sys_cloudcode", "1111111111111111111");
        params.put("ext", "0");

        ZDloader.with(this)
                .jsonUrl(JSONURL)
                .paramsMap(params)
                .jsonListener(new ZJsonListener<Root>(Root.class) {
                    @Override
                    public void fail(String errorMsg) {
                        super.fail(errorMsg);
                        Log.d(TAG, "zsr fail: "+errorMsg);
                    }

                    @Override
                    public void response(Root data) {
                        super.response(data);
                        Log.d(TAG, "zsr response: "+data.toString());
                    }
                }).parseJson();


    }

    private void download(){
        //如果不是正在下载，则让它继续下载即可
        if (!ZDloader.isDownloading()) {
            ZDloader.with(MainActivity.this)
                    .url(URL)
                    .threadCount(3)
                    .reFreshTime(1000)
                    .allowBackDownload(true)
                    .listener(MainActivity.this)
                    .download();
        }else {
            //否则，则更新接口，让它可以继续显示UI
            ZDloader.updateListener(MainActivity.this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                Log.d(TAG, "onRequestPermissionsResult: "+permissions.length+" "+
                        permissions[0]);
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    download();
                    Log.d(TAG, "zsr --> onRequestPermissionsResult: ");
                }else{
                    Toast.makeText(this, "权限申请被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSuccess(String path,String mdMsg) {
        Log.d(TAG, "zsr --> onSuccess: "+path);
        mTextView.setText("下载完成啦,路径: "+path+" "+mdMsg);
        mDownloadTv.setText("");
    }

    @Override
    public void onError(NetErrorStatus errorStatus, String errorMsg) {
        Log.d(TAG, "zsr --> onError: "+errorMsg);
        mTextView.setText(errorMsg);
    }

    @Override
    public void onDownloading(ZDownloadBean bean) {
        mTextView.setText(getString(R.string.download_progress,bean.progress+""));
        String nowSize = Formatter.formatFileSize(MainActivity.this,bean.downloadSize);
        String totalSize = Formatter.formatFileSize(MainActivity.this,bean.totalSize);
        mDownloadTv.setText("下载速度: "+bean.speed+"\t\t"+nowSize+" / "+totalSize);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "zsr onClick: .d"+ZDloader.isDownloading());
        if (ZDloader.isDownloading()){
            ZDloader.pauseDownload();
            mDownloadBtn.setText("已暂停");
        }else{
            ZDloader.reStartDownload();
            mDownloadBtn.setText("正在下载");
        }
    }
    public void deleteClick(View view) {
       ZDloader.deleteDownload(true);

       mTextView.setText("已删除");

    }


}
