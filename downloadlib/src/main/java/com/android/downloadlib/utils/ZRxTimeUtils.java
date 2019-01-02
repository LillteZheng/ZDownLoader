package com.android.downloadlib.utils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by zhengshaorui
 * Time on 2018/11/24
 */

public class ZRxTimeUtils {
    private static Disposable mDisposable;

    public interface onRxTimeListener{
        void onNext();
    }

    public static void timer(int time, final onRxTimeListener listener){
        Observable.timer(time, TimeUnit.MILLISECONDS)
                .compose(ZRxUtils.<Long>rxScheduers())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (listener != null){
                            listener.onNext();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        onStop();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public static void onStop(){
        if (mDisposable != null){
            mDisposable.dispose();
        }
    }

    public static void interval(int time, final onRxTimeListener listener){
        Observable.interval(time,TimeUnit.MILLISECONDS)
                .compose(ZRxUtils.<Long>rxScheduers())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        mDisposable = disposable;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (listener != null){
                            listener.onNext();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        onStop();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
