package com.fuzz.datacontroller.okhttp;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;

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
public class OkHttpDataSource<TResponse> implements DataSource.DataSourceCaller<TResponse> {

    public static <TResponse> DataSource.Builder<TResponse> builderInstance(
            ResponseConverter<TResponse> responseConverter,
            CallbackHandler<TResponse> handler) {
        return new DataSource.Builder<>(new OkHttpDataSource<>(responseConverter, handler),
                DataSource.SourceType.NETWORK);
    }

    public static <TResponse> DataSource.Builder<TResponse> builderInstance(
            ResponseConverter<TResponse> responseConverter) {
        return new DataSource.Builder<>(new OkHttpDataSource<>(responseConverter),
                DataSource.SourceType.NETWORK);
    }

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

    public interface CallbackHandler<TResponse> {

        void onHandleResponse(Call call, Response response, DataController.Success<TResponse> success,
                              DataController.Error error);

        void onHandleFailure(Call call, IOException e, DataController.Success<TResponse> success,
                             DataController.Error error);
    }

    private final ResponseConverter<TResponse> responseConverter;

    private final CallbackHandler<TResponse> callbackHandler;

    private Call currentCall;

    private OkHttpDataSource(ResponseConverter<TResponse> responseConverter,
                             CallbackHandler<TResponse> callbackHandler) {
        this.responseConverter = responseConverter;
        this.callbackHandler = callbackHandler;
    }

    private OkHttpDataSource(ResponseConverter<TResponse> responseConverter) {
        this.responseConverter = responseConverter;
        this.callbackHandler = new DefaultCallbackHandler();
    }

    @Override
    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        currentCall = null;
    }

    @Override
    public void get(DataSource.SourceParams sourceParams, DataController.Error error,
                    DataController.Success<TResponse> success) {
        OkHttpParamsInterface params = getParams(sourceParams);
        currentCall = params.getCall();
        if (responseConverter == null) {
            throw new IllegalStateException("Response converter must be specified so " +
                    "we can convert the response into a type");
        }

        if (currentCall == null) {
            throw new IllegalArgumentException("The OKHttpParams must provide a non null call to execute");
        }

        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callbackHandler.onHandleFailure(call, e, success, error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callbackHandler.onHandleResponse(call, response, success, error);
            }
        });
    }

    private OkHttpParamsInterface getParams(DataSource.SourceParams sourceParams) {
        OkHttpParamsInterface params = null;
        if (sourceParams instanceof OkHttpParamsInterface) {
            params = (OkHttpParamsInterface) sourceParams;
        }
        if (params == null) {
            throw new IllegalArgumentException("The passed dataSource params must implement "
                    + OkHttpParamsInterface.class.getSimpleName());
        }
        return params;
    }

    /**
     * Description: Represent the default params for {@link OkHttpDataSource}. It specifies the specific
     * {@link Call} to use when it loads from network.
     */
    public static class OkHttpParams extends DataSource.SourceParams
            implements OkHttpParamsInterface {

        private final Call call;

        public OkHttpParams(Call call) {
            this.call = call;
        }

        @Override
        public Call getCall() {
            return call;
        }
    }

    class DefaultCallbackHandler implements CallbackHandler<TResponse> {

        @Override
        public void onHandleResponse(Call call, Response response,
                                     DataController.Success<TResponse> success,
                                     DataController.Error error) {
            if (response.isSuccessful()) {
                success.onSuccess(new DataControllerResponse<>(
                        responseConverter.convert(call, response), DataSource.SourceType.NETWORK
                ));
            } else {
                error.onFailure(new DataResponseError
                        .Builder(DataSource.SourceType.NETWORK, response.message())
                        .build());
            }
        }

        @Override
        public void onHandleFailure(Call call, IOException e,
                                    DataController.Success<TResponse> success,
                                    DataController.Error error) {
            error.onFailure(new DataResponseError.Builder(DataSource.SourceType.NETWORK, e)
                    .build());
        }
    }
}
