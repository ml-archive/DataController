package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a {@link DataSource} for loading and storing a single {@link TModel}. These
 * operations happen synchronously.
 */
public class DBFlowDataSource<TModel extends Model> extends DataSource<TModel> {

    private DBFlowParamsInterface<TModel> defaultParams;

    public DBFlowDataSource() {
    }

    public DBFlowDataSource(RefreshStrategy<TModel> refreshStrategy) {
        super(refreshStrategy);
    }

    public DBFlowDataSource(RefreshStrategy<TModel> refreshStrategy,
                            DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy);
        this.defaultParams = defaultParams;
    }

    public DBFlowDataSource(DBFlowParamsInterface<TModel> defaultParams) {
        this.defaultParams = defaultParams;
    }

    @Override
    public void cancel() {
    }

    @Override
    protected void doGet(SourceParams sourceParams,
                         DataController.Success<TModel> success,
                         DataController.Error error) {
        TModel model = DBFlowParams.getParams(defaultParams, sourceParams)
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
    public SourceType getSourceType() {
        return SourceType.DISK;
    }

    public void store(TModel model) {
        if (model != null) {
            model.save();
        }
    }

}
