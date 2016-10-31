package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

/**
 * Description: Represent the default params for {@link DBFlowSingleSource}. It specifies the
 * {@link ModelQueriable} it will use to load from the DB.
 */
public class DBFlowParams<TModel>
        implements DBFlowParamsInterface<TModel> {

    public static <TModel> DBFlowParamsInterface<TModel>
    getParams(DataSource.SourceParams sourceParams) {
        DBFlowParamsInterface<TModel> params = null;
        if (sourceParams instanceof DBFlowParamsInterface) {
            //noinspection unchecked
            params = (DBFlowParamsInterface<TModel>) sourceParams;
        }

        if (params == null) {
            throw new IllegalArgumentException("The passed dataSource params must implement "
                    + DBFlowParamsInterface.class.getSimpleName());
        }
        return params;
    }

    private final ModelQueriable<TModel> modelQueriable;

    public DBFlowParams(ModelQueriable<TModel> modelQueriable) {
        this.modelQueriable = modelQueriable;
    }

    @Override
    public ModelQueriable<TModel> getModelQueriable() {
        return modelQueriable;
    }

}
