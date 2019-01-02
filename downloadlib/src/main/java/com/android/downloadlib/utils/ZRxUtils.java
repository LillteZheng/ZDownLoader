package com.android.downloadlib.utils;


import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhengshaorui
 * Time on 2018/10/22
 */

public class ZRxUtils {
    /**
     * 封装线程调度
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T,T> rxScheduers(){
        return new ObservableTransformer<T,T>(){
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
