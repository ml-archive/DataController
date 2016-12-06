package com.fuzz.datacontroller.dbflow;

import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

/**
 * Description: Loads and saves a {@link List} of {@link TModel} to/from the database.
 * Provides data from the DB when used.
 */
public class DBFlowListSource<TModel>
        extends BaseDBFlowSource<TModel, List<TModel>>
        implements ProcessModelTransaction.ProcessModel<TModel> {

    public static <TModel> DataSource.Builder<List<TModel>>
    builderInstance(Class<TModel> modelClass, boolean async) {
        DBFlowListSource<TModel> source = new DBFlowListSource<>(modelClass, async);
        return new DataSource.Builder<>(source);
    }

    private DBFlowListSource(Class<TModel> tModelClass, boolean async) {
        super(tModelClass, async);
    }

    @Override
    protected void storeSync(DataControllerResponse<List<TModel>> response) {
        if (response != null) {
            List<TModel> models = response.getResponse();
            storeAll(models);
        }
    }

    @Override
    protected DataControllerResponse<List<TModel>> executeSync(ModelQueriable<TModel> modelQueriable) {
        List<TModel> modelList = modelQueriable.queryList();
        return new DataControllerResponse<>(modelList, DataSource.SourceType.DISK);
    }

    @Override
    public List<TModel> getStoredData(DataSource.SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().queryList();
    }

    @Override
    protected void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                final DataController.Success<List<TModel>> success) {
        queryBuilder.queryListResult(new QueryTransaction.QueryResultListCallback<TModel>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction,
                                          @Nullable List<TModel> tResult) {
                success.onSuccess(new DataControllerResponse<>(tResult, DataSource.SourceType.DISK));
            }
        });
    }

    @Override
    protected void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                DataControllerResponse<List<TModel>> response) {
        processBuilder.addAll(response.getResponse());
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
