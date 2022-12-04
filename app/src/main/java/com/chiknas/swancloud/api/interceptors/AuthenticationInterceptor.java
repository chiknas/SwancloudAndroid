package com.chiknas.swancloud.api.interceptors;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Optional;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor responsible to add the authorisation header to each HTTP request.
 */
public class AuthenticationInterceptor implements Interceptor {

    private final Context context;

    public AuthenticationInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        SharedPreferences swancloudSharedPreferences = context.getSharedPreferences("swancloud", MODE_PRIVATE);
        Request request =
                Optional.ofNullable(swancloudSharedPreferences.getString("access_token", null))
                        .map(accessToken -> chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + accessToken)
                                .build()).orElse(chain.request());
        return chain.proceed(request);
    }
}
