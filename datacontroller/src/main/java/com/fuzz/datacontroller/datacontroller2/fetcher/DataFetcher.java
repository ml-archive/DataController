package com.fuzz.datacontroller.datacontroller2.fetcher;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.DataResponseError;

/**
 * Description: Responsible for fetching data.
 */
public interface DataFetcher<TResponse> {

    /**
     * Simple interface defining how to call this {@link DataFetcher}
     *
     * @param <TResponse>
     */
    interface Fetcher<TResponse> {

        void call(DataFetcher<TResponse> dataFetcher);

        void cancel(DataFetcher<TResponse> dataFetcher);
    }

    /**
     * Description: Represents a failed callback response.
     */
    interface Error {

        void onFailure(DataResponseError dataResponseError);
    }

    /**
     * Description: Represents a successful execution.
     */
    interface Success<TResponse> {

        void onSuccess(DataControllerResponse<TResponse> response);
    }

    void cancel();

    void call(Success<TResponse> success, Error error);

}
