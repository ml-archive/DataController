package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataController.DataControllerCallback;
import com.fuzz.datacontroller.DataControllerResponse;

/**
 * Description: Maps the callbacks of one source with another type.
 */
public class MappingSource<TFromResponse, TResponse>
        implements DataSource.Source<TResponse> {

    public static <TResponse, TFromResponse> DataSource.Builder<TResponse> builderInstance(
            DataSource<TFromResponse> datasource,
            Mapper<TFromResponse, TResponse> mapper) {
        MappingSource<TFromResponse, TResponse> mapping = new MappingSource<>(datasource, mapper);
        return new DataSource.Builder<>(mapping, datasource.sourceType())
                .storage(mapping);
    }

    /**
     * Defines how we map from one type of response to another.
     *
     * @param <TFromResponse> The response that is unique to this source and not its parent
     *                        {@link DataController}.
     * @param <TResponse>     The response the parent {@link DataController} expects that we map to.
     */
    public interface Mapper<TFromResponse, TResponse> {

        /**
         * Provide the mapping from the {@link TFromResponse} to the {@link TResponse} so that
         * the parent {@link DataController} can share it with other {@link DataSource} and
         * {@link DataControllerCallback}.
         */
        TResponse mapFrom(TFromResponse fromResponse);

        /**
         * Maps from a response of the {@link TResponse} that the parent {@link DataController}
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

    public MappingSource(DataSource<TFromResponse> fromDataSource,
                         Mapper<TFromResponse, TResponse> mapper) {
        this.fromDataSource = fromDataSource;
        this.mapper = mapper;
    }

    @Override
    public void cancel() {
        fromDataSource.cancel();
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    DataController.Error error, final DataController.Success<TResponse> success) {
        fromDataSource.get(sourceParams, new DataController.Success<TFromResponse>() {
            @Override
            public void onSuccess(DataControllerResponse<TFromResponse> response) {
                TResponse transformedResponse = mapper.mapFrom(response.getResponse());
                success.onSuccess(new DataControllerResponse<>(transformedResponse,
                        response.getSourceType(), response.getOriginalUrl()));
            }
        }, error);
    }

    @Override
    public void store(DataControllerResponse<TResponse> response) {
        TFromResponse transformedResponse = mapper.mapTo(response.getResponse());
        fromDataSource.store(new DataControllerResponse<>(transformedResponse,
                response.getSourceType(), response.getOriginalUrl()));
    }

    @Override
    public TResponse getStoredData(DataSource.SourceParams params) {
        return mapper.mapFrom(fromDataSource.getStoredData(params));
    }

    @Override
    public void clearStoredData(DataSource.SourceParams params) {
        fromDataSource.clearStoredData(params);
    }

    @Override
    public boolean hasStoredData(DataSource.SourceParams params) {
        return fromDataSource.hasStoredData(params);
    }
}
