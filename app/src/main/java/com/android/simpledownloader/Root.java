package com.android.simpledownloader;

import java.util.List;

/**
 * @author by  zhengshaorui on 2019/9/25
 * Describe:
 */
public class Root {
    public String latest;

    public String url_head;

    public List<BodyEntity> array ;


    @Override
    public String toString() {
        return "Root{" +
                "latest='" + latest + '\'' +
                ", url_head='" + url_head + '\'' +
                ", array=" + array +
                '}';
    }
}
