package com.fuzz.datacontroller.retrofit;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceCaller;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Description: Provides the default {@link DataSourceCaller} that we use to call through to retrofit
 * via {@link RetrofitSourceParams}.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class RetrofitSource<TResponse> implements DataSourceCaller<TResponse> {

    public static <T> DataSource.Builder<T> builderInstance(ResponseHandler<T> responseHandler,
                                                            ResponseErrorConverter<T> errorConverter) {
        RetrofitSource<T> source = new RetrofitSource<>(responseHandler, errorConverter);
        return new DataSource.Builder<>(source);
    }

    public static <T> DataSource.Builder<T> builderInstance(ResponseErrorConverter<T> errorConverter) {
        RetrofitSource<T> source = new RetrofitSource<>(errorConverter);
        return new DataSource.Builder<>(source);
    }


    public static <T> DataSource.Builder<T> builderInstance() {
        RetrofitSource<T> source = new RetrofitSource<>();
        return new DataSource.Builder<>(source);
    }

    public interface ResponseHandler<TResponse> {

        void handleResponse(RetrofitSource<TResponse> retrofitSource,
                            DataSource.SourceParams sourceParams, Call<TResponse> call,
                            Response<TResponse> response,
                            DataController.Success<TResponse> success,
                            DataController.Error error,
                            int currentRetryCount);

        void handleFailure(RetrofitSource<TResponse> retrofitSource,
                           DataSource.SourceParams sourceParams,
                           DataController.Success<TResponse> success,
                           DataController.Error error,
                           DataResponseError dataResponseError,
                           int currentRetryCount);
    }

    public interface ResponseErrorConverter<TResponse> {

        DataResponseError convertFromResponse(Response<TResponse> response);
    }

    private final ResponseHandler<TResponse> responseHandler;
    private final ResponseErrorConverter<TResponse> responseErrorConverter;

    private Call<TResponse> currentCall;

    public RetrofitSource(ResponseHandler<TResponse> responseHandler,
                          ResponseErrorConverter<TResponse> responseErrorConverter) {
        this.responseHandler = responseHandler;
        this.responseErrorConverter = responseErrorConverter;
    }

    public RetrofitSource() {
        this.responseErrorConverter = new DefaultResponseErrorConverter<>();
        this.responseHandler = new DefaultResponseHandler<>();
    }

    public RetrofitSource(ResponseErrorConverter<TResponse> responseErrorConverter) {
        this.responseHandler = new DefaultResponseHandler<>();
        this.responseErrorConverter = responseErrorConverter;
    }


    @Override
    public void get(final DataSource.SourceParams sourceParams,
                    final DataController.Error error,
                    final DataController.Success<TResponse> success) {
        doGetInternal(sourceParams, error, success, 0);
    }

    private void doGetInternal(final DataSource.SourceParams sourceParams,
                               final DataController.Error error,
                               final DataController.Success<TResponse> success,
                               final int currentRetryCount) {
        currentCall = getCall(sourceParams);
        if (currentCall != null) {
            currentCall.enqueue(new Callback<TResponse>() {
                @Override
                public void onResponse(Call<TResponse> call, Response<TResponse> response) {
                    responseHandler.handleResponse(RetrofitSource.this,
                        sourceParams, call, response, success, error, currentRetryCount);
                }

                @Override
                public void onFailure(Call<TResponse> call, Throwable t) {
                    responseHandler.handleFailure(RetrofitSource.this,
                        sourceParams, success, error,
                        new DataResponseError.Builder(DataSource.SourceType.NETWORK, t).build(),
                        currentRetryCount);
                }
            });
        } else {
            throw new IllegalStateException("No call found for this RetrofitDataSource. Please provide one " +
                "in the constructor or through ApiSourceParams");
        }
    }


    @Override
    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
    }

    @Override
    public DataSource.SourceType sourceType() {
        return DataSource.SourceType.NETWORK;
    }

    private Call<TResponse> getCall(DataSource.SourceParams sourceParams) {
        Call<TResponse> responseCall = null;
        if (sourceParams instanceof RetrofitSourceParams) {
            //noinspection unchecked
            responseCall = ((RetrofitSourceParams) sourceParams).getCall();
        }
        // here to patch up retrying requests.
        if (responseCall != null && responseCall.isExecuted()) {
            responseCall = responseCall.clone();
        }
        return responseCall;
    }

    public static class DefaultResponseHandler<TResponse> implements ResponseHandler<TResponse> {

        @Override
        public void handleResponse(RetrofitSource<TResponse> retrofitSource,
                                   DataSource.SourceParams sourceParams, Call<TResponse> call,
                                   Response<TResponse> response,
                                   DataController.Success<TResponse> success,
                                   DataController.Error error,
                                   int currentRetryCount) {
            if (response.isSuccessful()) {
                Request request = call.request();
                HttpUrl url = request.url();
                if (request.body() instanceof FormBody) {
                    FormBody body = ((FormBody) request.body());
                    HttpUrl.Builder builder = url.newBuilder();
                    for (int i = 0; i < body.size(); i++) {
                        builder.addEncodedQueryParameter(body.encodedName(i), body.encodedValue(i));
                    }
                    url = builder.build();
                }
                success.onSuccess(new DataControllerResponse<>(response.body(),
                    DataSource.SourceType.NETWORK, url.toString()));
            } else {
                handleFailure(retrofitSource, sourceParams, success, error,
                    retrofitSource.responseErrorConverter.convertFromResponse(response),
                    currentRetryCount);
            }
        }

        @Override
        public void handleFailure(RetrofitSource<TResponse> retrofitSource,
                                  DataSource.SourceParams sourceParams,
                                  DataController.Success<TResponse> success,
                                  DataController.Error error,
                                  DataResponseError dataResponseError,
                                  int currentRetryCount) {
            if (currentRetryCount < getRetryCount()) {
                int count = currentRetryCount + 1;
                retrofitSource.doGetInternal(sourceParams, error, success, count);
            } else {
                error.onFailure(dataResponseError);
            }
        }

        /**
         * @return Override to provide custom retry count number. Default is 0
         */
        protected int getRetryCount() {
            return 0;
        }
    }

    public static class DefaultResponseErrorConverter<TResponse> implements ResponseErrorConverter<TResponse> {

        @Override
        public DataResponseError convertFromResponse(Response<TResponse> response) {
            return new DataResponseError.Builder(DataSource.SourceType.NETWORK, response.message())
                .status(response.code())
                .userFacingMessage(response.message()).build();
        }
    }

    public static class RetrofitSourceParams<TResponse> extends DataSource.SourceParams {

        private final Call<TResponse> call;

        public RetrofitSourceParams(Call<TResponse> call) {
            this.call = call;
        }

        public Call<TResponse> getCall() {
            return call;
        }
    }
}
