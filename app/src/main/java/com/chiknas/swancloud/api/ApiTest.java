package com.chiknas.swancloud.api;

import com.chiknas.swancloud.api.services.AuthenticationServiceApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiTest {

    private static final String API_BASE_PATH = "http://192.168.0.12:8080";

    private final Retrofit retrofit;

    public ApiTest() {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_BASE_PATH)
                .build();
    }

    public AuthenticationServiceApi getAuthentication(){
        return retrofit.create(AuthenticationServiceApi.class);
    }
}
