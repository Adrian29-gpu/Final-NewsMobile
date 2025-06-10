package com.example.proyeknews.network;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final String API_KEY = "00e0eea8c43f4c6cb02effaee4ab274b";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create OkHttpClient with timeouts and API key interceptor only
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    // Add API key to all requests automatically
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws java.io.IOException {
                            Request original = chain.request();
                            HttpUrl originalHttpUrl = original.url();

                            HttpUrl url = originalHttpUrl.newBuilder()
                                    .addQueryParameter("apiKey", API_KEY)
                                    .build();

                            Request request = original.newBuilder()
                                    .url(url)
                                    .build();

                            return chain.proceed(request);
                        }
                    })
                    .build();

            // Create and configure Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}