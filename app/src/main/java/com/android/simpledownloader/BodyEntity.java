package com.android.simpledownloader;

/**
 * @author by  zhengshaorui on 2019/9/25
 * Describe:
 */
public class BodyEntity {
    public String version;

    public String incremental;

    public String url1;

    public String url1_md5;

    public String url2;

    public String url2_md5;

    @Override
    public String toString() {
        return "BodyEntity{" +
                "version='" + version + '\'' +
                ", incremental='" + incremental + '\'' +
                ", url1='" + url1 + '\'' +
                ", url1_md5='" + url1_md5 + '\'' +
                ", url2='" + url2 + '\'' +
                ", url2_md5='" + url2_md5 + '\'' +
                '}';
    }
}
