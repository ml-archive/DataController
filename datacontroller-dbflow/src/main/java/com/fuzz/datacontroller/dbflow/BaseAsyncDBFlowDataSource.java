package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Provides common functionality for operating with DBFlow asynchronously.
 */
public abstract class BaseAsyncDBFlowDataSource<TModel extends Model, TSource>
        extends BaseDBFlowDataSource<TModel, TSource> {

    private Transaction currentSaveTransaction;
    private Transaction currentLoadTransaction;

    public BaseAsyncDBFlowDataSource(RefreshStrategy<TSource> refreshStrategy,
                                     Class<TModel> tModelClass) {
        super(refreshStrategy, tModelClass);
    }

    public BaseAsyncDBFlowDataSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    public BaseAsyncDBFlowDataSource(RefreshStrategy<TSource> refreshStrategy,
                                     DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy, defaultParams);
    }

    public BaseAsyncDBFlowDataSource(DBFlowParamsInterface<TModel> defaultParams) {
        super(defaultParams);
    }

    public void cancelSaveIfNotNull() {
        if (currentSaveTransaction != null) {
            currentSaveTransaction.cancel();
            currentSaveTransaction = null;
        }
    }

    public void cancelLoadIfNotNull() {
        if (currentLoadTransaction != null) {
            currentLoadTransaction.cancel();
            currentLoadTransaction = null;
        }
    }

    @Override
    public void cancel() {
        cancelSaveIfNotNull();
        cancelLoadIfNotNull();
    }

    @Override
    protected void doGet(SourceParams sourceParams,
                         final DataController.Success<TSource> success,
                         final DataController.Error error) {
        DBFlowParamsInterface<TModel> params = getParams(sourceParams);
        ModelQueriable<TModel> modelQueriable = params.getModelQueriable();
        QueryTransaction.Builder<TModel> queryBuilder
                = new QueryTransaction.Builder<>(modelQueriable);
        prepareQuery(queryBuilder, success);
        currentLoadTransaction = FlowManager.getDatabaseForTable(getModelClass())
                .beginTransactionAsync(queryBuilder
                        .build())
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable throwable) {
                        error.onFailure(new DataResponseError.Builder(getSourceType(),
                                throwable).build());
                    }
                }).build();
        currentLoadTransaction.execute();
    }

    @Override
    protected void doStore(DataControllerResponse<TSource> dataControllerResponse) {
        ProcessModelTransaction.Builder<TModel> processBuilder = new ProcessModelTransaction
                .Builder<>(this);
        prepareStore(processBuilder, dataControllerResponse);
        currentSaveTransaction = FlowManager.getDatabaseForTable(getModelClass())
                .beginTransactionAsync(processBuilder.build())
                .build();
        currentSaveTransaction.execute();
    }

    protected abstract void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                         DataController.Success<TSource> success);

    protected abstract void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                         DataControllerResponse<TSource> response);
}
