package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource2;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

/**
 * Description:
 */
public abstract class BaseDBFlowDataSource<TModel extends Model, TSource>
        extends DataSource<TSource>
        implements ProcessModelTransaction.ProcessModel<TModel> {

    private DBFlowParamsInterface<TModel> defaultParams;
    private final Class<TModel> modelClass;

    public BaseDBFlowDataSource(DataSource2.RefreshStrategy<TSource> refreshStrategy,
                                Class<TModel> modelClass) {
        super(refreshStrategy);
        this.modelClass = modelClass;
        this.defaultParams = new DBFlowParams<>(SQLite.select().from(modelClass));
    }

    public BaseDBFlowDataSource(Class<TModel> modelClass) {
        this.modelClass = modelClass;
        this.defaultParams = new DBFlowParams<>(SQLite.select().from(modelClass));
    }

    public BaseDBFlowDataSource(DataSource2.RefreshStrategy<TSource> refreshStrategy,
                                DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy);
        this.defaultParams = defaultParams;
        this.modelClass = defaultParams.getModelQueriable().getTable();
    }

    public BaseDBFlowDataSource(DBFlowParamsInterface<TModel> defaultParams) {
        this.defaultParams = defaultParams;
        this.modelClass = defaultParams.getModelQueriable().getTable();
    }

    @Override
    public void cancel() {
    }

    @Override
    public void processModel(TModel model) {
        store(model);
    }

    public void store(TModel model) {
        if (model != null) {
            model.save();
        }
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.DISK;
    }

    protected DBFlowParamsInterface<TModel> getParams(SourceParams sourceParams) {
        return DBFlowParams.getParams(defaultParams, sourceParams);
    }

    public Class<TModel> getModelClass() {
        return modelClass;
    }
}
