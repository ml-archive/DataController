package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
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
                    MergeConstruct.ResponseMerger<TFirst, TSecond, TMerge> responseMerger) {
        return new ParallelConstruct.Builder<>(caller, secondCaller, responseMerger);
    }


    ParallelConstruct(Builder<TFirst, TSecond, TMerge> builder) {

    }

    @Override
    public void get(DataSource.SourceParams sourceParams, DataController.Error error, DataController.Success<TMerge> success) {

    }

    @Override
    public void cancel() {

    }

    public static final class Builder<TFirst, TSecond, TMerge> {

        private final DataSourceCaller<TFirst> firstDataSource;
        private final DataSourceCaller<TSecond> secondDataSource;
        private final MergeConstruct.ResponseMerger<TFirst, TSecond, TMerge> responseMerger;

        public Builder(DataSourceCaller<TFirst> firstDataSource,
                       DataSourceCaller<TSecond> secondDataSource,
                       MergeConstruct.ResponseMerger<TFirst, TSecond, TMerge> responseMerger) {
            this.firstDataSource = firstDataSource;
            this.secondDataSource = secondDataSource;
            this.responseMerger = responseMerger;
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
                 MergeConstruct.ResponseMerger<TMerge, TNext, TMerge2> responseMerger) {
            return ParallelConstruct.builderInstance(build(), nextDataSourceCaller,
                    responseMerger);
        }

        public ParallelConstruct<TFirst, TSecond, TMerge> build() {
            return new ParallelConstruct<>(this);
        }

        public DataSource.Builder<TMerge> toSourceBuilder(DataSource.SourceType sourceType) {
            return new DataSource.Builder<>(build(), sourceType);
        }
    }
}
