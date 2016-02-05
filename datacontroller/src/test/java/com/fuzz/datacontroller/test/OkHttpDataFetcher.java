package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.IDataCallback;
import com.fuzz.datacontroller.fetcher.DataFetcher;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Description: Defines a datafetcher for OkHttp.
 */
public abstract class OkHttpDataFetcher<TResponse> extends DataFetcher<TResponse> {

    private Call call;

    private Type responseClass;

    public OkHttpDataFetcher(IDataCallback<DataControllerResponse<TResponse>> callback, Class<TResponse> responseClass) {
        super(callback);
        this.responseClass = responseClass;
    }

    public OkHttpDataFetcher(IDataCallback<DataControllerResponse<TResponse>> callback, TypeToken<TResponse> responseType) {
        super(callback);
        this.responseClass = responseType.getType();
    }

    @Override
    public void callAsync() {
        if (call == null) {
            call = createCall();
        }
        call.enqueue(callback);
    }

    @Override
    public TResponse call() {
        if (call == null) {
            call = createCall();
        }
        try {
            Response response = call.execute();
            return NetworkApiManager.get().getGson().fromJson(response.body().charStream(), responseClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public DataControllerResponse.ResponseType getResponseType() {
        return DataControllerResponse.ResponseType.NETWORK;
    }

    protected abstract Call createCall();

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            getCallback().onFailure(new DataResponseError(e));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                try {
                    TResponse tResponse = NetworkApiManager.get().getGson().fromJson(response.body().charStream(), responseClass);
                    OkHttpDataFetcher.this.onSuccess(tResponse, call.request().url().toString());
                } catch (Exception e) {
                    OkHttpDataFetcher.this.onFailure(new DataResponseError(e));
                }
            } else {
                OkHttpDataFetcher.this.onFailure(new DataResponseError(response.message()));
            }
        }
    };
}
