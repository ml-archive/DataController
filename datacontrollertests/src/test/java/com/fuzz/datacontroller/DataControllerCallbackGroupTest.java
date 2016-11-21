package com.fuzz.datacontroller;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerCallbackGroup;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
@SuppressWarnings("unchecked")
public class DataControllerCallbackGroupTest {

    private final DataControllerCallbackGroup callbackGroup = new DataControllerCallbackGroup();
    private boolean isSuccessCalled = false;
    private boolean isFailureCalled = false;

    @Test
    public void test_CanRegister() {
        callbackGroup.registerForCallbacks(dataControllerCallback);
        callbackGroup.onSuccess(null);
        callbackGroup.onFailure(null);

        assertTrue(isSuccessCalled);
        assertTrue(isFailureCalled);
        isFailureCalled = false;
        isSuccessCalled = false;
        callbackGroup.deregisterForCallbacks(dataControllerCallback);
        callbackGroup.onSuccess(null);
        callbackGroup.onFailure(null);
        assertFalse(isSuccessCalled);
        assertFalse(isFailureCalled);
    }

    @Test
    public void test_CanClear() {
        callbackGroup.registerForCallbacks(dataControllerCallback);
        callbackGroup.onSuccess(null);
        callbackGroup.onFailure(null);

        assertTrue(isSuccessCalled);
        assertTrue(isFailureCalled);
        isFailureCalled = false;
        isSuccessCalled = false;
        callbackGroup.clearCallbacks();
        callbackGroup.onSuccess(null);
        callbackGroup.onFailure(null);
        assertFalse(isSuccessCalled);
        assertFalse(isFailureCalled);
    }

    private final DataController.DataControllerCallback dataControllerCallback = new DataController.DataControllerCallback() {
        @Override
        public void onFailure(DataResponseError dataResponseError) {
            isFailureCalled = true;
        }

        @Override
        public void onSuccess(DataControllerResponse response) {
            isSuccessCalled = true;
        }
    };
}
