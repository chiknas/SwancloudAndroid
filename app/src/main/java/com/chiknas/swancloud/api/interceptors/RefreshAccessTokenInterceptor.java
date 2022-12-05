package com.chiknas.swancloud.api.interceptors;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.chiknas.swancloud.api.apiservices.authentication.RefreshTokenRequest;
import com.chiknas.swancloud.api.apiservices.authentication.RefreshTokenResponse;
import com.chiknas.swancloud.sharedpreferences.AuthenticationSharedPreferences;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Interceptor responsible to refresh an access token when it expires.
 * It works by checking the response on each request. If a request fails
 * with error code 403 (unauthorised) then it will try to refresh the access token
 * with the refresh token.
 * If the refresh token is expired it takes the user to the login page.
 */
public class RefreshAccessTokenInterceptor implements Interceptor {

    private final Context context;

    public RefreshAccessTokenInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        // Execute request and return if successful. No need to refresh token.
        Request initialRequest = chain.request();
        Response initialResponse = chain.proceed(initialRequest);
        if(initialResponse.code() != 403){
            return initialResponse;
        }

        refreshAccessToken(chain);

        return chain.proceed(initialRequest);
    }

    private void refreshAccessToken(Chain chain){
        try{
            // Get refresh token from shared preferences
            SharedPreferences swancloudSharedPreferences = context.getSharedPreferences("swancloud", MODE_PRIVATE);
            String refreshToken = swancloudSharedPreferences.getString(AuthenticationSharedPreferences.REFRESH_TOKEN, null);

            // Setup request POJO
            RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
            refreshTokenRequest.setRefreshToken(refreshToken);

            // Get new access token
            RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), new Gson().toJson(refreshTokenRequest));
            Request refreshTokenHttpRequest = chain.request()
                    .newBuilder()
                    .url(swancloudSharedPreferences.getString(AuthenticationSharedPreferences.BASE_SERVER_URL, null) + "/auth/refreshtoken")
                    .post(requestBody)
                    .build();
            Response refreshTokenResponse = chain.proceed(refreshTokenHttpRequest);
            RefreshTokenResponse refreshedAccessToken = new Gson().fromJson(refreshTokenResponse.body().string(), RefreshTokenResponse.class);

            // Update access token to shared preferences
            swancloudSharedPreferences
                    .edit()
                    .putString(AuthenticationSharedPreferences.ACCESS_TOKEN, refreshedAccessToken.getAccessToken())
                    .putLong(AuthenticationSharedPreferences.ACCESS_TOKEN_EXPIRY, refreshedAccessToken.getAccessTokenExpiry())
                    .apply();

        }catch(Exception e){
            System.out.println(e);
        }
    }
}
