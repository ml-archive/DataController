package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource.SourceType;

/**
 * Description: Represents a response object called from a {@link DataController2}. This
 * class contains the original response class, the {@link SourceType} and other information.
 */
public class DataControllerResponse<TResponse> {

    private final TResponse response;
    private final SourceType sourceType;
    private final String originalUrl;

    public DataControllerResponse(TResponse response, SourceType type, String originalUrl) {
        this.response = response;
        this.sourceType = type;
        this.originalUrl = originalUrl;
    }

    public DataControllerResponse(TResponse response, SourceType type) {
        this(response, type, null);
    }

    public TResponse getResponse() {
        return response;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}
