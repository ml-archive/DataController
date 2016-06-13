package com.fuzz.datacontroller.source;

import java.util.Collection;

/**
 * Description: Defines how {@link DataSource} are stored and retrieved.
 */
public interface DataSourceStorage<TResponse> {

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

        int position = -1;

        DataSource.SourceType sourceType;

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
    }

    void registerDataSource(DataSource<TResponse> dataSource);

    DataSource<TResponse> getDataSource(DataSourceParams sourceParams);

    void deregisterDataSource(DataSource<TResponse> dataSource);

    Collection<DataSource<TResponse>> sources();
}
