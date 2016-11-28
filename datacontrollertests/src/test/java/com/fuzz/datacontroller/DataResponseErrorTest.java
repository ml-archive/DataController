package com.fuzz.datacontroller;

import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Description:
 */
public class DataResponseErrorTest {

    @Test
    public void test_dataResponse() {
        DataResponseError responseError = new DataResponseError.Builder(
                DataSource.SourceType.NETWORK,
                "This is a test")
                .build();
        assertEquals("This is a test", responseError.message());
        assertEquals(responseError.message(), responseError.userFacingMessage());
        assertEquals(0, responseError.status());
        assertEquals(DataSource.SourceType.NETWORK, responseError.failedSource());
        assertNull(null, responseError.exception());

        DataResponseError newError = responseError.newBuilder().build();
        assertEquals(responseError.message(), newError.message());
        assertEquals(responseError.userFacingMessage(), newError.userFacingMessage());
        assertEquals(responseError.exception(), newError.exception());
        assertEquals(responseError.failedSource(), newError.failedSource());
        assertEquals(responseError.status(), newError.status());
        assertEquals(responseError.toString(), newError.toString());
    }

    @Test
    public void test_throwableResponse() {
        DataResponseError responseError = new DataResponseError.Builder(
                DataSource.SourceType.NETWORK, new RuntimeException())
                .build();
        assertNotNull(responseError.exception());
        assertEquals(0, responseError.status());
        assertEquals(null, responseError.message());
        assertEquals(DataSource.SourceType.NETWORK, responseError.failedSource());

        DataResponseError newError = responseError.newBuilder().build();
        assertEquals(responseError.message(), newError.message());
        assertEquals(responseError.userFacingMessage(), newError.userFacingMessage());
        assertEquals(responseError.exception(), newError.exception());
        assertEquals(responseError.failedSource(), newError.failedSource());
        assertEquals(responseError.status(), newError.status());
        assertEquals(responseError.toString(), newError.toString());
    }

}