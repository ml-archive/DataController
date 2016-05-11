package com.fuzz.datacontroller;

import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Represents a response object called from a {@link DataController}.
 */
public class DataControllerResponse<TResponse> {

    private final TResponse response;
    private final DataSource.SourceType sourceType;

    public DataControllerResponse(TResponse response, DataSource.SourceType type) {
        this.response = response;
        this.sourceType = type;
    }

    public TResponse getResponse() {
        return response;
    }

    public DataSource.SourceType getSourceType() {
        return sourceType;
    }
}
