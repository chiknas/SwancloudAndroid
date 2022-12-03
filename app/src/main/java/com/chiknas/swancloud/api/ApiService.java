package com.chiknas.swancloud.api;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.chiknas.swancloud.api.services.authentication.AuthenticationServiceApi;
import com.chiknas.swancloud.api.services.files.FileServiceApi;

import java.util.Optional;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class to contact the backend server from. Holds configuration of the http client and generates
 * api services to contact the server.
 * Information required to authenticate to the server should be present in the shared preferences.
 */
public class ApiService {

    private static final String API_BASE_PATH = "http://192.168.0.12:8080";

    private static Retrofit retrofit;

    public ApiService(Context context) {
        retrofit = new Retrofit.Builder()
                .client(httpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_BASE_PATH)
                .build();
    }

    private OkHttpClient httpClient(Context context) {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            SharedPreferences swancloudSharedPreferences = context.getSharedPreferences("swancloud", MODE_PRIVATE);
            Request request =
                    Optional.ofNullable(swancloudSharedPreferences.getString("access_token", null))
                            .map(accessToken -> chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer " + accessToken)
                                    .build()).orElse(chain.request());
            return chain.proceed(request);
        }).build();
    }

    public AuthenticationServiceApi getAuthenticationApi() {
        return retrofit.create(AuthenticationServiceApi.class);
    }

    public FileServiceApi getFilesApi() {
        return retrofit.create(FileServiceApi.class);
    }
}
