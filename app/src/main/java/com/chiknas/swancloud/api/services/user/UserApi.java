package com.chiknas.swancloud.api.services.user;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApi {

    @GET("/api/userdetails")
    Call<CurrentUserDetails> currentUserDetails();

}
