package com.chiknas.swancloud.api.apiservices.authentication;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.chiknas.swancloud.sharedpreferences.AuthenticationSharedPreferences;
import com.chiknas.swancloud.workers.FileSyncWorker;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefreshAccessTokenCallback implements Callback<RefreshTokenResponse> {

    private final Context context;

    public RefreshAccessTokenCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onResponse(@NonNull Call<RefreshTokenResponse> call, Response<RefreshTokenResponse> response) {

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
                .putString(AuthenticationSharedPreferences.ACCESS_TOKEN, jwtToken.getAccessToken())
                .putLong(AuthenticationSharedPreferences.ACCESS_TOKEN_EXPIRY, jwtToken.getAccessTokenExpiry())
                .apply();

        // Start media file sync in the background
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();

        PeriodicWorkRequest periodicFileSyncWorkRequest = new PeriodicWorkRequest.Builder(FileSyncWorker.class, 1, TimeUnit.HOURS).setConstraints(constraints).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("FileSyncPeriodicTask", ExistingPeriodicWorkPolicy.KEEP, periodicFileSyncWorkRequest);
    }

    @Override
    public void onFailure(@NonNull Call<RefreshTokenResponse> call, @NonNull Throwable t) {
        Toast.makeText(context, "Nope!", LENGTH_SHORT).show();
    }
}
