package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.DataResponseError;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Provides stubs for method calls.
 */
public abstract class MockDataSource<TResponse> extends DataSource<TResponse> {

    private boolean isGetCalled;
    private boolean isStoreCalled;
    private boolean isGetStoredCalled;

    @Override
    protected void doGet(SourceParams sourceParams, DataController.Success<TResponse> success, DataController.Error error) {
        isGetCalled = true;
        if (sourceParams == null) {
            error.onFailure(new DataResponseError(""));
        } else {
            success.onSuccess(new DataControllerResponse<TResponse>(null, getSourceType()));
        }
    }

    @Override
    protected void doStore(DataControllerResponse<TResponse> dataControllerResponse) {
        isStoreCalled = true;
    }

    @Override
    public TResponse getStoredData(SourceParams sourceParams) {
        isGetStoredCalled = true;
        return null;
    }

    public boolean isGetCalled() {
        return isGetCalled;
    }

    public boolean isStoreCalled() {
        return isStoreCalled;
    }

    public void setGetCalled(boolean getCalled) {
        isGetCalled = getCalled;
    }

    public void setStoreCalled(boolean storeCalled) {
        isStoreCalled = storeCalled;
    }

    public boolean isGetStoredCalled() {
        return isGetStoredCalled;
    }

    public void setGetStoredCalled(boolean getStoredCalled) {
        isGetStoredCalled = getStoredCalled;
    }
}
