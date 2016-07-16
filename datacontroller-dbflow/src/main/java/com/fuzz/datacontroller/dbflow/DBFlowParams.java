package com.fuzz.datacontroller.dbflow;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Represent the default params for {@link DBFlowDataSource}. It specifies the
 * {@link ModelQueriable} it will use to load from the DB.
 */
public class DBFlowParams<TModel extends Model>
        implements DBFlowParamsInterface<TModel> {

    private final ModelQueriable<TModel> modelQueriable;

    public DBFlowParams(ModelQueriable<TModel> modelQueriable) {
        this.modelQueriable = modelQueriable;
    }

    @Override
    public ModelQueriable<TModel> getModelQueriable() {
        return modelQueriable;
    }

}
