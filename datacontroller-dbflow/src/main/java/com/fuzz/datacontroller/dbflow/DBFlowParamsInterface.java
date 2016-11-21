package com.fuzz.datacontroller.dbflow;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

/**
 * Description: The main interface for {@link BaseDBFlowSource.DBFlowParams} that tell the {@link BaseDBFlowSource}
 * how to retrieve data from the database.
 */
public interface DBFlowParamsInterface<TModel> {

    ModelQueriable<TModel> getModelQueriable();
}
