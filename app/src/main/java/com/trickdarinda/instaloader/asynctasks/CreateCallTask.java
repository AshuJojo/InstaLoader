package com.trickdarinda.instaloader.asynctasks;

import android.os.AsyncTask;
import android.os.Looper;

import com.trickdarinda.instaloader.api.ApiClient;
import com.trickdarinda.instaloader.model.PostsResponse;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateCallTask extends AsyncTask<String, Void, Void> {
    private onResponseListener responseListener = null;

    public CreateCallTask() {

    }

    public interface onResponseListener {
        void onCallStart();

        void onCallFinished();

        void onResponse(PostsResponse response);

        void onFailure(Throwable t);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (responseListener != null)
            responseListener.onCallFinished();
    }

    public void setOnResponseListener(onResponseListener listener) {
            this.responseListener = listener;
    }

    @Override
    protected Void doInBackground(String... strings) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        if (responseListener != null)
            responseListener.onCallStart();
        makeCall(strings[0]);
        return null;
    }

    /* method to create a call */
    private void makeCall(String url) {
        //get the object of ApiClient Class
        ApiClient apiClient = new ApiClient(url);
        //create call from the apiclient
        Call<PostsResponse> call = apiClient.getPostsResponse().postsResponse();

        //Enqueue the call from callback from PostsResponse
        call.enqueue(new Callback<PostsResponse>() {
            @Override
            public void onResponse(@NotNull Call<PostsResponse> call, @NotNull Response<PostsResponse> response) {
                //If response is successful send the response to main Activity
                if (response.isSuccessful()) {
                    if (responseListener != null)
                        responseListener.onResponse(response.body());
                } else {
                        responseListener.onResponse(null);
                }
            }

            @Override
            public void onFailure(@NotNull Call<PostsResponse> call, @NotNull Throwable t) {
                //If an error occurred cancel the call
                //and show a toast of call failure
                if (responseListener != null)
                    responseListener.onFailure(t);
                call.cancel();
            }
        });
    }
}
