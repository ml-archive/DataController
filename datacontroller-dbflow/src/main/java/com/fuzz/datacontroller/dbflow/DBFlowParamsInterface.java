package com.fuzz.datacontroller.dbflow;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

/**
 * Description: The main interface for {@link DBFlowParams} that tell the {@link DBFlowSingleSource}
 * how to retrieve data from the database.
 */
public interface DBFlowParamsInterface<TModel> {

    ModelQueriable<TModel> getModelQueriable();
}
