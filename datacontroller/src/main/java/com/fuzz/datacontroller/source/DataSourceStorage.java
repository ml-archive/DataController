package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataControllerResponse;

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
public interface DataSourceStorage<T> {

    void store(DataControllerResponse<T> response);

    T getStoredData(DataSource.SourceParams params);

    void clearStoredData(DataSource.SourceParams params);

    boolean hasStoredData(DataSource.SourceParams params);
}
