package com.fuzz.datacontroller;

import com.fuzz.datacontroller.codegen.BaseGeneratedDefinitionHolder;

import retrofit2.Retrofit;

/**
 * Description: Main interaction point into generated _Def files.
 */
public class DataControllerManager {

    private BaseGeneratedDefinitionHolder holder;

    private BaseGeneratedDefinitionHolder getHolder() {
        if (holder == null) {
            try {
                holder = (BaseGeneratedDefinitionHolder)
                        Class.forName("com.fuzz.datacontroller.codegen.GeneratedDefinitionHolder").newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return holder;
    }

    public <T> T create(Class<T> tClass) {
        return getHolder().getDefaultCreator(tClass).newInstance(null);
    }

    public <T> T create(Class<T> tClass, Retrofit retrofit) {
        return getHolder().getRetrofitCreator(tClass).newInstance(retrofit);
    }
}
