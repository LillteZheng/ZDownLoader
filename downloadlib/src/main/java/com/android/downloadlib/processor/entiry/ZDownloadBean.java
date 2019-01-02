package com.android.downloadlib.processor.entiry;

/**
 * Created by zhengshaorui
 * Time on 2018/12/7
 */

public class ZDownloadBean {
    public int progress;
    public long totalSize;
    public long downloadSize;
    public String speed;

    @Override
    public String toString() {
        return "ZDownloadBean{" +
                "progress=" + progress +
                ", totalSize=" + totalSize +
                ", downloadSize=" + downloadSize +
                ", speed='" + speed + '\'' +
                '}';
    }
}
