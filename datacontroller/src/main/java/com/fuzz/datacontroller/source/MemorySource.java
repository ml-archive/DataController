package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;

/**
 * Description: Provides default memory based source. It will always succeed in returning because
 * the storage may or may not have data in it. It does not rely on a network call or database lookup.
 * It stores only information coming from other sources.
 */
public class MemorySource<TResponse> implements DataSource.Source<TResponse> {

    public static <TResponse> DataSource.Builder<TResponse> builderInstance() {
        MemorySource<TResponse> source = new MemorySource<>();
        return new DataSource.Builder<>(source, DataSource.SourceType.MEMORY);
    }

    private TResponse storage;

    @Override
    public TResponse getStoredData(DataSource.SourceParams sourceParams) {
        return storage;
    }

    @Override
    public void clearStoredData(DataSource.SourceParams sourceParams) {
        storage = null;
    }

    @Override
    public void get(DataSource.SourceParams sourceParams, DataController.Error error,
                    DataController.Success<TResponse> success) {
        success.onSuccess(new DataControllerResponse<>(storage, DataSource.SourceType.MEMORY));
    }

    @Override
    public void cancel() {
    }

    @Override
    public void store(DataControllerResponse<TResponse> response) {
        this.storage = response.getResponse();
    }

    @Override
    public boolean hasStoredData(DataSource.SourceParams params) {
        return getStoredData(params) != null;
    }
}
