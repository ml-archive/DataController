package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource.DataSourceCaller;
import com.fuzz.datacontroller.source.chain.ChainConstruct.ResponseToNextCallConverter;
import com.fuzz.datacontroller.source.chain.ChainConstruct.ResponseValidator;

/**
 * Description: Provides ability to merge {@link DataSourceCaller}. This merges the responses.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class MergeConstruct<TFirst, TSecond, TMerge> implements DataSourceCaller<TMerge> {

    public static <TFirst, TSecond, TMerge> MergeConstruct.Builder<TFirst, TSecond, TMerge>
    builderInstance(DataSourceCaller<TFirst> caller,
                    DataSourceCaller<TSecond> secondCaller,
                    ResponseToNextCallConverter<TFirst> callConverter,
                    ResponseMerger<TFirst, TSecond, TMerge> responseMerger) {
        return new Builder<>(caller, secondCaller, callConverter, responseMerger);
    }

    public static <TFirst, TSecond, TMerge> MergeConstruct.Builder<TFirst, TSecond, TMerge>
    builderInstance(DataSourceCaller<TFirst> caller,
                    DataSourceCaller<TSecond> secondCaller,
                    ResponseMerger<TFirst, TSecond, TMerge> responseMerger) {
        return new Builder<>(caller, secondCaller, responseMerger);
    }

    /**
     * Merges a response from two places into one type.
     */
    public interface ResponseMerger<TFirst, TSecond, TMerge> {

        DataControllerResponse<TMerge> mergeResponses(DataControllerResponse<TFirst> firstResponse,
                                                      DataControllerResponse<TSecond> secondResponse);
    }

    private final DataSourceCaller<TFirst> firstDataSource;
    private final DataSourceCaller<TSecond> secondDataSource;
    private final ResponseToNextCallConverter<TFirst> responseToNextCallConverter;
    private final ResponseValidator<TFirst> responseValidator;
    private final ResponseMerger<TFirst, TSecond, TMerge> responseMerger;

    MergeConstruct(Builder<TFirst, TSecond, TMerge> builder) {
        this.firstDataSource = builder.firstDataSource;
        this.secondDataSource = builder.secondDataSource;
        if (builder.responseToNextCallConverter == null) {
            this.responseToNextCallConverter = new ChainConstruct.DefaultResponseToNextCallConverter<>();
        } else {
            this.responseToNextCallConverter = builder.responseToNextCallConverter;
        }
        if (builder.responseValidator == null) {
            this.responseValidator = new ChainConstruct.DefaultResponseValidator<>();
        } else {
            this.responseValidator = builder.responseValidator;
        }
        this.responseMerger = builder.responseMerger;
    }

    @Override
    public void get(final DataSource.SourceParams sourceParams,
                    final DataController.Error error, final DataController.Success<TMerge> success) {
        firstDataSource.get(sourceParams, error, new DataController.Success<TFirst>() {
            @Override
            public void onSuccess(final DataControllerResponse<TFirst> firstResponse) {
                if (responseValidator.isValid(firstResponse)) {
                    DataSource.SourceParams nextParams;
                    nextParams = responseToNextCallConverter
                            .provideNextParams(firstResponse.getResponse(), sourceParams);
                    secondDataSource.get(nextParams, error, new DataController.Success<TSecond>() {
                        @Override
                        public void onSuccess(DataControllerResponse<TSecond> secondResponse) {
                            success.onSuccess(responseMerger.mergeResponses(firstResponse, secondResponse));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void cancel() {
        firstDataSource.cancel();
        secondDataSource.cancel();
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

    public static final class Builder<TFirst, TSecond, TMerge> {


        private final DataSourceCaller<TFirst> firstDataSource;
        private final DataSourceCaller<TSecond> secondDataSource;
        private final ResponseToNextCallConverter<TFirst> responseToNextCallConverter;
        private final ResponseMerger<TFirst, TSecond, TMerge> responseMerger;

        private ResponseValidator<TFirst> responseValidator;

        private Builder(DataSourceCaller<TFirst> firstDataSource,
                        DataSourceCaller<TSecond> secondDataSource,
                        ResponseToNextCallConverter<TFirst> responseToNextCallConverter,
                        ResponseMerger<TFirst, TSecond, TMerge> responseMerger) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.responseToNextCallConverter = responseToNextCallConverter;
            this.responseMerger = responseMerger;
        }

        private Builder(DataSourceCaller<TFirst> firstDataSource,
                        DataSourceCaller<TSecond> secondDataSource,
                        ResponseMerger<TFirst, TSecond, TMerge> responseMerger) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.responseToNextCallConverter = null;
            this.responseMerger = responseMerger;
        }

        public Builder<TFirst, TSecond, TMerge> responseValidator(
                ResponseValidator<TFirst> responseValidator) {
            this.responseValidator = responseValidator;
            return this;
        }

        public MergeConstruct<TFirst, TSecond, TMerge> build() {
            return new MergeConstruct<>(this);
        }

        public <TNext> ChainConstruct.Builder<TMerge, TNext>
        chain(DataSourceCaller<TNext> nextDataSourceCaller,
              ResponseToNextCallConverter<TMerge> responseToNextCallConverter) {
            return ChainConstruct.builderInstance(build(), nextDataSourceCaller, responseToNextCallConverter);
        }

        public <TNext> ChainConstruct.Builder<TMerge, TNext>
        chain(DataSourceCaller<TNext> nextDataSourceCaller) {
            return ChainConstruct.builderInstance(build(), nextDataSourceCaller);
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

}
