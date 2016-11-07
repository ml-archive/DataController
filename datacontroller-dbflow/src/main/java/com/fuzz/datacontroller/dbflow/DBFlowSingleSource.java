package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: Provides a {@link DataSource} for loading and storing a single {@link TModel}. These
 * operations happen synchronously.
 */
public class DBFlowSingleSource<TModel> extends BaseDBFlowSource<TModel, TModel> {

    public static <TModel> DataSource.Builder<TModel> builderInstance(Class<TModel> modelClass) {
        DBFlowSingleSource<TModel> source = new DBFlowSingleSource<>(modelClass);
        return new DataSource.Builder<>(source, DataSource.SourceType.DISK)
                .storage(source);
    }

    private DBFlowSingleSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    @Override
    public void store(DataControllerResponse<TModel> response) {
        if (response != null) {
            TModel model = response.getResponse();
            store(model);
        }
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    DataController.Error error, DataController.Success<TModel> success) {
        TModel model = getParams(sourceParams)
                .getModelQueriable()
                .querySingle();
        success.onSuccess(new DataControllerResponse<>(model, DataSource.SourceType.DISK));
    }

    @Override
    public TModel getStoredData(DataSource.SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().querySingle();
    }
}
