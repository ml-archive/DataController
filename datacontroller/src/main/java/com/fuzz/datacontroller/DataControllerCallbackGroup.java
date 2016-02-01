package com.fuzz.datacontroller;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Description:
 */
public class DataControllerCallbackGroup<TResponse> implements IDataControllerCallback<TResponse> {

    private final Set<IDataControllerCallback<TResponse>> callbacks = new LinkedHashSet<>();

    public void registerForCallbacks(IDataControllerCallback<TResponse> dataControllerCallback) {
        synchronized (callbacks) {
            callbacks.add(dataControllerCallback);
        }
    }

    public void deregisterForCallbacks(IDataControllerCallback<TResponse> dataControllerCallback) {
        synchronized (callbacks) {
            callbacks.remove(dataControllerCallback);
        }
    }

    public boolean isEmpty() {
        return callbacks.isEmpty();
    }

    @Override
    public void onSuccess(TResponse response, String requestUrl) {
        for (IDataControllerCallback<TResponse> callback : callbacks) {
            callback.onSuccess(response, requestUrl);
        }
    }

    @Override
    public void onFailure(DataResponseError error) {
        for (IDataControllerCallback<TResponse> callback : callbacks) {
            callback.onFailure(error);
        }
    }

    @Override
    public void onEmpty() {
        for (IDataControllerCallback<TResponse> callback : callbacks) {
            callback.onEmpty();
        }
    }

    @Override
    public void onStartLoading() {
        for (IDataControllerCallback<TResponse> callback : callbacks) {
            callback.onStartLoading();
        }
    }

    @Override
    public void onClosed() {
        for (IDataControllerCallback<TResponse> callback : callbacks) {
            callback.onClosed();
        }
    }
}
