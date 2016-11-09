package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */

public class ChainConstruct<TFirst, TSecond> implements DataSource.DataSourceCaller<TSecond> {

    public static <TFirst, TSecond> ChainConstruct.Builder<TFirst, TSecond> builderInstance(
            DataSource.DataSourceCaller<TFirst> caller,
            DataSource.DataSourceCaller<TSecond> secondCaller,
            ResponseToNextCallConverter<TFirst> callConverter) {
        return new Builder<>(caller, secondCaller, callConverter);
    }

    public static <TFirst, TSecond> ChainConstruct.Builder<TFirst, TSecond> builderInstance(
            DataSource.DataSourceCaller<TFirst> caller,
            DataSource.DataSourceCaller<TSecond> secondCaller) {
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

    private final DataSource.DataSourceCaller<TFirst> firstDataSource;
    private final DataSource.DataSourceCaller<TSecond> secondDataSource;
    private final ResponseToNextCallConverter<TFirst> responseToNextCallConverter;
    private final ResponseValidator<TFirst> responseValidator;


    ChainConstruct(Builder<TFirst, TSecond> builder) {
        this.firstDataSource = builder.firstDataSource;
        this.secondDataSource = builder.secondDataSource;
        if (builder.responseToNextCallConverter == null) {
            this.responseToNextCallConverter = new DefaultResponseToNextCallConverter();
        } else {
            this.responseToNextCallConverter = builder.responseToNextCallConverter;
        }
        if (builder.responseValidator == null) {
            this.responseValidator = new DefaultResponseValidator();
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
                }
            }
        });
    }

    @Override
    public void cancel() {
        firstDataSource.cancel();
        secondDataSource.cancel();
    }

    public DataSource.DataSourceCaller<TFirst> firstDataSource() {
        return firstDataSource;
    }

    public DataSource.DataSourceCaller<TSecond> secondDataSource() {
        return secondDataSource;
    }

    public ResponseToNextCallConverter<TFirst> responseToNextCallConverter() {
        return responseToNextCallConverter;
    }

    public ResponseValidator<TFirst> responseValidator() {
        return responseValidator;
    }

    public static final class Builder<TFirst, TSecond> {


        private final DataSource.DataSourceCaller<TFirst> firstDataSource;
        private final DataSource.DataSourceCaller<TSecond> secondDataSource;
        private final ResponseToNextCallConverter<TFirst> responseToNextCallConverter;

        private ResponseValidator<TFirst> responseValidator;

        private Builder(DataSource.DataSourceCaller<TFirst> firstDataSource,
                        DataSource.DataSourceCaller<TSecond> secondDataSource,
                        ResponseToNextCallConverter<TFirst> responseToNextCallConverter) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.responseToNextCallConverter = responseToNextCallConverter;
        }

        private Builder(DataSource.DataSourceCaller<TFirst> firstDataSource,
                        DataSource.DataSourceCaller<TSecond> secondDataSource) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.responseToNextCallConverter = null;
        }

        public Builder<TFirst, TSecond> responseValidator(
                ResponseValidator<TFirst> responseValidator) {
            this.responseValidator = responseValidator;
            return this;
        }

        public ChainConstruct<TFirst, TSecond> build() {
            return new ChainConstruct<>(this);
        }

        public <TNext> ChainConstruct.Builder<TSecond, TNext>
        chain(DataSource.DataSourceCaller<TNext> nextDataSourceCaller,
              ResponseToNextCallConverter<TSecond> responseToNextCallConverter) {
            return new ChainConstruct.Builder<>(build(), nextDataSourceCaller,
                    responseToNextCallConverter);
        }

        public <TNext> ChainConstruct.Builder<TSecond, TNext>
        chain(DataSource.DataSourceCaller<TNext> nextDataSourceCaller) {
            return new ChainConstruct.Builder<>(build(), nextDataSourceCaller);
        }

        public <TNext, TMerge> MergeConstruct.Builder<TSecond, TNext, TMerge>
        merge(DataSource.DataSourceCaller<TSecond> firstDataSource,
              DataSource.DataSourceCaller<TNext> secondDataSource,
              ResponseToNextCallConverter<TSecond> responseToNextCallConverter,
              MergeConstruct.ResponseMerger<TSecond, TNext, TMerge> responseMerger) {
            return MergeConstruct.builderInstance(firstDataSource, secondDataSource,
                    responseToNextCallConverter, responseMerger);
        }

        public <TNext, TMerge> MergeConstruct.Builder<TSecond, TNext, TMerge>
        merge(DataSource.DataSourceCaller<TSecond> firstDataSource,
              DataSource.DataSourceCaller<TNext> secondDataSource,
              MergeConstruct.ResponseMerger<TSecond, TNext, TMerge> responseMerger) {
            return MergeConstruct.builderInstance(firstDataSource, secondDataSource, responseMerger);
        }
    }

    static final class DefaultResponseValidator<TFirst> implements ResponseValidator<TFirst> {

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
