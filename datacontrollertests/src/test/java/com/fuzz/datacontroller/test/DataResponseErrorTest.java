package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataResponseError;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Description:
 */
public class DataResponseErrorTest {

    @Test
    public void test_dataResponse() {
        DataResponseError responseError = new DataResponseError("This is a test");
        assertEquals("This is a test", responseError.getMessage());
        assertEquals(responseError.getMessage(), responseError.getUserFacingMessage());
        assertEquals(0, responseError.getStatusCode());
        assertEquals(false, responseError.isNetworkError());
        assertNull(null, responseError.getThrowable());
    }

    @Test
    public void test_throwableResponse() {
        DataResponseError responseError = new DataResponseError(new RuntimeException());
        assertNotNull(responseError.getThrowable());
        assertEquals(0, responseError.getStatusCode());
        assertEquals(null, responseError.getMessage());
        assertFalse(responseError.isNetworkError());
    }
}
