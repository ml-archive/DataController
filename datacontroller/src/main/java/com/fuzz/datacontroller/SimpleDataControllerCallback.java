package com.fuzz.datacontroller;

/**
 * Description: Allows us to provide callbacks separate from implementation.
 */
public class SimpleDataControllerCallback<TResponse> implements DataController.DataControllerCallback<TResponse> {

    private final DataController.Error error;

    private final DataController.Success<TResponse> success;

    public SimpleDataControllerCallback(DataController.Error error,
                                        DataController.Success<TResponse> success) {
        this.error = error;
        this.success = success;
    }

    @Override
    public void onFailure(DataResponseError dataResponseError) {
        if (error != null) {
            error.onFailure(dataResponseError);
        }
    }

    @Override
    public void onSuccess(DataControllerResponse<TResponse> response) {
        if (success != null) {
            success.onSuccess(response);
        }
    }
}
