package com.trickdarinda.instaloader.api;

import android.util.Log;

import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private String url;
    public ApiClient(String url) {
        this.url = url;
    }

    private Retrofit getRetrofit() {
        if (url != null) {
            //HttpLoggingInterceptor to log request and response information
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            //OkHttpClient to work as a client between application and server
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)
                    .build();

            //return the Retrofit Object
            return new Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return null;
    }

    public ApiInterface getPostsResponse(){
        return Objects.requireNonNull(getRetrofit()).create(ApiInterface.class);
    }
}
