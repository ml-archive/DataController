package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description:
 */
public abstract class BaseDBFlowSource<TModel, TSource>
        implements DataSource.Source<TSource>, ProcessModelTransaction.ProcessModel<TModel> {

    private Transaction currentSaveTransaction;
    private Transaction currentLoadTransaction;

    private final Class<TModel> modelClass;
    private final boolean async;

    BaseDBFlowSource(Class<TModel> modelClass, boolean async) {
        this.modelClass = modelClass;
        this.async = async;
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
        cancelSaveIfNotNull();
        cancelLoadIfNotNull();
    }

    @Override
    public void store(DataControllerResponse<TSource> response) {
        if (async) {
            ProcessModelTransaction.Builder<TModel> processBuilder = new ProcessModelTransaction
                    .Builder<>(this);
            prepareStore(processBuilder, response);
            currentSaveTransaction = FlowManager.getDatabaseForTable(getModelClass())
                    .beginTransactionAsync(processBuilder.build())
                    .build();
            currentSaveTransaction.execute();
        } else {
            storeSync(response);
        }
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    final DataController.Error error, DataController.Success<TSource> success) {
        DBFlowParamsInterface<TModel> params = getParams(sourceParams);
        ModelQueriable<TModel> modelQueriable = params.getModelQueriable();
        if (async) {
            QueryTransaction.Builder<TModel> queryBuilder
                    = new QueryTransaction.Builder<>(modelQueriable);
            prepareQuery(queryBuilder, success);
            currentLoadTransaction = FlowManager.getDatabaseForTable(getModelClass())
                    .beginTransactionAsync(queryBuilder
                            .build())
                    .error(new Transaction.Error() {
                        @Override
                        public void onError(Transaction transaction, Throwable throwable) {
                            error.onFailure(new DataResponseError.Builder(DataSource.SourceType.DISK,
                                    throwable).build());
                        }
                    }).build();
            currentLoadTransaction.execute();
        } else {
            success.onSuccess(executeSync(modelQueriable));
        }
    }

    public Class<TModel> getModelClass() {
        return modelClass;
    }

    protected abstract void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                         DataController.Success<TSource> success);

    protected abstract void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                         DataControllerResponse<TSource> response);

    protected abstract void storeSync(DataControllerResponse<TSource> response);

    protected abstract DataControllerResponse<TSource> executeSync(ModelQueriable<TModel> modelQueriable);

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
     * Description: Represent the default params for {@link BaseDBFlowSource}. It specifies the
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
