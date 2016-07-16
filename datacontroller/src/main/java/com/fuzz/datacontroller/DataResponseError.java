package com.fuzz.datacontroller;

/**
 * Description: The main Error class that returns in a callback. Override this class to provide different kind
 * of errors.
 */
public class DataResponseError {

    private final String message;
    private final long status;
    private final Throwable exception;
    private final String userFacingMessage;

    private DataResponseError(Builder builder) {
        message = builder.message;
        status = builder.statusCode;
        exception = builder.exception;
        userFacingMessage = builder.userFacingMessage;
    }

    @Override
    public String toString() {
        String value = userFacingMessage;
        if (status > 0) {
            value += ", status = " + status;}

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

    /**
     * @return A new instance of a {@link Builder} for modification.
     */
    public Builder newBuilder() {
        Builder builder;
        if (exception != null) {
            builder = new Builder(exception);
        } else {
            builder = new Builder(message);
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

        public Builder(Throwable exception) {
            this.exception = exception;
            this.message = exception.getMessage();
            this.userFacingMessage = exception.getMessage();
        }

        public Builder(String message) {
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
