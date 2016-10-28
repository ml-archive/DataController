package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController2;
import com.fuzz.datacontroller.DataControllerResponse;

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
    public void doStore(DataControllerResponse<TResponse> tResponse) {
        this.storage = tResponse.getResponse();
    }

    @Override
    public void doGet(SourceParams sourceParams,
                      DataController2.Success<TResponse> success,
                      DataController2.Error error) {
        success.onSuccess(new DataControllerResponse<>(storage, getSourceType()));
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.MEMORY;
    }

    @Override
    public TResponse getStoredData(SourceParams sourceParams) {
        return storage;
    }

    @Override
    public void clearStoredData(SourceParams sourceParams) {
        storage = null;
    }

    @Override
    public void cancel() {
    }
}
