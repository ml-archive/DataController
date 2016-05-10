package com.fuzz.datacontroller.datacontroller2.source;

/**
 * Description: Provides a source of where information comes from.
 */
public interface DataSource<TResponse, TStorage> {

    class SourceParams {

        /**
         * an optional index to use. -1 is default, meaning we should retrieve all information.
         */
        public int index = -1;
    }

    /**
     * Stores a response.
     */
    void store(TResponse tResponse);

    /**
     * @param sourceParams The params used to retrieve information from the {@link DataSource}.
     * @return What is stored.
     */
    TStorage get(SourceParams sourceParams);
}
