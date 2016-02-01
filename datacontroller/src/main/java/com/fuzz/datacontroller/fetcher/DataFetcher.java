package com.fuzz.datacontroller.fetcher;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.IDataCallback;

/**
 * Description: Responsible for fetching data.
 */
public abstract class DataFetcher<TResponse> {

    private final IDataCallback<DataControllerResponse<TResponse>> callback;

    protected DataFetcher(IDataCallback<DataControllerResponse<TResponse>> callback) {
        this.callback = callback;
    }

    public IDataCallback<DataControllerResponse<TResponse>> getCallback() {
        return callback;
    }

    /**
     * Call this data fetcher.
     */
    public abstract void call();

    /**
     * @return The kind of response this fetcher gets its information from.
     */
    public abstract DataControllerResponse.ResponseType getResponseType();

    /**
     * Attempts to cancel itself.
     */
    public void cancel() {

    }

    /**
     * Invoke this method when the datafetcher completes, or override this method to suit your needs.
     *
     * @param response    The response from a {@link DataFetcher}.
     * @param originalUrl
     */
    public void onSuccess(TResponse response, String originalUrl) {
        getCallback().onSuccess(new DataControllerResponse<>(response, getResponseType()), originalUrl);
    }

    public void onFailure(DataResponseError error) {
        getCallback().onFailure(error);
    }
}
