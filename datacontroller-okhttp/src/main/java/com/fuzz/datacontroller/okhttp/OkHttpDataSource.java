package com.fuzz.datacontroller.okhttp;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource2;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Provides the default Okhttp data source implementation. This class performs one {@link Call}
 * at a time.
 *
 * @param <TResponse> The type of the response we will convert the {@link Response} into.
 */
public class OkHttpDataSource<TResponse> extends DataSource<TResponse> {

    /**
     * The interface for {@link OkHttpParams}. This allows other params to provide a {@link Call}.
     */
    public interface OkHttpParamsInterface {

        Call getCall();
    }

    /**
     * Responsible for converting the {@link Response} into a {@link TResponse}. Use a JSON parsing
     * library of your choice to handle this.
     */
    public interface ResponseConverter<TResponse> {

        /**
         * Called whenever we get a successful response and need typed data returned.
         */
        TResponse convert(Call call, Response response);
    }

    private final ResponseConverter<TResponse> responseConverter;

    private OkHttpParamsInterface defaultParams;

    private Call currentCall;

    public OkHttpDataSource(DataSource2.RefreshStrategy<TResponse> refreshStrategy,
                            ResponseConverter<TResponse> responseConverter,
                            OkHttpParamsInterface defaultParams) {
        super(refreshStrategy);
        this.responseConverter = responseConverter;
        this.defaultParams = defaultParams;
    }

    public OkHttpDataSource(ResponseConverter<TResponse> responseConverter,
                            OkHttpParamsInterface defaultParams) {
        this.responseConverter = responseConverter;
        this.defaultParams = defaultParams;
    }

    public OkHttpDataSource(DataSource2.RefreshStrategy<TResponse> refreshStrategy,
                            ResponseConverter<TResponse> responseConverter) {
        super(refreshStrategy);
        this.responseConverter = responseConverter;
    }

    public OkHttpDataSource(ResponseConverter<TResponse> responseConverter) {
        this.responseConverter = responseConverter;
    }

    @Override
    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        currentCall = null;
    }

    @Override
    protected void doGet(SourceParams sourceParams, DataController.Success<TResponse> success,
                         DataController.Error error) {
        OkHttpParamsInterface params = getParams(sourceParams);
        currentCall = params.getCall();
        if (responseConverter == null) {
            throw new IllegalStateException("Response converter must be specified so " +
                    "we can convert the response into a type");
        }

        currentCall.enqueue(newCallback(success, error));
    }

    @Override
    protected void doStore(DataControllerResponse<TResponse> dataControllerResponse) {
        // do not store anything by default.
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.NETWORK;
    }

    protected Callback newCallback(DataController.Success<TResponse> success,
                                   DataController.Error error) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(call, e, success, error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(call, response, success, error);
            }
        };
    }

    protected void handleFailure(Call call, IOException e,
                                 DataController.Success<TResponse> success,
                                 DataController.Error error) {
        error.onFailure(new DataResponseError.Builder(getSourceType(), e)
                .build());
    }

    protected void handleResponse(Call call, Response response,
                                  DataController.Success<TResponse> success,
                                  DataController.Error error) {
        if (response.isSuccessful()) {
            success.onSuccess(new DataControllerResponse<>(
                    responseConverter.convert(call, response), getSourceType()
            ));
        } else {
            error.onFailure(new DataResponseError
                    .Builder(getSourceType(), response.message())
                    .build());
        }
    }

    protected OkHttpParamsInterface getParams(SourceParams sourceParams) {
        OkHttpParamsInterface params = defaultParams;
        if (sourceParams instanceof OkHttpParamsInterface) {
            params = (OkHttpParamsInterface) sourceParams;
        }
        if (params == null) {
            throw new IllegalArgumentException("The passed dataSource params must implement "
                    + OkHttpParamsInterface.class.getSimpleName());
        }
        return params;
    }
}
