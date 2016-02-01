package com.fuzz.datacontroller;

/**
 * Description: Represents a response object called from a {@link DataController}.
 */
public class DataControllerResponse<TResponse> {

    private final TResponse response;
    private final ResponseType type;

    /**
     * Represents the type of response we received from where.
     */
    public enum ResponseType {

        /**
         * This response is returned from a database-like storage.
         */
        DATABASE,

        /**
         * This response is called from the network.
         */
        NETWORK,

        /**
         * Response is pulled from a file.
         */
        FILE,

        /**
         * Some other undefined kind of response.
         */
        OTHER
    }

    public DataControllerResponse(TResponse response, ResponseType type) {
        this.response = response;
        this.type = type;
    }

    public TResponse getResponse() {
        return response;
    }

    public ResponseType getType() {
        return type;
    }
}
