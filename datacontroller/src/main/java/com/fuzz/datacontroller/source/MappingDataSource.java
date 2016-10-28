package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController2;
import com.fuzz.datacontroller.DataController2.DataControllerCallback;
import com.fuzz.datacontroller.DataControllerResponse;

/**
 * Description: Maps the callbacks of one source with another type.
 */
public class MappingDataSource<TFromResponse, TResponse> extends DataSource<TResponse> {

    /**
     * Defines how we map from one type of response to another.
     *
     * @param <TFromResponse> The response that is unique to this source and not its parent
     *                        {@link DataController2}.
     * @param <TResponse>     The response the parent {@link DataController2} expects that we map to.
     */
    public interface Mapper<TFromResponse, TResponse> {

        /**
         * Provide the mapping from the {@link TFromResponse} to the {@link TResponse} so that
         * the parent {@link DataController2} can share it with other {@link DataSource} and
         * {@link DataControllerCallback}.
         */
        TResponse mapFrom(TFromResponse fromResponse);

        /**
         * Maps from a response of the {@link TResponse} that the parent {@link DataController2}
         * uses.
         */
        TFromResponse mapTo(TResponse response);
    }

    private final DataSource<TFromResponse> fromDataSource;
    private final Mapper<TFromResponse, TResponse> mapper;

    public DataSource<TFromResponse> getFromDataSource() {
        return fromDataSource;
    }

    public Mapper<TFromResponse, TResponse> getMapper() {
        return mapper;
    }

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
    public SourceType getSourceType() {
        return fromDataSource.getSourceType();
    }

    @Override
    protected void doGet(SourceParams sourceParams, final DataController2.Success<TResponse> success, DataController2.Error error) {
        fromDataSource.doGet(sourceParams, new DataController2.Success<TFromResponse>() {
            @Override
            public void onSuccess(DataControllerResponse<TFromResponse> response) {
                TResponse transformedResponse = mapper.mapFrom(response.getResponse());
                success.onSuccess(new DataControllerResponse<>(transformedResponse,
                        response.getSourceType(), response.getOriginalUrl()));
            }
        }, error);
    }

    @Override
    protected void doStore(DataControllerResponse<TResponse> response) {
        TFromResponse transformedResponse = mapper.mapTo(response.getResponse());
        fromDataSource.doStore(new DataControllerResponse<>(transformedResponse,
                response.getSourceType(), response.getOriginalUrl()));
    }

}
