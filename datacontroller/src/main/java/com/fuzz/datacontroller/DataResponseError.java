package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: The main error class that returns in a callback. Provides a unified set of data
 * about the error that occurred here.
 */
public class DataResponseError {

    public interface ErrorFilter {

        DataResponseError filter(DataResponseError dataResponseError);
    }

    private final String message;
    private final long status;
    private final Throwable exception;
    private final String userFacingMessage;
    private final DataSource.SourceType failedSource;

    private DataResponseError(Builder builder) {
        message = builder.message;
        status = builder.statusCode;
        exception = builder.exception;
        userFacingMessage = builder.userFacingMessage;
        failedSource = builder.failedSource;
    }

    @Override
    public String toString() {
        String value = "Failure within " + failedSource + ": ";
        value += userFacingMessage;
        if (status > 0) {
            value += ", status = " + status;
        }

        if (exception != null) {
            value += ", exception = " + exception;
        }
        return value;
    }

    public String userFacingMessage() {
        return message();
    }

    public String message() {
        return message;
    }

    public long status() {
        return status;
    }

    public Throwable exception() {
        return exception;
    }

    public DataSource.SourceType failedSource() {
        return failedSource;
    }

    /**
     * @return A new instance of a {@link Builder} for modification.
     */
    public Builder newBuilder() {
        Builder builder;
        if (exception != null) {
            builder = new Builder(failedSource, exception);
        } else {
            builder = new Builder(failedSource, message);
        }
        return builder
                .userFacingMessage(userFacingMessage)
                .status(status);
    }

    public static class Builder {

        private String message;
        private Throwable exception;
        private long statusCode;
        private String userFacingMessage;
        private DataSource.SourceType failedSource;

        public Builder(DataSource.SourceType failedSource, Throwable exception) {
            this.failedSource = failedSource;
            this.exception = exception;
            this.message = exception.getMessage();
            this.userFacingMessage = exception.getMessage();
        }

        public Builder(DataSource.SourceType failedSource, String message) {
            this.failedSource = failedSource;
            this.message = message;
            this.userFacingMessage = message;
        }

        public Builder status(long statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder userFacingMessage(String userFacingMessage) {
            this.userFacingMessage = userFacingMessage;
            return this;
        }

        public DataResponseError build() {
            return new DataResponseError(this);
        }
    }
}
