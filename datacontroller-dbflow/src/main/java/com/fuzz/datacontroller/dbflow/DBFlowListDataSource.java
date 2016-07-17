package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

import java.util.List;

/**
 * Description: Provides a {@link DataSource} for loading and storing a {@link List} of
 * {@link TModel}. These operations happen synchronously. We assume that all models passed here
 * come from the same database.
 */
public class DBFlowListDataSource<TModel extends Model>
        extends BaseDBFlowDataSource<TModel, List<TModel>> {

    public DBFlowListDataSource(RefreshStrategy<List<TModel>> refreshStrategy,
                                Class<TModel> tModelClass) {
        super(refreshStrategy, tModelClass);
    }

    public DBFlowListDataSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    public DBFlowListDataSource(RefreshStrategy<List<TModel>> refreshStrategy,
                                DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy, defaultParams);
    }

    public DBFlowListDataSource(DBFlowParamsInterface<TModel> defaultParams) {
        super(defaultParams);
    }

    @Override
    public void cancel() {
    }

    @Override
    protected void doGet(SourceParams sourceParams,
                         DataController.Success<List<TModel>> success,
                         DataController.Error error) {
        List<TModel> modelList = getParams(sourceParams)
                .getModelQueriable().queryList();
        success.onSuccess(new DataControllerResponse<>(modelList, getSourceType()));
    }

    @Override
    protected void doStore(DataControllerResponse<List<TModel>> dataControllerResponse) {
        if (dataControllerResponse != null) {
            List<TModel> response = dataControllerResponse.getResponse();
            if (response != null) {
                storeAll(response);
            }
        }
    }

    @Override
    public List<TModel> getStoredData(SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().queryList();
    }

    public void storeAll(List<TModel> modelList) {
        if (modelList != null && !modelList.isEmpty()) {
            //noinspection unchecked
            Class<TModel> modelClass = (Class<TModel>) modelList.get(0).getClass();
            FlowManager.getDatabaseForTable(modelClass)
                    .executeTransaction(new ProcessModelTransaction.Builder<>(this)
                            .addAll(modelList)
                            .build());
        }
    }
}
