package com.fuzz.datacontroller;

import com.fuzz.datacontroller.DataController.DataControllerCallback;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Description: Groups a set of {@link DataControllerCallback} into one unified group.
 */
public class DataControllerCallbackGroup<TResponse> implements DataControllerCallback<TResponse> {

    private final Set<DataControllerCallback<TResponse>> callbacks = new CopyOnWriteArraySet<>();

    public void registerForCallbacks(DataControllerCallback<TResponse> dataControllerCallback) {
        synchronized (callbacks) {
            callbacks.add(dataControllerCallback);
        }
    }

    public void deregisterForCallbacks(DataControllerCallback<TResponse> dataControllerCallback) {
        synchronized (callbacks) {
            callbacks.remove(dataControllerCallback);
        }
    }

    public void clearCallbacks() {
        synchronized (callbacks) {
            callbacks.clear();
        }
    }

    public boolean hasCallbacks() {
        synchronized (callbacks) {
            return !callbacks.isEmpty();
        }
    }

    @Override
    public void onFailure(DataResponseError dataResponseError) {
        synchronized (callbacks) {
            for (DataControllerCallback<TResponse> callback : callbacks) {
                if (callback != null) {
                    callback.onFailure(dataResponseError);
                }
            }
        }
    }

    @Override
    public void onSuccess(DataControllerResponse<TResponse> response) {
        synchronized (callbacks) {
            for (DataControllerCallback<TResponse> callback : callbacks) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }
        }
    }
}
