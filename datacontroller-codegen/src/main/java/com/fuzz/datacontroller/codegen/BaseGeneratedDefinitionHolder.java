package com.fuzz.datacontroller.codegen;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;

/**
 * Description: Extended to provide useful methods.
 */
public class BaseGeneratedDefinitionHolder {

    private final Map<Class<?>, Creator<?, Retrofit>> retrofitCreatorMap = new HashMap<>();
    private final Map<Class<?>, Creator<?, Void>> defaultCreatorMap = new HashMap<>();

    public <T> void addRetrofitCreatorForClass(Class<T> clazz, Creator<T, Retrofit> creator) {
        retrofitCreatorMap.put(clazz, creator);
    }

    public <T> void addDefaultCreatorForClass(Class<T> clazz, Creator<T, Void> creator) {
        defaultCreatorMap.put(clazz, creator);
    }

    @SuppressWarnings("unchecked")
    public <T> Creator<T, Retrofit> getRetrofitCreator(Class<T> tClass) {
        return (Creator<T, Retrofit>) retrofitCreatorMap.get(tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> Creator<T, Void> getDefaultCreator(Class<T> tClass) {
        return (Creator<T, Void>) defaultCreatorMap.get(tClass);
    }


}
