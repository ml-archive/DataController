package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: Simple class that provides convenience for {@link ChainingSource} chaining operations.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class DataSourceChain<T, V> {

    public interface ResponseValidator<T> {

        /**
         * @return True if response is good, false if we want to fail.
         * Then {@link #provideErrorForResponse(DataControllerResponse)} is called.
         */
        boolean isValid(DataControllerResponse<T> response);

        /**
         * @return when response {@link #isValid(DataControllerResponse)} fails, provide a {@link DataResponseError}
         * that identifies the problem.
         */
        DataResponseError provideErrorForResponse(DataControllerResponse<T> response);
    }

    public interface ResponseToNextCallConverter<T> {

        DataSource.SourceParams provideNextParams(T previousResponse,
                                                  DataSource.SourceParams previousParams);
    }

    public interface ResponseConverter<T, V> {

        V fromResponse(T response);
    }

    private final DataSource<T> dataSource;
    private final ResponseValidator<T> responseValidator;
    private final ResponseToNextCallConverter<T> reponseToNextCallConverter;
    private final ResponseConverter<T, V> responseConverter;
    private final boolean failChainOnError;


    private DataSourceChain(Builder<T, V> builder) {
        responseConverter = builder.responseConverter;
        dataSource = builder.dataSource;
        if (builder.responseValidator != null) {
            responseValidator = builder.responseValidator;
        } else {
            responseValidator = new DefaultResponseValidator();
        }
        reponseToNextCallConverter = builder.reponseToNextCallConverter;
        failChainOnError = builder.failChainOnError;
    }

    void get(DataSource.SourceParams sourceParams,
             final DataController.Success<V> success,
             final DataController.Error error) {
        dataSource.get(sourceParams, new DataController.Success<T>() {
            @Override
            public void onSuccess(DataControllerResponse<T> response) {
                if (responseValidator.isValid(response)) {
                    DataControllerResponse<V> convertedResponse =
                            new DataControllerResponse<>(responseConverter.fromResponse(response.getResponse()),
                                    dataSource.sourceType());
                    success.onSuccess(convertedResponse);
                } else {
                    error.onFailure(responseValidator.provideErrorForResponse(response));
                }
            }
        }, new DataController.Error() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {
                if (failChainOnError) {
                    error.onFailure(dataResponseError);
                } else {
                    success.onSuccess(new DataControllerResponse<V>(null, dataSource.sourceType()));
                }
            }
        });
    }

    public static class Builder<T, V> {

        private final ChainingSource.Builder<V> chainBuilder;
        private final DataSource<T> dataSource;
        private final ResponseConverter<T, V> responseConverter;
        private ResponseValidator<T> responseValidator;
        private ResponseToNextCallConverter<T> reponseToNextCallConverter;
        private boolean failChainOnError;

        public Builder(ChainingSource.Builder<V> chainBuilder,
                       DataSource<T> dataSource,
                       ResponseConverter<T, V> responseConverter) {
            this.chainBuilder = chainBuilder;
            this.dataSource = dataSource;
            this.responseConverter = responseConverter;
        }

        public Builder<T, V> responseValidator(ResponseValidator<T> responseValidator) {
            this.responseValidator = responseValidator;
            return this;
        }

        public Builder<T, V> reponseToNextCallConverter(ResponseToNextCallConverter<T> converter) {
            this.reponseToNextCallConverter = converter;
            return this;
        }

        public Builder<T, V> failChainOnError(boolean failChainOnError) {
            this.failChainOnError = failChainOnError;
            return this;
        }

        public <Z> DataSourceChain.Builder<Z, V> chain(DataSource<Z> dataSource,
                                                       ResponseConverter<Z, V> converter) {
            DataSourceChain<T, V> chain = new DataSourceChain<>(this);
            chainBuilder.addChain(chain);
            return chainBuilder.chain(dataSource, converter);
        }

        public ChainingSource<V> build() {
            return chainBuilder.build();
        }
    }

    private final class DefaultResponseValidator implements ResponseValidator<T> {

        @Override
        public boolean isValid(DataControllerResponse<T> response) {
            return true;
        }

        @Override
        public DataResponseError provideErrorForResponse(DataControllerResponse<T> response) {
            return null;
        }
    }
}
