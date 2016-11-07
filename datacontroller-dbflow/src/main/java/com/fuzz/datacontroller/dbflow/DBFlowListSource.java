package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;

import java.util.List;

/**
 * Description: Provides a {@link DataSource} for loading and storing a {@link List} of
 * {@link TModel}. These operations happen synchronously. We assume that all models passed here
 * come from the same database.
 */
public class DBFlowListSource<TModel>
        extends BaseDBFlowSource<TModel, List<TModel>> {

    public static <TModel> DataSource.Builder<List<TModel>> builderInstance(Class<TModel> modelClass) {
        DBFlowListSource<TModel> source = new DBFlowListSource<>(modelClass);
        return new DataSource.Builder<>(source, DataSource.SourceType.DISK);
    }

    private DBFlowListSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    DataController.Error error, DataController.Success<List<TModel>> success) {
        List<TModel> modelList = getParams(sourceParams)
                .getModelQueriable().queryList();
        success.onSuccess(new DataControllerResponse<>(modelList, DataSource.SourceType.DISK));
    }

    @Override
    public void store(DataControllerResponse<List<TModel>> response) {
        if (response != null) {
            List<TModel> models = response.getResponse();
            storeAll(models);
        }
    }

    @Override
    public List<TModel> getStoredData(DataSource.SourceParams sourceParams) {
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
