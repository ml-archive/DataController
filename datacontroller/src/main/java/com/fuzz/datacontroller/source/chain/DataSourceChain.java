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
public class DataSourceChain<TChainType, TConvertedType> {

    public interface ResponseValidator<TChainType> {

        /**
         * @return True if response is good, false if we want to fail.
         * Then {@link #provideErrorForResponse(DataControllerResponse)} is called.
         */
        boolean isValid(DataControllerResponse<TChainType> response);

        /**
         * @return when response {@link #isValid(DataControllerResponse)} fails, provide a {@link DataResponseError}
         * that identifies the problem.
         */
        DataResponseError provideErrorForResponse(DataControllerResponse<TChainType> response);
    }

    public interface ResponseToNextCallConverter<T> {

        DataSource.SourceParams provideNextParams(T previousResponse,
                                                  DataSource.SourceParams previousParams);
    }

    public interface ResponseConverter<TChainType, TConvertedType> {

        TConvertedType fromResponse(TChainType response);
    }

    private final DataSource<TChainType> dataSource;
    private final ResponseValidator<TChainType> responseValidator;
    private final ResponseToNextCallConverter<TChainType> responseToNextCallConverter;
    private final ResponseConverter<TChainType, TConvertedType> responseConverter;
    private final boolean failChainOnError;


    private DataSourceChain(Builder<TChainType, TConvertedType> builder) {
        responseConverter = builder.responseConverter;
        dataSource = builder.dataSource;
        if (builder.responseValidator != null) {
            responseValidator = builder.responseValidator;
        } else {
            responseValidator = new DefaultResponseValidator();
        }
        if (builder.responseToNextCallConverter != null) {
            responseToNextCallConverter = builder.responseToNextCallConverter;
        } else {
            responseToNextCallConverter = new DefaultResponseToNextCallConverter();
        }
        failChainOnError = builder.failChainOnError;
    }

    void get(DataSource.SourceParams sourceParams,
             final DataController.Success<TConvertedType> success,
             final DataController.Error error) {
        dataSource.get(sourceParams, new DataController.Success<TChainType>() {
            @Override
            public void onSuccess(DataControllerResponse<TChainType> response) {
                if (responseValidator.isValid(response)) {
                    DataControllerResponse<TConvertedType> convertedResponse =
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
                    success.onSuccess(new DataControllerResponse<TConvertedType>(null, dataSource.sourceType()));
                }
            }
        });
    }

    ResponseToNextCallConverter<TChainType> responseToNextCallConverter() {
        return responseToNextCallConverter;
    }

    public static class Builder<TChainType, TConvertedType> {

        private final ChainingSource.Builder<TConvertedType> chainBuilder;
        private final DataSource<TChainType> dataSource;
        private final ResponseConverter<TChainType, TConvertedType> responseConverter;
        private ResponseValidator<TChainType> responseValidator;
        private ResponseToNextCallConverter<TChainType> responseToNextCallConverter;
        private boolean failChainOnError;

        public Builder(ChainingSource.Builder<TConvertedType> chainBuilder,
                       DataSource<TChainType> dataSource,
                       ResponseConverter<TChainType, TConvertedType> responseConverter) {
            this.chainBuilder = chainBuilder;
            this.dataSource = dataSource;
            this.responseConverter = responseConverter;
        }

        public Builder<TChainType, TConvertedType> responseValidator(
                ResponseValidator<TChainType> responseValidator) {
            this.responseValidator = responseValidator;
            return this;
        }

        public Builder<TChainType, TConvertedType> reponseToNextCallConverter(
                ResponseToNextCallConverter<TChainType> converter) {
            this.responseToNextCallConverter = converter;
            return this;
        }

        public Builder<TChainType, TConvertedType> failChainOnError(boolean failChainOnError) {
            this.failChainOnError = failChainOnError;
            return this;
        }

        public <TNewChainType> DataSourceChain.Builder<TNewChainType, TConvertedType> chain(
                DataSource<TNewChainType> dataSource,
                ResponseConverter<TNewChainType, TConvertedType> converter) {
            return chainBuilder.addChain(new DataSourceChain<>(this))
                    .chain(dataSource, converter);
        }

        public ChainingSource<TConvertedType> build() {
            return chainBuilder.addChain(new DataSourceChain<>(this)).build();
        }
    }

    private final class DefaultResponseValidator implements ResponseValidator<TChainType> {

        @Override
        public boolean isValid(DataControllerResponse<TChainType> response) {
            return true;
        }

        @Override
        public DataResponseError provideErrorForResponse(DataControllerResponse<TChainType> response) {
            return null;
        }
    }

    private final class DefaultResponseToNextCallConverter
            implements ResponseToNextCallConverter<TChainType> {

        @Override
        public DataSource.SourceParams provideNextParams(TChainType previousResponse,
                                                         DataSource.SourceParams previousParams) {
            return previousParams;
        }
    }
}
