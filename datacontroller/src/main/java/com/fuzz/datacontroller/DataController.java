package com.fuzz.datacontroller;

import com.fuzz.datacontroller.fetcher.DataFetcher;
import com.fuzz.datacontroller.strategy.IRefreshStrategy;

/**
 * Description: Responsible for managing how data is loaded, stored, and handled outside of Activity,
 * or Android lifecycles.
 */
public abstract class DataController<TResponse> {

    public enum State {
        /**
         * No state. Nothing has been started. Results from app launching or data clearing
         */
        NONE,

        /**
         * Loading state. We are performing some network or async call on a different thread. This
         * prevents us from making the same call twice.
         */
        LOADING,

        /**
         * Empty state. The resulting response is empty.
         */
        EMPTY,

        /**
         * Last update was a success and we have data.
         */
        SUCCESS,

        /**
         * Last update failed and our data may or may not be up to date.
         */
        FAILURE
    }

    private DataFetcher<TResponse> dataFetcher;
    private IDataStore<TResponse> dataStore;
    private IRefreshStrategy refreshStrategy;
    private final DataControllerCallbackGroup<TResponse> dataControllerGroup = new DataControllerCallbackGroup<>();
    private State state = State.NONE;

    public DataFetcher<TResponse> getDataFetcher() {
        if (dataFetcher == null) {
            dataFetcher = createDataFetcher();
        }
        return dataFetcher;
    }

    public void setRefreshStrategy(IRefreshStrategy refreshStrategy) {
        this.refreshStrategy = refreshStrategy;
    }

    public void setDataStore(IDataStore<TResponse> dataStore) {
        this.dataStore = dataStore;
    }

    public TResponse getStoredData() {
        return dataStore != null ? dataStore.get() : null;
    }

    public boolean hasStoredData() {
        return !isEmpty(getStoredData());
    }

    public void registerForCallbacks(IDataControllerCallback<TResponse> dataControllerCallback) {
        dataControllerGroup.registerForCallbacks(dataControllerCallback);
    }

    public void deregisterForCallbacks(IDataControllerCallback<TResponse> dataControllerCallback) {
        dataControllerGroup.deregisterForCallbacks(dataControllerCallback);
    }

    /**
     * Cancels any pending requests and then re-requests data using the {@link DataFetcher}.
     *
     * @return Stored data.
     */
    public TResponse requestDataCancel() {
        getDataFetcher().cancel();
        setState(State.NONE);
        return requestData();
    }

    public void cancel() {
        getDataFetcher().cancel();
        setState(State.NONE);
    }

    public TResponse requestData() {
        TResponse response = getStoredData();
        if (!state.equals(State.LOADING) && (refreshStrategy == null || refreshStrategy.shouldRefresh(this))) {
            setState(State.LOADING);
            dataControllerGroup.onStartLoading();
            requestDataAsync();
        }
        return response;
    }

    public final void requestDataAsync() {
        getDataFetcher().call();
    }

    protected synchronized void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public IDataCallback<TResponse> getDataCallback() {
        return dataCallback;
    }

    public void clearStoredData() {
        if (dataStore != null) {
            dataStore.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IRefreshStrategy> T getRefreshStrategy() {
        return (T) refreshStrategy;
    }

    /**
     * Clears stored data and will set its state back to {@link com.belk.android.api.data.DataController.State#NONE}.
     */
    public void close() {
        setState(State.NONE);
        clearStoredData();
        dataControllerGroup.onClosed();
    }

    /**
     * Closes if no {@link IDataControllerCallback} exists for it. Such that we don't unnecessary clear
     * and remove data if its still referenced somewhere else.
     */
    public void closeIfNecessary() {
        if (dataControllerGroup.isEmpty()) {
            close();
        }
    }

    protected void storeResponseData(TResponse response) {
        if (dataStore != null) {
            dataStore.store(response);
        }
    }

    protected abstract boolean isEmpty(TResponse response);

    protected abstract DataFetcher<TResponse> createDataFetcher();

    /**
     * Nulls it out to request a {@link #createDataFetcher()} next {@link #requestDataAsync()}.
     */
    protected void destroyDataFetcher() {
        dataFetcher = null;
    }

    protected void onSuccess(TResponse response, String requestUrl) {
        storeResponseData(response);
        if (isEmpty(response)) {
            setState(State.EMPTY);
            dataControllerGroup.onEmpty();
        } else {
            setState(State.SUCCESS);
            dataControllerGroup.onSuccess(response, requestUrl);
        }
    }

    protected final void onFailure(DataResponseError error) {
        setState(State.FAILURE);
        dataControllerGroup.onFailure(error);
    }


    private final IDataCallback<TResponse> dataCallback = new IDataCallback<TResponse>() {
        @Override
        public void onSuccess(TResponse tResponse, String originalUrl) {
            DataController.this.onSuccess(tResponse, originalUrl);
        }

        @Override
        public void onFailure(DataResponseError dataResponseError) {
            DataController.this.onFailure(dataResponseError);
        }
    };

}
