package com.android.downloadlib.processor.callback;

import java.io.IOException;

import retrofit2.Response;


/**
 * Created by zhengsr on 2019/6/22.
 */

public abstract class BaseListener<T> {
    public abstract T transCallback(Response response) ;
    public void fail(String errorMsg){

    };
    public void response(T data){};

}
