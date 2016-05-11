package com.fuzz.datacontroller.datacontroller2;

import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Represents a response object called from a {@link com.fuzz.datacontroller.DataController}.
 */
public class DataControllerResponse<TResponse> {

    private final TResponse response;
    private final DataSource.SourceType sourceType;
    private final String originalUrl;

    public DataControllerResponse(TResponse response, DataSource.SourceType type, String originalUrl) {
        this.response = response;
        this.sourceType = type;
        this.originalUrl = originalUrl;
    }

    public DataControllerResponse(TResponse response, DataSource.SourceType type) {
        this(response, type, null);
    }

    public TResponse getResponse() {
        return response;
    }

    public DataSource.SourceType getSourceType() {
        return sourceType;
    }
}
