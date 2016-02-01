package com.fuzz.datacontroller.android;

import com.fuzz.datacontroller.DataResponseError;

/**
 * Description: Provides method stubs for the callback, so a smaller subset of methods only need be implemented.
 * Similiar to {@link SimpleAndroidDataControllerCallback}
 */
public abstract class SimpleAndroidDataControllerCallback<TResponse> extends AndroidDataControllerCallback<TResponse> {
    @Override
    public void onFGFailure(DataResponseError error) {

    }

    @Override
    public void onFGEmpty() {

    }

    @Override
    public void onFGStartLoading() {

    }

    @Override
    public void onFGClosed() {

    }
}
