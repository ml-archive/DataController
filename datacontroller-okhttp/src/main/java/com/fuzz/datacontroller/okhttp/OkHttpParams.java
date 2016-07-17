package com.fuzz.datacontroller.okhttp;

import com.fuzz.datacontroller.source.DataSource;

import okhttp3.Call;

/**
 * Description: Represent the default params for {@link OkHttpDataSource}. It specifies the specific
 * {@link Call} to use when it loads from network.
 */
public class OkHttpParams extends DataSource.SourceParams
        implements OkHttpDataSource.OkHttpParamsInterface {

    private final Call call;

    public OkHttpParams(Call call) {
        this.call = call;
    }

    @Override
    public Call getCall() {
        return call;
    }
}
