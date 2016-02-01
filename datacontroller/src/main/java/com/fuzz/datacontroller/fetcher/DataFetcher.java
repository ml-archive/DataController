package com.fuzz.datacontroller.fetcher;

import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.IDataCallback;

/**
 * Description: Responsible for fetching data.
 */
public abstract class DataFetcher<TResponse> {

    private final IDataCallback<TResponse> callback;

    protected DataFetcher(IDataCallback<TResponse> callback) {
        this.callback = callback;
    }

    public IDataCallback<TResponse> getCallback() {
        return callback;
    }

    /**
     * Call this data fetcher.
     */
    public abstract void call();

    /**
     * Attempts to cancel itself.
     */
    public void cancel() {

    }

    /**
     * Invoke this method when the datafetcher completes, or override this method to suit your needs.
     * @param response The response from a {@link DataFetcher}.
     * @param originalUrl
     */
    public void onSuccess(TResponse response, String originalUrl) {
        getCallback().onSuccess(response, originalUrl);
    }

    public void onFailure(DataResponseError error) {
        getCallback().onFailure(error);
    }
}
