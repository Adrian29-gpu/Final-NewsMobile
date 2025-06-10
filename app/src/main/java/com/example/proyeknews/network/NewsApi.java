package com.example.proyeknews.network;

import com.example.proyeknews.models.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {

    @GET("top-headlines")
    Call<NewsResponse> getTopHeadlines(
            @Query("country") String country,
            @Query("category") String category,
            @Query("apiKey") String apiKey
    );

    @GET("everything")
    Call<NewsResponse> getEverything(
            @Query("q") String query,
            @Query("apiKey") String apiKey
    );
}