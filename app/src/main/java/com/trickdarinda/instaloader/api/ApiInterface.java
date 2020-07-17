package com.trickdarinda.instaloader.api;

import com.trickdarinda.instaloader.model.PostsResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    @GET("?__a=1")
    Call<PostsResponse> postsResponse();
}
