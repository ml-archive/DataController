package com.fuzz.datacontroller;

import com.fuzz.datacontroller.fetcher.DataFetcher;

/**
 * Description: Interface for abstracting out the data requesting callback.
 */
public interface IDataCallback<TResponse> {

    /**
     * Called when completed.
     *
     * @param response    The response from {@link DataFetcher}.
     * @param originalUrl The original URL/data that was called.
     */
    void onSuccess(TResponse response, String originalUrl);

    /**
     * Called when there was a failure.
     *
     * @param dataResponseError The failure wrapper class that occured.
     */
    void onFailure(DataResponseError dataResponseError);
}
