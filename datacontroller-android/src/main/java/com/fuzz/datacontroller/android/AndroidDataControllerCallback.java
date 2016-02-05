package com.fuzz.datacontroller.android;

import android.os.Handler;
import android.os.Looper;

import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.IDataControllerCallback;

/**
 * Description: All call backs will run on the UI thread. This will either post to a {@link Handler}
 * if this comes from a background thread, otherwise it will execute the callbacks immediately.
 */
public abstract class AndroidDataControllerCallback<TResponse> implements IDataControllerCallback<TResponse> {

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable currentRunnable;

    @Override
    public final void onSuccess(final TResponse tResponse, final String requestUrl) {
        executeCurrent(new Runnable() {
            @Override
            public void run() {
                onFGSuccess(tResponse, requestUrl);
            }
        });
    }

    @Override
    public final void onFailure(final DataResponseError error) {
        executeCurrent(new Runnable() {
            @Override
            public void run() {
                onFGFailure(error);
            }
        });
    }

    @Override
    public final void onEmpty() {
        executeCurrent(new Runnable() {
            @Override
            public void run() {
                onFGEmpty();
            }
        });
    }

    @Override
    public final void onStartLoading() {
        executeCurrent(new Runnable() {
            @Override
            public void run() {
                onFGStartLoading();
            }
        });
    }

    @Override
    public final void onClosed() {
        executeCurrent(new Runnable() {
            @Override
            public void run() {
                onFGClosed();
            }
        });
    }

    public void cancelCurrent() {
        if (currentRunnable != null) {
            handler.removeCallbacks(currentRunnable);
            currentRunnable = null;
        }
    }

    private void executeCurrent(Runnable runnable) {
        if (currentRunnable != null) {
            handler.removeCallbacks(currentRunnable);
        }
        currentRunnable = runnable;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(currentRunnable);
        } else {
            currentRunnable.run();
        }
    }

    public abstract void onFGSuccess(TResponse response, String requestUrl);

    public abstract void onFGFailure(DataResponseError error);

    public abstract void onFGEmpty();

    public abstract void onFGStartLoading();

    public abstract void onFGClosed();
}
