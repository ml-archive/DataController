package com.fuzz.datacontroller.datacontroller2.source;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.strategy.RefreshStrategy;

/**
 * Description: Provides default memory based source. It will always succeed in returning because
 * the storage may or may not have data in it. It does not rely on a network call or database lookup.
 * It stores only information coming from other sources.
 */
public class MemoryDataSource<TResponse> extends DataSource<TResponse> {

    private TResponse storage;

    public MemoryDataSource(RefreshStrategy<TResponse> refreshStrategy) {
        super(refreshStrategy);
    }

    public MemoryDataSource() {
    }

    @Override
    public void store(DataControllerResponse<TResponse> tResponse) {
        if (!tResponse.getSourceType().equals(SourceType.MEMORY)) {
            this.storage = tResponse.getResponse();
        }
    }

    @Override
    public void doGet(SourceParams sourceParams, DataController.Success<TResponse> success, DataController.Error error) {
        success.onSuccess(new DataControllerResponse<>(storage, SourceType.MEMORY));
    }
}
