package com.fuzz.datacontroller;

/**
 * A simple callback interface that passes back state information.
 *
 * @param <TResponse> The response of the success result.
 */
public interface IDataControllerCallback<TResponse> {

    /**
     * Called when the response is successful and the {@link DataController#isEmpty(Object)} is false.
     * Otherwise {@link #onEmpty()} is called.
     *
     * @param response The response that we got back from our {@link DataController}.
     * @param requestUrl
     */
    void onSuccess(TResponse response, String requestUrl);

    /**
     * Called when the response fails due to some error. The {@link DataController} manages the message
     * that is returned here.
     * TODO: solidify response and possibly the logic behind error messaging.
     *
     * @param error The message that the {@link DataController} returns for an error.
     */
    void onFailure(DataResponseError error);

    /**
     * Called when the response is {@link DataController#isEmpty(Object)}. When this is called, you should
     * show an empty state.
     */
    void onEmpty();

    /**
     * Called whenever we begin loading data from the network. It is wise to set any loading UI logic
     * in this method since it may not always get called in a {@link DataController} if its already loading
     * something in the background.
     * TODO: call this method if the {@link DataController} is already loading so we get this callback.
     */
    void onStartLoading();

    /**
     * Called when the {@link DataController} broadcasts a state of {@link DataController.State#NONE}.
     */
    void onClosed();
}
