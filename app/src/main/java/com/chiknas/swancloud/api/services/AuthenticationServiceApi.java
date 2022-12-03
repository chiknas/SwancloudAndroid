package com.chiknas.swancloud.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationServiceApi {

    @POST("/auth/signin")
    Call<JwtToken> authenticateUser(@Body LoginRequest loginRequest);

}
