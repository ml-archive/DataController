package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
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
        extends DataSource<TSource> implements ProcessModelTransaction.ProcessModel<TModel> {

    private final Class<?> databaseClass;
    private DBFlowParamsInterface<TModel> defaultParams;

    private Transaction currentSaveTransaction;
    private Transaction currentLoadTransaction;

    public BaseAsyncDBFlowDataSource(Class<?> databaseClass) {
        this.databaseClass = databaseClass;
    }

    public BaseAsyncDBFlowDataSource(RefreshStrategy<TSource> refreshStrategy,
                                     Class<?> databaseClass) {
        super(refreshStrategy);
        this.databaseClass = databaseClass;
    }

    public BaseAsyncDBFlowDataSource(RefreshStrategy<TSource> refreshStrategy,
                                     Class<?> databaseClass,
                                     DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy);
        this.databaseClass = databaseClass;
        this.defaultParams = defaultParams;
    }

    public BaseAsyncDBFlowDataSource(Class<?> databaseClass,
                                     DBFlowParamsInterface<TModel> defaultParams) {
        this.databaseClass = databaseClass;
        this.defaultParams = defaultParams;
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

    public void store(TModel model) {
        if (model != null) {
            model.save();
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
        DBFlowParamsInterface<TModel> params = DBFlowParams.getParams(defaultParams, sourceParams);
        ModelQueriable<TModel> modelQueriable = params.getModelQueriable();
        QueryTransaction.Builder<TModel> queryBuilder
                = new QueryTransaction.Builder<>(modelQueriable);
        prepareQuery(queryBuilder, success);
        currentLoadTransaction = FlowManager.getDatabase(databaseClass)
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
        currentSaveTransaction = FlowManager.getDatabase(databaseClass)
                .beginTransactionAsync(processBuilder.build())
                .build();
        currentSaveTransaction.execute();
    }

    @Override
    public void processModel(TModel model) {
        store(model);
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.DISK;
    }

    protected DBFlowParamsInterface<TModel> getParams(SourceParams sourceParams) {
        return DBFlowParams.getParams(defaultParams, sourceParams);
    }

    protected abstract void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                         DataController.Success<TSource> success);

    protected abstract void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                         DataControllerResponse<TSource> response);
}