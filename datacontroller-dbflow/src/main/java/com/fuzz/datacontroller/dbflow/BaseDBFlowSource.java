package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
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
        return DBFlowParams.getParams(sourceParams);
    }
}
