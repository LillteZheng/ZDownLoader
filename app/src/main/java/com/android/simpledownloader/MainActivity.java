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
import com.android.downloadlib.processor.callback.ZupdateListener;
import com.android.downloadlib.processor.entiry.ZDownloadBean;

public class MainActivity extends AppCompatActivity implements ZupdateListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String URL = "http://172.16.29.103:8989/icon/test.rar";
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

    }

    private void download(){
        ZDloader.with(MainActivity.this)
                .url(URL)
                .threadCount(3)
                .reFreshTime(1000)
                .allowBackDownload(true)
                .listener(MainActivity.this)
                .download();
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
                }else{
                    Toast.makeText(this, "权限申请被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onSuccess(String path) {
        Log.d(TAG, "zsr --> onSuccess: "+path);
        mTextView.setText("下载完成啦,路径: "+path);
        mDownloadTv.setText("");
    }

    @Override
    public void onError(NetErrorStatus errorStatus, String errorMsg) {
        Log.d(TAG, "zsr --> onError: "+errorMsg);
    }

    @Override
    public void onDownloading(ZDownloadBean bean) {
        mTextView.setText(getString(R.string.download_progress,bean.progress+""));
        String nowSize = Formatter.formatFileSize(MainActivity.this,bean.downloadSize);
        String totalSize = Formatter.formatFileSize(MainActivity.this,bean.totalSize);
        mDownloadTv.setText("下载速度: "+bean.speed+"                       "+nowSize+" / "+totalSize);
    }

    @Override
    public void onClick(View v) {
        if (ZDloader.isDownloading()){
            ZDloader.pauseDownload();
            mDownloadBtn.setText("已暂停");
        }else{
            ZDloader.reStartDownload();
            mDownloadBtn.setText("正在下载");
        }
    }
    public void deleteClick(View view) {
        mTextView.setText("已删除");
        ZDloader.deleteDownload();
    }
}
