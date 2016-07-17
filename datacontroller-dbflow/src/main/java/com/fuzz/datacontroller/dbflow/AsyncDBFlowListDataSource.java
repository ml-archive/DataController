package com.fuzz.datacontroller.dbflow;

import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

/**
 * Description: Loads and saves a {@link List} of {@link TModel} to/from the database.
 * Provides data from the DB when used.
 */
public class AsyncDBFlowListDataSource<TModel extends Model>
        extends BaseAsyncDBFlowDataSource<TModel, List<TModel>>
        implements ProcessModelTransaction.ProcessModel<TModel> {

    public AsyncDBFlowListDataSource(RefreshStrategy<List<TModel>> refreshStrategy,
                                     Class<TModel> tModelClass) {
        super(refreshStrategy, tModelClass);
    }

    public AsyncDBFlowListDataSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    public AsyncDBFlowListDataSource(RefreshStrategy<List<TModel>> refreshStrategy,
                                     DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy, defaultParams);
    }

    public AsyncDBFlowListDataSource(DBFlowParamsInterface<TModel> defaultParams) {
        super(defaultParams);
    }

    @Override
    public List<TModel> getStoredData(SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().queryList();
    }

    @Override
    protected void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                final DataController.Success<List<TModel>> success) {
        queryBuilder.queryListResult(new QueryTransaction.QueryResultListCallback<TModel>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction,
                                          @Nullable List<TModel> tResult) {
                success.onSuccess(new DataControllerResponse<>(tResult, getSourceType()));
            }
        });
    }

    @Override
    protected void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                DataControllerResponse<List<TModel>> response) {
        processBuilder.addAll(response.getResponse());
    }
}
