package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Description: Defines a datafetcher for OkHttp.
 */
public abstract class OkHttpDataSource<TResponse> extends DataSource<TResponse> {

    private Call call;

    private final Type responseClass;

    public OkHttpDataSource(RefreshStrategy<TResponse> refreshStrategy, Type responseClass) {
        super(refreshStrategy);
        this.responseClass = responseClass;
    }

    public OkHttpDataSource(Type responseClass) {
        this.responseClass = responseClass;
    }


    @Override
    public SourceType getSourceType() {
        return SourceType.NETWORK;
    }

    @Override
    protected void doGet(SourceParams sourceParams, final DataController.Success<TResponse> success,
                         final DataController.Error error) {
        if (call == null) {
            call = createCall();
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error.onFailure(new DataResponseError(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        TResponse tResponse = NetworkApiManager.get().getGson().fromJson(response.body().charStream(), responseClass);
                        callSuccess(tResponse, call.request().url().toString(), success);
                    } catch (Exception e) {
                        callFailure(e, null, error);
                    }
                } else {
                    callFailure(null, response.message(), error);
                }
            }
        });
    }

    @Override
    protected void doStore(DataControllerResponse<TResponse> dataControllerResponse) {
    }

    protected void callSuccess(TResponse response, String originalUrl, DataController.Success<TResponse> success) {
        success.onSuccess(new DataControllerResponse<>(response, SourceType.NETWORK, originalUrl));
    }

    protected void callFailure(Throwable throwable, String responseError, DataController.Error error) {
        if (throwable != null) {
            error.onFailure(new DataResponseError(throwable));
        } else {
            error.onFailure(new DataResponseError(responseError));
        }
    }

    protected abstract Call createCall();
}
