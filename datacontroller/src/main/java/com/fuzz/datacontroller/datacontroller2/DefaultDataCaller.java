package com.fuzz.datacontroller.datacontroller2;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.fetcher.DataFetcher;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Provides default implementation for mapping {@link DataFetcher} to a {@link DataSource}.
 * It simply if found successful, stores non-memory data in the {@link DataSource}.
 */
public class DefaultDataCaller<TResponse, TStorage> implements DataCaller<TResponse, TStorage> {

    @Override
    public void call(DataFetcher<TResponse> dataFetcher, final DataSource<TResponse, TStorage> dataSource) {
        dataFetcher.call(new DataFetcher.Success<TResponse>() {
            @Override
            public void onSuccess(DataControllerResponse<TResponse> response) {
                if (!response.getType().equals(DataControllerResponse.ResponseType.MEMORY)) {
                    dataSource.store(response.getResponse());
                }
            }
        }, new DataFetcher.Error() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {

            }
        });
    }

    @Override
    public void cancel(DataFetcher<TResponse> dataFetcher) {
        dataFetcher.cancel();
    }
}
