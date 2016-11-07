package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

/**
 * Description:
 */
public abstract class BaseDBFlowSource<TModel, TSource>
        implements DataSource.Source<TSource>, ProcessModelTransaction.ProcessModel<TModel> {

    private final Class<TModel> modelClass;

    public BaseDBFlowSource(Class<TModel> modelClass) {
        this.modelClass = modelClass;
    }

    @Override
    public void processModel(TModel model) {
        store(model);
    }

    public void store(TModel model) {
        if (model != null) {
            if (model instanceof Model) {
                ((Model) model).save();
            } else {
                FlowManager.getModelAdapter(modelClass).save(model);
            }
        }
    }

    @Override
    public void clearStoredData(DataSource.SourceParams params) {
        getParams(params).getModelQueriable().executeUpdateDelete();
    }

    @Override
    public boolean hasStoredData(DataSource.SourceParams params) {
        return getParams(params).getModelQueriable().hasData();
    }

    @Override
    public void cancel() {

    }

    public Class<TModel> getModelClass() {
        return modelClass;
    }

    DBFlowParamsInterface<TModel> getParams(DataSource.SourceParams sourceParams) {
        DBFlowParamsInterface<TModel> params = null;
        if (sourceParams instanceof DBFlowParamsInterface) {
            //noinspection unchecked
            params = (DBFlowParamsInterface<TModel>) sourceParams;
        } else if (DataSource.SourceParams.defaultParams.equals(sourceParams)) {
            params = new DBFlowParams<>(SQLite.select().from(getModelClass()));
        }

        if (params == null) {
            throw new IllegalArgumentException("The passed dataSource params must implement "
                    + DBFlowParamsInterface.class.getSimpleName());
        }
        return params;
    }

    /**
     * Description: Represent the default params for {@link DBFlowSingleSource}. It specifies the
     * {@link ModelQueriable} it will use to load from the DB.
     */
    public static class DBFlowParams<TModel> extends DataSource.SourceParams
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
}
