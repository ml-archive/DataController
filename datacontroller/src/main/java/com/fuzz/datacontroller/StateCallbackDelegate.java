package com.fuzz.datacontroller;

import com.fuzz.datacontroller.DataController.State;

/**
 * Description: Provides handy logic to wrap a callback in {@link State}
 */
public class StateCallbackDelegate<TResponse> implements IDataControllerCallback<TResponse> {

    public interface StateSetter {

        void setState(State state);
    }

    private final IDataControllerCallback<TResponse> containedCallback;
    private final StateSetter stateSetter;

    public StateCallbackDelegate(IDataControllerCallback<TResponse> containedCallback, StateSetter stateSetter) {
        this.containedCallback = containedCallback;
        this.stateSetter = stateSetter;
    }

    @Override
    public void onSuccess(TResponse tResponse, String requestUrl) {
        stateSetter.setState(State.SUCCESS);
        containedCallback.onSuccess(tResponse, requestUrl);
    }

    @Override
    public void onFailure(DataResponseError error) {
        stateSetter.setState(State.FAILURE);
        containedCallback.onFailure(error);
    }

    @Override
    public void onEmpty() {
        stateSetter.setState(State.EMPTY);
        containedCallback.onEmpty();
    }

    @Override
    public void onStartLoading() {
        if (shouldShowLoadingState()) {
            stateSetter.setState(State.LOADING);
        }
        containedCallback.onStartLoading();
    }

    @Override
    public void onClosed() {
        stateSetter.setState(State.NONE);
        containedCallback.onClosed();
    }

    protected boolean shouldShowLoadingState() {
        return true;
    }

}
