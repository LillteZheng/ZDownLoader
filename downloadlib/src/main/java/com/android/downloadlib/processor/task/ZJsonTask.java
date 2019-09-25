package com.android.downloadlib.processor.task;

import com.android.downloadlib.processor.entiry.ZLoadInfo;
import com.android.downloadlib.processor.server.ZHttpCreate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zhengsr on 2019/6/22.
 */

public class ZJsonTask {
    private static final String TAG = "ZJsonTask";

    public ZJsonTask(final ZLoadInfo info) {
        Call<String> call;
        if (info.paramsMap.size() > 0) {
            call = ZHttpCreate.getService().getJson(info.jsonUrl,info.paramsMap);
        }else{
            call = ZHttpCreate.getService().getJson(info.jsonUrl);

        }
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Object data = info.jsonListener.transCallback(response);
                info.jsonListener.response(data);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                info.jsonListener.fail(t.toString());
            }
        });

    }


}
