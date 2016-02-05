package com.fuzz.datacontroller.fetcher;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.IDataCallback;

/**
 * Description: Responsible for fetching data.
 */
public abstract class DataFetcher<TResponse> {

    private IDataCallback<DataControllerResponse<TResponse>> callback;

    protected DataFetcher(IDataCallback<DataControllerResponse<TResponse>> callback) {
        this.callback = callback;
    }

    protected DataFetcher() {
    }

    public void setCallback(IDataCallback<DataControllerResponse<TResponse>> callback) {
        this.callback = callback;
    }

    public IDataCallback<DataControllerResponse<TResponse>> getCallback() {
        return callback;
    }

    /**
     * Call this data fetcher async.
     */
    public abstract void callAsync();

    /**
     * Call to execute synchronously. This should happen on a non-UI thread on an
     * Android device.
     */
    public TResponse call() {
        throw new IllegalStateException("This method should be overridden with proper implementation");
    }

    /**
     * @return The kind of response this fetcher gets its information from.
     */
    public abstract DataControllerResponse.ResponseType getResponseType();

    /**
     * Attempts to cancel itself. Override to properly cancel when called.
     */
    public void cancel() {

    }

    /**
     * Invoke this method when the datafetcher completes, or override this method to suit your needs.
     *
     * @param response    The response from a {@link DataFetcher}.
     * @param originalUrl The original url called.
     */
    public void onSuccess(TResponse response, String originalUrl) {
        getCallback().onSuccess(new DataControllerResponse<>(response, getResponseType()), originalUrl);
    }

    /**
     * Called when we experience some kind of error.
     *
     * @param error The error we experienced.
     */
    public void onFailure(DataResponseError error) {
        getCallback().onFailure(error);
    }
}
