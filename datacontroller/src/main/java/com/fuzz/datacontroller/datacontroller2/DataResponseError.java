package com.fuzz.datacontroller.datacontroller2;

/**
 * Description:
 */
public class DataResponseError {

    private String message;
    private long statusCode;
    private boolean isNetworkError;
    private Throwable throwable;

    public DataResponseError(String message) {
        this.message = message;
        statusCode = 0;
        isNetworkError = false;
    }

    public DataResponseError(Throwable t) {
        throwable = t;
        message = throwable.getMessage();
        statusCode = 0;
        isNetworkError = false;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(long statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return getMessage();
    }

    public String getUserFacingMessage() {
        return getMessage();
    }

    public String getMessage() {
        return message;
    }

    public long getStatusCode() {
        return statusCode;
    }

    public boolean isNetworkError() {
        return isNetworkError;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
