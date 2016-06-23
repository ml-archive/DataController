package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;

/**
 * Description: Maps the callbacks of one source with another type.
 */
public class MappingDataSource<TFromResponse, TResponse> extends DataSource<TResponse> {

    public interface Mapper<TFromResponse, TResponse> {

        TResponse mapFrom(TFromResponse fromResponse);

        TFromResponse mapTo(TResponse response);
    }

    private final DataSource<TFromResponse> fromDataSource;
    private final Mapper<TFromResponse, TResponse> mapper;

    public MappingDataSource(DataSource<TFromResponse> fromDataSource,
                             Mapper<TFromResponse, TResponse> mapper) {
        this.fromDataSource = fromDataSource;
        this.mapper = mapper;
    }

    @Override
    public void cancel() {
        fromDataSource.cancel();
    }

    @Override
    protected void doGet(SourceParams sourceParams, final DataController.Success<TResponse> success, DataController.Error error) {
        fromDataSource.doGet(sourceParams, new DataController.Success<TFromResponse>() {
            @Override
            public void onSuccess(DataControllerResponse<TFromResponse> response) {
                success.onSuccess(new DataControllerResponse<>(mapper.mapFrom(response.getResponse()),
                        response.getSourceType(), response.getOriginalUrl()));
            }
        }, error);
    }

    @Override
    protected void doStore(DataControllerResponse<TResponse> response) {
        fromDataSource.doStore(new DataControllerResponse<>(mapper.mapTo(response.getResponse()),
                response.getSourceType(), response.getOriginalUrl()));
    }

    @Override
    public SourceType getSourceType() {
        return fromDataSource.getSourceType();
    }

    public DataSource<TFromResponse> getFromDataSource() {
        return fromDataSource;
    }

    public Mapper<TFromResponse, TResponse> getMapper() {
        return mapper;
    }
}
