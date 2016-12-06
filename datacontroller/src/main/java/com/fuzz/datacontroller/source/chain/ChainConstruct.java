package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceCaller;

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */

public class ChainConstruct<TFirst, TSecond> implements DataSourceCaller<TSecond> {

    public static <TFirst, TSecond> ChainConstruct.Builder<TFirst, TSecond> builderInstance(
            DataSourceCaller<TFirst> caller,
            DataSourceCaller<TSecond> secondCaller) {
        return new Builder<>(caller, secondCaller);
    }

    public interface ResponseValidator<TFirst> {

        /**
         * @return True if response is good, false if we want to fail.
         * Then {@link #provideErrorForResponse(DataControllerResponse)} is called.
         */
        boolean isValid(DataControllerResponse<TFirst> response);

        /**
         * @return when response {@link #isValid(DataControllerResponse)} fails, provide a {@link DataResponseError}
         * that identifies the problem.
         */
        DataResponseError provideErrorForResponse(DataControllerResponse<TFirst> response);
    }

    public interface ResponseToNextCallConverter<TFirst> {

        DataSource.SourceParams provideNextParams(TFirst previousResponse,
                                                  DataSource.SourceParams previousParams);
    }

    private final DataSourceCaller<TFirst> firstDataSource;
    private final DataSourceCaller<TSecond> secondDataSource;
    private final DataSource.SourceType sourceType;
    private final ResponseToNextCallConverter<TFirst> responseToNextCallConverter;
    private final ResponseValidator<TFirst> responseValidator;


    ChainConstruct(Builder<TFirst, TSecond> builder, DataSource.SourceType sourceType) {
        this.firstDataSource = builder.firstDataSource;
        this.secondDataSource = builder.secondDataSource;
        this.sourceType = sourceType;
        if (builder.responseToNextCallConverter == null) {
            this.responseToNextCallConverter = new DefaultResponseToNextCallConverter<>();
        } else {
            this.responseToNextCallConverter = builder.responseToNextCallConverter;
        }
        if (builder.responseValidator == null) {
            this.responseValidator = new DefaultResponseValidator<>();
        } else {
            this.responseValidator = builder.responseValidator;
        }
    }

    @Override
    public void get(final DataSource.SourceParams sourceParams,
                    final DataController.Error error, final DataController.Success<TSecond> success) {
        firstDataSource.get(sourceParams, error, new DataController.Success<TFirst>() {
            @Override
            public void onSuccess(DataControllerResponse<TFirst> response) {
                if (responseValidator.isValid(response)) {
                    DataSource.SourceParams nextParams;
                    nextParams = responseToNextCallConverter
                            .provideNextParams(response.getResponse(), sourceParams);
                    secondDataSource.get(nextParams, error, success);
                } else {
                    error.onFailure(responseValidator.provideErrorForResponse(response));
                }
            }
        });
    }

    @Override
    public void cancel() {
        firstDataSource.cancel();
        secondDataSource.cancel();
    }

    @Override
    public DataSource.SourceType sourceType() {
        return sourceType;
    }

    public DataSourceCaller<TFirst> firstDataSource() {
        return firstDataSource;
    }

    public DataSourceCaller<TSecond> secondDataSource() {
        return secondDataSource;
    }

    public ResponseToNextCallConverter<TFirst> responseToNextCallConverter() {
        return responseToNextCallConverter;
    }

    public ResponseValidator<TFirst> responseValidator() {
        return responseValidator;
    }

    public static final class Builder<TFirst, TSecond> {


        private final DataSourceCaller<TFirst> firstDataSource;
        private final DataSourceCaller<TSecond> secondDataSource;
        private ResponseToNextCallConverter<TFirst> responseToNextCallConverter;

        private ResponseValidator<TFirst> responseValidator;

        private Builder(DataSourceCaller<TFirst> firstDataSource,
                        DataSourceCaller<TSecond> secondDataSource) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.responseToNextCallConverter = null;
        }

        public Builder<TFirst, TSecond> responseValidator(
                ResponseValidator<TFirst> responseValidator) {
            this.responseValidator = responseValidator;
            return this;
        }

        public Builder<TFirst, TSecond> responseToNextCallConverter(
                ResponseToNextCallConverter<TFirst> responseToNextCallConverter) {
            this.responseToNextCallConverter = responseToNextCallConverter;
            return this;
        }

        public ChainConstruct<TFirst, TSecond> build(DataSource.SourceType sourceType) {
            return new ChainConstruct<>(this, sourceType);
        }

        public DataSource.Builder<TSecond> toSourceBuilder(DataSource.SourceType sourceType) {
            return new DataSource.Builder<>(build(sourceType));
        }

        public <TNext> ChainConstruct.Builder<TSecond, TNext>
        chain(DataSourceCaller<TNext> nextDataSourceCaller) {
            return new ChainConstruct.Builder<>(build(nextDataSourceCaller.sourceType()), nextDataSourceCaller);
        }

        public <TNext, TMerge> MergeConstruct.Builder<TSecond, TNext, TMerge>
        merge(DataSourceCaller<TNext> secondDataSource,
              MergeConstruct.ResponseMerger<TSecond, TNext, TMerge> responseMerger) {
            return MergeConstruct.builderInstance(build(secondDataSource.sourceType()),
                    secondDataSource, responseMerger);
        }

        public <TNext, TMerge> ParallelConstruct.Builder<TSecond, TNext, TMerge>
        parallel(DataSourceCaller<TNext> nextDataSourceCaller,
                 ParallelConstruct.ParallelMerger<TSecond, TNext, TMerge> parallelMerger) {
            return ParallelConstruct.builderInstance(build(nextDataSourceCaller.sourceType()),
                    nextDataSourceCaller, parallelMerger);
        }
    }

    static class DefaultResponseValidator<TFirst> implements ResponseValidator<TFirst> {

        @Override
        public boolean isValid(DataControllerResponse<TFirst> response) {
            return true;
        }

        @Override
        public DataResponseError provideErrorForResponse(DataControllerResponse<TFirst> response) {
            return null;
        }
    }

    static final class DefaultResponseToNextCallConverter<TFirst>
            implements ResponseToNextCallConverter<TFirst> {

        @Override
        public DataSource.SourceParams provideNextParams(TFirst previousResponse,
                                                         DataSource.SourceParams previousParams) {
            return previousParams;
        }
    }
}
