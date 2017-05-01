package com.fuzz.datacontroller.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource.SourceParams;
import com.fuzz.datacontroller.source.DataSource.SourceType;
import com.fuzz.datacontroller.source.Source;

/**
 * Description: wrapper for one field in/from SharedPreferences.
 * <p>
 * Make sure to set a {@link #setPreferenceDelegate(PreferenceDelegate) PreferenceDelegate}
 * before calling {@link #get} or {@link #store}.
 * </p>
 */
public class SharedPreferencesSource<T> implements Source<T> {

    public static <T> DataSource.Builder<T> builderInstance(SharedPreferences sharedPreferences,
                                                            PreferenceDelegate<T> preferenceDelegate) {
        SharedPreferencesSource<T> source = new SharedPreferencesSource<>(sharedPreferences, preferenceDelegate);
        return new DataSource.Builder<T>(source);
    }

    @NonNull
    private final SharedPreferences sharedPrefs;

    @Nullable
    private PreferenceDelegate<T> preferenceDelegate;

    public SharedPreferencesSource(@NonNull SharedPreferences sharedPrefs,
                                   @Nullable PreferenceDelegate<T> preferenceDelegate) {
        this.sharedPrefs = sharedPrefs;
        this.preferenceDelegate = preferenceDelegate;
    }

    public SharedPreferencesSource(@NonNull Context context, String fileName,
                                   @Nullable PreferenceDelegate<T> preferenceDelegate) {
        this.preferenceDelegate = preferenceDelegate;
        this.sharedPrefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    protected void setPreferenceDelegate(@NonNull PreferenceDelegate<T> preferenceDelegate) {
        this.preferenceDelegate = preferenceDelegate;
    }

    @Override
    public void cancel() {
        // canceling is not applicable to SharedPreferences objects; all relevant operations are atomic
    }

    @Override
    public void get(SourceParams sourceParams, DataController.Error error, DataController.Success<T> success) {
        if (preferenceDelegate == null) {
            error.onFailure(new DataResponseError.Builder(SourceType.DISK,
                    "There is currently no way to retrieve the requested value.").build());
        } else {
            success.onSuccess(new DataControllerResponse<>(preferenceDelegate.getValue(sharedPrefs), SourceType.DISK));
        }
    }

    @Override
    public SourceType sourceType() {
        return SourceType.DISK;
    }

    @Override
    public void store(DataControllerResponse<T> dataControllerResponse) {
        if (preferenceDelegate != null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            preferenceDelegate.setValue(editor, dataControllerResponse);
            editor.apply();
        }
    }

    @Override
    public void clearStoredData(SourceParams sourceParams) {
        if (preferenceDelegate != null) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            //preferenceDelegate must clear editor on null response object in DataControllerResponse for this method to clear
            preferenceDelegate.setValue(editor,
                    new DataControllerResponse<T>(null, SourceType.DISK));
            editor.apply();
        }
    }

    @Override
    public boolean hasStoredData(SourceParams params) {
        return getStoredData(params) != null;
    }

    @Override
    public T getStoredData(SourceParams sourceParams) {
        if (preferenceDelegate != null) {
            return preferenceDelegate.getValue(sharedPrefs);
        } else {
            return null;
        }
    }

    public void store(T data) {
        store(new DataControllerResponse<>(data, SourceType.NETWORK));
    }

}
