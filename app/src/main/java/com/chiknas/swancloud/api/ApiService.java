package com.chiknas.swancloud.api;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.chiknas.swancloud.api.interceptors.AuthenticationInterceptor;
import com.chiknas.swancloud.api.interceptors.RefreshAccessTokenInterceptor;
import com.chiknas.swancloud.api.services.authentication.AuthenticationServiceApi;
import com.chiknas.swancloud.api.services.files.FileServiceApi;
import com.chiknas.swancloud.sharedpreferences.AuthenticationSharedPreferences;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class to contact the backend server from. Holds configuration of the http client and generates
 * api services to contact the server.
 * Information required to authenticate to the server should be present in the shared preferences.
 */
public class ApiService {

    private static Retrofit retrofit;

    public ApiService(Context context) {
        SharedPreferences swancloudSharedPreferences = context.getSharedPreferences("swancloud", MODE_PRIVATE);
        retrofit = new Retrofit.Builder()
                .client(httpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(swancloudSharedPreferences.getString(AuthenticationSharedPreferences.BASE_SERVER_URL, ""))
                .build();
    }

    private OkHttpClient httpClient(Context context) {
        return new OkHttpClient.Builder()
                .addInterceptor(new RefreshAccessTokenInterceptor(context))
                .addInterceptor(new AuthenticationInterceptor(context))
                .build();
    }

    public AuthenticationServiceApi getAuthenticationApi() {
        return retrofit.create(AuthenticationServiceApi.class);
    }

    public FileServiceApi getFilesApi() {
        return retrofit.create(FileServiceApi.class);
    }
}
