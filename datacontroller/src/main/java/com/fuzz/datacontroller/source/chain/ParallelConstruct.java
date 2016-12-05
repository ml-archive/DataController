package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceCaller;

/**
 * Description: Provides the ability to merge {@link DataSourceCaller} after calling themselves
 * in parallel.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class ParallelConstruct<TFirst, TSecond, TMerge> implements DataSourceCaller<TMerge> {

    public static <TFirst, TSecond, TMerge> ParallelConstruct.Builder<TFirst, TSecond, TMerge>
    builderInstance(DataSourceCaller<TFirst> caller,
                    DataSourceCaller<TSecond> secondCaller,
                    ParallelMerger<TFirst, TSecond, TMerge> parallelMerger) {
        return new ParallelConstruct.Builder<>(caller, secondCaller, parallelMerger);
    }

    /**
     * Returns the parallel object that we will use to populate as we get responses. The
     * corresponding {@link #mergeResponses(ParallelResponse, ParallelResponse)} is called after completion.
     * <p>
     * The two responses may either have an error or success. Handle accordingly.
     *
     * @param <TMerge>
     */
    public interface ParallelMerger<TFirst, TSecond, TMerge> {

        ParallelResponse<TMerge> mergeResponses(ParallelResponse<TFirst> firstParallelResponse,
                                                ParallelResponse<TSecond> secondParallelResponse);
    }

    public static class ParallelParams extends DataSource.SourceParams {

        private final DataSource.SourceParams firstParams;
        private final DataSource.SourceParams secondParams;

        public ParallelParams(DataSource.SourceParams firstParams,
                              DataSource.SourceParams secondParams) {
            this.firstParams = firstParams;
            this.secondParams = secondParams;
        }

        DataSource.SourceParams getFirstParams() {
            return firstParams;
        }

        DataSource.SourceParams getSecondParams() {
            return secondParams;
        }
    }

    private final DataSourceCaller<TFirst> firstDataSource;
    private final DataSourceCaller<TSecond> secondDataSource;
    private final ParallelMerger<TFirst, TSecond, TMerge> parallelMerger;


    ParallelConstruct(Builder<TFirst, TSecond, TMerge> builder) {
        firstDataSource = builder.firstDataSource;
        secondDataSource = builder.secondDataSource;
        parallelMerger = builder.parallelMerger;
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    final DataController.Error error,
                    final DataController.Success<TMerge> success) {
        ParallelParams params = getParams(sourceParams);
        final Object syncLock = new Object();
        final ParallelResponse<TFirst> firstResponse = new ParallelResponse<>();
        final ParallelResponse<TSecond> secondResponse = new ParallelResponse<>();

        firstDataSource.get(params.firstParams, new DataController.Error() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {
                synchronized (syncLock) {
                    firstResponse.dataResponseError = dataResponseError;
                    checkCompletion(firstResponse, secondResponse,
                            error, success);
                }
            }
        }, new DataController.Success<TFirst>() {
            @Override
            public void onSuccess(DataControllerResponse<TFirst> response) {
                synchronized (syncLock) {
                    firstResponse.response = response;
                    checkCompletion(firstResponse, secondResponse,
                            error, success);
                }
            }
        });

        secondDataSource.get(params.secondParams, new DataController.Error() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {
                synchronized (syncLock) {
                    secondResponse.dataResponseError = dataResponseError;
                    checkCompletion(firstResponse, secondResponse,
                            error, success);
                }
            }
        }, new DataController.Success<TSecond>() {
            @Override
            public void onSuccess(DataControllerResponse<TSecond> response) {
                synchronized (syncLock) {
                    secondResponse.response = response;
                    checkCompletion(firstResponse, secondResponse,
                            error, success);
                }
            }
        });
    }


    @Override
    public void cancel() {
        firstDataSource.cancel();
        secondDataSource.cancel();
    }

    private synchronized void checkCompletion(ParallelResponse<TFirst> firstParallelResponse,
                                              ParallelResponse<TSecond> secondParallelResponse,
                                              DataController.Error error,
                                              DataController.Success<TMerge> success) {
        if (firstParallelResponse.hasResponse() && secondParallelResponse.hasResponse()) {
            ParallelResponse<TMerge> response = parallelMerger
                    .mergeResponses(firstParallelResponse, secondParallelResponse);
            if (response.dataResponseError != null) {
                // signals failure
                error.onFailure(response.dataResponseError);
            } else {
                success.onSuccess(response.getResponse());
            }
        }
    }

    private ParallelParams getParams(DataSource.SourceParams sourceParams) {
        ParallelParams parallelParams = null;
        if (sourceParams instanceof ParallelParams) {
            parallelParams = (ParallelParams) sourceParams;
        }
        if (parallelParams == null) {
            throw new IllegalArgumentException("You must pass a set of ParallelParams to the " +
                    "parallel data source so it knows how to call its sources.");
        }
        return parallelParams;
    }

    public static class ParallelResponse<T> {

        private DataResponseError dataResponseError;

        private DataControllerResponse<T> response;

        ParallelResponse() {
        }

        public ParallelResponse(ParallelError parallelError,
                                DataControllerResponse<T> response) {
            if (parallelError != null) {
                this.dataResponseError = ParallelError.setParallelError(
                        new DataResponseError.Builder(DataSource.SourceType.MULTIPLE, "")
                                .build(), parallelError);
            }
            this.response = response;
        }

        void setDataResponseError(DataResponseError dataResponseError) {
            this.dataResponseError = dataResponseError;
        }

        void setResponse(DataControllerResponse<T> response) {
            this.response = response;
        }

        public DataResponseError getDataResponseError() {
            return dataResponseError;
        }

        public DataControllerResponse<T> getResponse() {
            return response;
        }

        boolean hasResponse() {
            return dataResponseError != null || response != null;
        }
    }

    public static final class Builder<TFirst, TSecond, TMerge> {

        private final DataSourceCaller<TFirst> firstDataSource;
        private final DataSourceCaller<TSecond> secondDataSource;
        private final ParallelMerger<TFirst, TSecond, TMerge> parallelMerger;

        public Builder(DataSourceCaller<TFirst> firstDataSource,
                       DataSourceCaller<TSecond> secondDataSource,
                       ParallelMerger<TFirst, TSecond, TMerge> parallelMerger) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.parallelMerger = parallelMerger;
        }

        public <TNext> ChainConstruct.Builder<TMerge, TNext>
        chain(DataSourceCaller<TNext> nextDataSourceCaller) {
            return ChainConstruct.builderInstance(build(), nextDataSourceCaller);
        }

        public <TNext, TMerge2> MergeConstruct.Builder<TMerge, TNext, TMerge2>
        merge(DataSourceCaller<TNext> secondDataSource,
              MergeConstruct.ResponseMerger<TMerge, TNext, TMerge2> responseMerger) {
            return MergeConstruct.builderInstance(build(), secondDataSource, responseMerger);
        }

        public <TNext, TMerge2> ParallelConstruct.Builder<TMerge, TNext, TMerge2>
        parallel(DataSourceCaller<TNext> nextDataSourceCaller,
                 ParallelMerger<TMerge, TNext, TMerge2> parallelMerger) {
            return ParallelConstruct.builderInstance(build(), nextDataSourceCaller,
                    parallelMerger);
        }

        public ParallelConstruct<TFirst, TSecond, TMerge> build() {
            return new ParallelConstruct<>(this);
        }

        public DataSource.Builder<TMerge> toSourceBuilder(DataSource.SourceType sourceType) {
            return new DataSource.Builder<>(build(), sourceType);
        }
    }

    public static class ParallelError {

        public static boolean isParallelError(DataResponseError dataResponseError) {
            return dataResponseError != null && dataResponseError.metaData()
                    instanceof ParallelError;
        }

        public static ParallelError getParallelError(DataResponseError dataResponseError) {
            return isParallelError(dataResponseError)
                    ? (ParallelError) dataResponseError.metaData() : null;
        }

        static DataResponseError setParallelError(DataResponseError dataResponseError,
                                                  ParallelError parallelError) {
            return dataResponseError.newBuilder().metaData(parallelError).build();
        }


        private final DataResponseError firstError;

        private final DataResponseError secondError;

        public ParallelError(DataResponseError firstError, DataResponseError secondError) {
            this.firstError = firstError;
            this.secondError = secondError;
        }

        public DataResponseError getFirstError() {
            return firstError;
        }

        public DataResponseError getSecondError() {
            return secondError;
        }

        public boolean isFullFailure() {
            return firstError != null && secondError != null;
        }

        public boolean isFirstFailure() {
            return firstError != null;
        }

        public boolean isSecondFailure() {
            return secondError != null;
        }
    }
}
