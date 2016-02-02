package com.fuzz.datacontroller;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Description: Represents a set of {@link IDataControllerCallback}. It contains methods to allow
 * multiple callbacks, and itself is a {@link IDataControllerCallback}.
 */
public class DataControllerCallbackGroup<TResponse> implements IDataControllerCallback<DataControllerResponse<TResponse>> {

    private final Set<IDataControllerCallback<DataControllerResponse<TResponse>>> callbacks = new LinkedHashSet<>();

    public void registerForCallbacks(IDataControllerCallback<DataControllerResponse<TResponse>> dataControllerCallback) {
        synchronized (callbacks) {
            callbacks.add(dataControllerCallback);
        }
    }

    public void deregisterForCallbacks(IDataControllerCallback<DataControllerResponse<TResponse>> dataControllerCallback) {
        synchronized (callbacks) {
            callbacks.remove(dataControllerCallback);
        }
    }

    public boolean isEmpty() {
        return callbacks.isEmpty();
    }

    @Override
    public void onSuccess(DataControllerResponse<TResponse> response, String requestUrl) {
        for (IDataControllerCallback<DataControllerResponse<TResponse>> callback : callbacks) {
            callback.onSuccess(response, requestUrl);
        }
    }

    @Override
    public void onFailure(DataResponseError error) {
        for (IDataControllerCallback<DataControllerResponse<TResponse>> callback : callbacks) {
            callback.onFailure(error);
        }
    }

    @Override
    public void onEmpty() {
        for (IDataControllerCallback<DataControllerResponse<TResponse>> callback : callbacks) {
            callback.onEmpty();
        }
    }

    @Override
    public void onStartLoading() {
        for (IDataControllerCallback<DataControllerResponse<TResponse>> callback : callbacks) {
            callback.onStartLoading();
        }
    }

    @Override
    public void onClosed() {
        for (IDataControllerCallback<DataControllerResponse<TResponse>> callback : callbacks) {
            callback.onClosed();
        }
    }
}
