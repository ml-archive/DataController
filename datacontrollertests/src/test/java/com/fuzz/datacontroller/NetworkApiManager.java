package com.fuzz.datacontroller;

import com.google.gson.Gson;

import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Description:
 */
public class NetworkApiManager {

    private static NetworkApiManager manager;

    public static NetworkApiManager get() {
        if (manager == null) {
            manager = new NetworkApiManager();
        }
        return manager;
    }

    private OkHttpClient client;
    private Gson gson;

    public NetworkApiManager() {
        gson = new Gson();
        client = new OkHttpClient();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * Builds a GET request.
     *
     * @param url the url to get.
     * @return A new {@link Call} that we will use to make the request. You can cancel
     * at will.
     */
    public Call createGet(String url) {
        return createGet(url, null);
    }

    /**
     * Builds a GET request with specified parameters.
     *
     * @param url       the url to get.
     * @param urlParams The nullable set of params to pass to the url in GET.
     * @return A new {@link Call} that we will use to make the request. You can cancel
     * at will.
     */
    public Call createGet(String url, Map<String, String> urlParams) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (urlParams != null) {
            for (Map.Entry<String, String> params : urlParams.entrySet()) {
                urlBuilder.addQueryParameter(params.getKey(), params.getValue());
            }
        }

        Request request = new Request.Builder()
                .get()
                .url(urlBuilder.build())
                .build();
        return getClient().newCall(request);
    }
}
