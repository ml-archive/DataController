package com.fuzz.datacontroller.dbflow;

import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Description: Loads and saves a single {@link TModel} to/from the database.
 * Provides data from the DB when used.
 */
public class DBFlowSingleSource<TModel>
        extends BaseDBFlowSource<TModel, TModel> {

    public static <TModel> DataSource.Builder<TModel>
    builderInstance(Class<TModel> modelClass, boolean async) {
        DBFlowSingleSource<TModel> source = new DBFlowSingleSource<>(modelClass, async);
        return new DataSource.Builder<>(source, DataSource.SourceType.DISK);
    }

    private DBFlowSingleSource(Class<TModel> tModelClass, boolean async) {
        super(tModelClass, async);
    }

    @Override
    protected void storeSync(DataControllerResponse<TModel> response) {
        if (response != null) {
            TModel model = response.getResponse();
            store(model);
        }
    }

    @Override
    protected DataControllerResponse<TModel> executeSync(ModelQueriable<TModel> modelQueriable) {
        TModel model = modelQueriable.querySingle();
        return new DataControllerResponse<>(model, DataSource.SourceType.DISK);
    }

    @Override
    public TModel getStoredData(DataSource.SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().querySingle();
    }

    @Override
    protected void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                final DataController.Success<TModel> success) {
        queryBuilder.querySingleResult(new QueryTransaction.QueryResultSingleCallback<TModel>() {
            @Override
            public void onSingleQueryResult(QueryTransaction transaction,
                                            @Nullable TModel model) {
                success.onSuccess(new DataControllerResponse<>(model, DataSource.SourceType.DISK));
            }
        });
    }

    @Override
    protected void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                DataControllerResponse<TModel> response) {
        if (response.getResponse() != null) {
            processBuilder.add(response.getResponse());
        }
    }
}
