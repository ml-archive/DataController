package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController2;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;

public class ValidateCallback<TResponse>
        implements DataController2.DataControllerCallback<TResponse> {

    private boolean isSuccessCalled;
    private boolean isFailureCalled;
    private DataControllerResponse<TResponse> response;
    private DataResponseError error;

    @Override
    public void onFailure(DataResponseError dataResponseError) {
        isFailureCalled = true;
        error = dataResponseError;
    }

    @Override
    public void onSuccess(DataControllerResponse<TResponse> response) {
        isSuccessCalled = true;
        this.response = response;
    }

    public boolean isSuccessCalled() {
        return isSuccessCalled;
    }

    public boolean isFailureCalled() {
        return isFailureCalled;
    }

    public DataControllerResponse<TResponse> getResponse() {
        return response;
    }

    public DataResponseError getError() {
        return error;
    }
}
