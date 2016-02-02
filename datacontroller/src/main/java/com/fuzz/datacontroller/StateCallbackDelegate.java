package com.fuzz.datacontroller;

import com.fuzz.datacontroller.DataController.State;

/**
 * Description: Provides handy logic to wrap a callback in {@link State}. This allows you to respond to state
 * changes in an efficient way.
 */
public class StateCallbackDelegate<TResponse> implements IDataControllerCallback<TResponse> {

    /**
     * Simple interface for setting state. Use this to update your View hierarchy or some object that needs it.
     */
    public interface StateSetter {

        /**
         * The state to set.
         *
         * @param state State from the callbacks.
         */
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
        if (shouldShowFailureState()) {
            stateSetter.setState(State.FAILURE);
        }
        containedCallback.onFailure(error);
    }

    @Override
    public void onEmpty() {
        if (shouldShowEmptyState()) {
            stateSetter.setState(State.EMPTY);
        }
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

    protected boolean shouldShowFailureState() {
        return true;
    }

    protected boolean shouldShowEmptyState() {
        return true;
    }

}
