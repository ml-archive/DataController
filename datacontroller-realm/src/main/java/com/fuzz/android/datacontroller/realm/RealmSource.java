package com.fuzz.android.datacontroller.realm;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;

import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Description: Provides a {}
 *
 * @author Andrew Grosner (Fuzz)
 */

public class RealmSource<T extends RealmModel> implements DataSource.Source<RealmResults<T>> {

    public interface RealmParamsInterface<T extends RealmModel> {

        RealmQuery<T> getQuery();
    }

    private final boolean async;

    private RealmSource(boolean async) {
        this.async = async;
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    DataController.Error error, final DataController.Success<RealmResults<T>> success) {
        RealmParams<T> params = getParams(sourceParams);
        RealmQuery<T> query = params.getQuery();

        if (async) {
            RealmResults<T> realmResults = query.findAllAsync();
            realmResults.addChangeListener(new RealmChangeListener<RealmResults<T>>() {
                @Override
                public void onChange(RealmResults<T> results) {
                    success.onSuccess(new DataControllerResponse<>(results, DataSource.SourceType.DISK));
                }
            });
        } else {
            RealmResults<T> results = query.findAll();
            success.onSuccess(new DataControllerResponse<>(results, DataSource.SourceType.DISK));
        }
    }

    @Override
    public void cancel() {
        // cannot cancel async queries in Realm?
    }

    @Override
    public void store(DataControllerResponse<RealmResults<T>> response) {

    }

    @Override
    public RealmResults<T> getStoredData(DataSource.SourceParams params) {
        return getParams(params).getQuery().findAll();
    }

    @Override
    public void clearStoredData(DataSource.SourceParams params) {

    }

    @Override
    public boolean hasStoredData(DataSource.SourceParams params) {
        return getParams(params).getQuery().count() > 0;
    }

    @SuppressWarnings("unchecked")
    private RealmParams<T> getParams(DataSource.SourceParams sourceParams) {
        RealmParams<T> params = null;
        if (sourceParams instanceof RealmParams) {
            params = (RealmParams<T>) sourceParams;
        }
        if (params == null) {
            throw new IllegalArgumentException("The passed dataSource params must implement "
                    + RealmParams.class.getSimpleName());
        }
        if (params.getQuery() == null) {
            throw new IllegalArgumentException("The RealmParams must provide a non-null RealmQuery" +
                    "to execute");
        }
        return params;
    }

    public static class RealmParams<T extends RealmModel> extends DataSource.SourceParams
            implements RealmParamsInterface<T> {

        private final RealmQuery<T> realmQuery;

        public RealmParams(RealmQuery<T> realmQuery) {
            this.realmQuery = realmQuery;
        }

        @Override
        public RealmQuery<T> getQuery() {
            return realmQuery;
        }
    }
}
