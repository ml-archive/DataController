package com.fuzz.datacontroller;

/**
 * Description: Provides method stubs for the callback, so a smaller subset of methods only need be implemented.
 */
public abstract class SimpleDataControllerCallback<TResponse> implements IDataControllerCallback<TResponse> {

    @Override
    public void onFailure(DataResponseError error) {

    }

    @Override
    public void onEmpty() {

    }

    @Override
    public void onStartLoading() {

    }

    @Override
    public void onClosed() {

    }
}
