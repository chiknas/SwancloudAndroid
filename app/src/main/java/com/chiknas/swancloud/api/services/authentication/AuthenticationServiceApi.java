package com.chiknas.swancloud.api.services.authentication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationServiceApi {

    @POST("/auth/refreshtoken")
    Call<RefreshTokenResponse> refreshAccessToken(@Body RefreshTokenRequest refreshTokenRequest);

}
