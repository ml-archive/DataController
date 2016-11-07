package com.fuzz.datacontroller.source;

import java.util.Collection;

/**
 * Description: Defines how {@link DataSource} are stored and retrieved.
 */
public interface DataSourceContainer<TResponse> {

    /**
     * Used to distinctly acquire a {@link DataSource} from a {@link DataSourceContainer}. This
     * flexibility enables multiple of the same {@link DataSource} via
     * the {@link ListBasedDataSourceContainer}.
     */
    class DataSourceParams {

        public static DataSourceParams networkParams() {
            return new DataSourceParams(DataSource.SourceType.NETWORK);
        }

        public static DataSourceParams diskParams() {
            return new DataSourceParams(DataSource.SourceType.DISK);
        }

        public static DataSourceParams memoryParams() {
            return new DataSourceParams(DataSource.SourceType.MEMORY);
        }

        private int position = -1;

        private DataSource.SourceType sourceType;

        public DataSourceParams(int position, DataSource.SourceType sourceType) {
            this.position = position;
            this.sourceType = sourceType;
        }

        public DataSourceParams() {
        }

        public DataSourceParams(int position) {
            this.position = position;
        }

        public DataSourceParams(DataSource.SourceType sourceType) {
            this.sourceType = sourceType;
        }

        public int getPosition() {
            return position;
        }

        public DataSource.SourceType getSourceType() {
            return sourceType;
        }
    }

    /**
     * Append the {@link DataSource} to the inner container here.
     */
    void registerDataSource(DataSource<TResponse> dataSource);

    /**
     * @return a {@link DataSource} from the container.
     */
    DataSource<TResponse> getDataSource(DataSourceParams sourceParams);

    /**
     * Removes a {@link DataSource} from this container.
     */
    void deregisterDataSource(DataSource<TResponse> dataSource);

    /**
     * @return A {@link Collection} of {@link DataSource} in this container. Should never cache
     * this result unless you don't expect modification.
     */
    Collection<DataSource<TResponse>> sources();
}
