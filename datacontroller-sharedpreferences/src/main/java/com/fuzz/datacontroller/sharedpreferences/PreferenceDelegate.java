package com.fuzz.datacontroller.sharedpreferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataControllerResponse;

/**
 * Description: provides wrapper around one field in a SharedPreferences object.
 *
 * @param <T> what type of object is being set and got
 */
public interface PreferenceDelegate<T> {
    /**
     * Set a new value on the specified sharedPreferences object.
     * <p>
     * Implementations are not responsible for calling
     * {@link Editor#commit()} or {@link Editor#apply()}
     * </p>
     */
    void setValue(@NonNull Editor editor, @NonNull DataControllerResponse<T> value);

    /**
     * Retrieve a value from the specified sharedPreferences
     *
     * @return a value of the right type
     */
    @Nullable
    T getValue(@NonNull SharedPreferences sharedPreferences);
}
