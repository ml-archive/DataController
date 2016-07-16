package com.fuzz.datacontroller.dbflow;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: The main interface for {@link DBFlowParams} that tell the {@link DBFlowDataSource}
 * how to retrieve data from the database.
 */
public interface DBFlowParamsInterface<TModel extends Model> {

    ModelQueriable<TModel> getModelQueriable();
}
