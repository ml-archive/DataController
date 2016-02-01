package com.fuzz.datacontroller;

/**
 * Description: A simple implementation of the {@link IDataControllerCallback} so that you don't need
 * override every method if you don't care about it.
 */
public abstract class DataControllerCallback<TResponse> implements IDataControllerCallback<TResponse> {

    @Override
    public void onSuccess(TResponse tResponse, String requestUrl) {

    }

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
