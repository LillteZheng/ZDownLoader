package com.android.downloadlib.processor.callback;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import retrofit2.Response;


/**
 * Created by zhengsr on 2019/6/22.
 */

public abstract class ZJsonListener<T> extends BaseListener<T>{
    private Class<T> mclazz;

    public ZJsonListener(Class<T> mclazz) {
        this.mclazz = mclazz;
    }


    @Override
    public T transCallback(Response response) {
        T t = JSON.parseObject((String) response.body(), mclazz);
        return t;
    }

}
