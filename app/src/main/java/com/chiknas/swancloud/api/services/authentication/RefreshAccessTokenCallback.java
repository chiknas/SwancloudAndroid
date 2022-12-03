package com.chiknas.swancloud.api.services.authentication;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefreshAccessTokenCallback implements Callback<RefreshTokenResponse> {

    private final Context context;

    public RefreshAccessTokenCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onResponse(Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {

        Optional<RefreshTokenResponse> responseBody = Optional.ofNullable(response.body());
        if (!response.isSuccessful() || !responseBody.isPresent()) {
            Toast.makeText(context, "Network error!", LENGTH_SHORT).show();
            return;
        }

        // Store JWT to be used from the shared preferences
        RefreshTokenResponse jwtToken = responseBody.get();
        SharedPreferences swancloudSharedPreferences = context.getSharedPreferences("swancloud", MODE_PRIVATE);
        swancloudSharedPreferences
                .edit()
                .putString("access_token", jwtToken.getAccessToken())
                .putLong("access_token_expiry", jwtToken.getAccessTokenExpiry())
                .apply();
    }

    @Override
    public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
        Toast.makeText(context, "Nope!", LENGTH_SHORT).show();
    }
}
