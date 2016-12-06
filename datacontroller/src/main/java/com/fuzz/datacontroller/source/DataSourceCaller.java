package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController;

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
public interface DataSourceCaller<T> {
    void get(DataSource.SourceParams sourceParams,
             DataController.Error error, DataController.Success<T> success);

    void cancel();

    DataSource.SourceType sourceType();
}
