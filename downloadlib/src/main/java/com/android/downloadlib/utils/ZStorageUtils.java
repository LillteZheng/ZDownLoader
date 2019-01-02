package com.android.downloadlib.utils;

import android.os.StatFs;

/**
 * Created by zhengshaorui
 * Time on 2018/12/6
 */

public class ZStorageUtils {
    /**
     * 获取已经存储的大小
     * @return
     */
    public static long getAvailDiskSize(String path){
        StatFs sf = new StatFs(path);
        long blockSize = sf.getBlockSizeLong();
        long availCount = sf.getAvailableBlocksLong();

        return blockSize * availCount;
    }
}
