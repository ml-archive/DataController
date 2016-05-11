package com.fuzz.datacontroller.test;

import android.support.annotation.NonNull;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.DataResponseError;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;

/**
 * Description: Stores data in a dbflow database.
 */
public class DBFlowDataSource<TModel extends Model> extends DataSource<List<TModel>> {

    public static class DatabaseParams<TModel extends Model> extends SourceParams {

        public ModelQueriable<TModel> modelQueriable;
    }

    private final Class<TModel> modelClass;

    public DBFlowDataSource(RefreshStrategy<List<TModel>> refreshStrategy, Class<TModel> modelClass) {
        super(refreshStrategy);
        this.modelClass = modelClass;
    }

    public DBFlowDataSource(Class<TModel> modelClass) {
        this.modelClass = modelClass;
    }

    @Override
    protected void doGet(SourceParams sourceParams, final DataController.Success<List<TModel>> success, final DataController.Error error) {
        DatabaseDefinition database = FlowManager.getDatabaseForTable(modelClass);
        database.beginTransactionAsync(new QueryTransaction.Builder<>(getModelQueriableFromParams(sourceParams))
                .queryResult(new QueryTransaction.QueryResultCallback<TModel>() {
                    @Override
                    public void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TModel> tResult) {
                        success.onSuccess(new DataControllerResponse<>(tResult.toListClose(), SourceType.DISK));
                    }
                }).runResultCallbacksOnSameThread(true)
                .build())
                .error(new Transaction.Error() {
                    @Override
                    public void onError(Transaction transaction, Throwable throwable) {
                        error.onFailure(new DataResponseError(throwable));
                    }
                })
                .build().execute();
    }

    @Override
    protected void doStore(DataControllerResponse<List<TModel>> dataControllerResponse) {
        if (dataControllerResponse != null && dataControllerResponse.getResponse() != null) {
            // store synchronous. Usually we would want to store async.
            DatabaseDefinition database = FlowManager.getDatabaseForTable(modelClass);
            database.executeTransaction(new ProcessModelTransaction.Builder<>(
                    new ProcessModelTransaction.ProcessModel<TModel>() {
                        @Override
                        public void processModel(TModel model) {
                            model.save();
                        }
                    })
                    .addAll(dataControllerResponse.getResponse())
                    .build());
        }
    }

    @Override
    public List<TModel> getStoredData(SourceParams sourceParams) {
        return getModelQueriableFromParams(sourceParams).queryList();
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.DISK;
    }

    /**
     * Retrieves a {@link ModelQueriable}. If none specified, then we retrieve all table data.
     *
     * @param sourceParams The params to retrieve data from.
     */
    protected ModelQueriable<TModel> getModelQueriableFromParams(SourceParams sourceParams) {
        ModelQueriable<TModel> modelQueriable = null;
        if (sourceParams instanceof DatabaseParams) {
            //noinspection unchecked
            DatabaseParams<TModel> databaseParams = ((DatabaseParams) sourceParams);
            modelQueriable = databaseParams.modelQueriable;
        }

        if (modelQueriable == null) {
            modelQueriable = SQLite.select().from(modelClass);
        }
        return modelQueriable;
    }
}
