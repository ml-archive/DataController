package com.fuzz.datacontroller;

import com.fuzz.datacontroller.codegen.BaseGeneratedDefinitionHolder;
import com.fuzz.datacontroller.codegen.Creator;

import java.lang.reflect.Constructor;

import retrofit2.Retrofit;

/**
 * Description: Main interaction point into generated _Def files.
 */
public class DataControllerManager {

    private static BaseGeneratedDefinitionHolder holder;

    private static BaseGeneratedDefinitionHolder getHolder() {
        if (holder == null) {
            try {
                final Class<?> clazz = Class.forName("com.fuzz.datacontroller.codegen.GeneratedDefinitionHolder");
                final Constructor<?> constructor = clazz.getConstructor();
                constructor.setAccessible(true);
                holder = (BaseGeneratedDefinitionHolder) constructor.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return holder;
    }

    public static <T> T create(Class<T> tClass) {
        final Creator<T, Void> creator = getHolder().getDefaultCreator(tClass);
        if (creator == null) {
            throw new IllegalStateException("Cannot find class " + tClass + " default creator." +
                    " Ensure you added a DataDefinition annotation and that the constructor is not different.");
        }
        return creator.newInstance(null);
    }

    public static <T> T create(Class<T> tClass, Retrofit retrofit) {
        final Creator<T, Retrofit> retrofitCreator = getHolder().getRetrofitCreator(tClass);
        if (retrofit == null) {
            throw new IllegalStateException("Cannot find class " + tClass + " retrofit creator." +
                    " Ensure you added a DataDefinition annotation and that there's a network component.");
        }
        return retrofitCreator.newInstance(retrofit);
    }
}
