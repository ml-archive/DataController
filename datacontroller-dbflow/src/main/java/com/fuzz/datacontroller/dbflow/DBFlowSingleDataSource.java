package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController2;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a {@link DataSource} for loading and storing a single {@link TModel}. These
 * operations happen synchronously.
 */
public class DBFlowSingleDataSource<TModel extends Model>
        extends BaseDBFlowDataSource<TModel, TModel> {

    public DBFlowSingleDataSource(RefreshStrategy<TModel> refreshStrategy,
                                  Class<TModel> tModelClass) {
        super(refreshStrategy, tModelClass);
    }

    public DBFlowSingleDataSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    public DBFlowSingleDataSource(RefreshStrategy<TModel> refreshStrategy,
                                  DBFlowParamsInterface<TModel> defaultParams,
                                  Class<TModel> tModelClass) {
        super(refreshStrategy, defaultParams);
    }

    public DBFlowSingleDataSource(DBFlowParamsInterface<TModel> defaultParams,
                                  Class<TModel> tModelClass) {
        super(defaultParams);
    }

    @Override
    public void cancel() {
    }

    @Override
    protected void doGet(SourceParams sourceParams,
                         DataController2.Success<TModel> success,
                         DataController2.Error error) {
        TModel model = getParams(sourceParams)
                .getModelQueriable()
                .querySingle();
        success.onSuccess(new DataControllerResponse<>(model, getSourceType()));
    }

    @Override
    protected void doStore(DataControllerResponse<TModel> dataControllerResponse) {
        if (dataControllerResponse != null) {
            TModel response = dataControllerResponse.getResponse();
            store(response);
        }
    }

    @Override
    public TModel getStoredData(SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().querySingle();
    }
}
