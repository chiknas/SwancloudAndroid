package com.chiknas.swancloud;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.chiknas.swancloud.api.ApiService;
import com.chiknas.swancloud.api.apiservices.user.CurrentUserDetails;
import com.chiknas.swancloud.services.MediaStoreService;
import com.chiknas.swancloud.workers.FileSyncWorker;

import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loadData();

        Button suncNowButton = findViewById(R.id.sync_now);
        suncNowButton.setOnClickListener(v -> {
            OneTimeWorkRequest oneTimeFileSyncWorkRequest = new OneTimeWorkRequest.Builder(FileSyncWorker.class).build();
            WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork("OneTimeFileSyncWorker", ExistingWorkPolicy.KEEP, oneTimeFileSyncWorkRequest);
        });

        Button refreshButton = findViewById(R.id.refresh);
        refreshButton.setOnClickListener(v -> loadData());

        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)));

    }

    private void loadData() {
        TextView lastUploadedFileDate = findViewById(R.id.last_uploaded_file_date);
        TextView unsyncedFiles = findViewById(R.id.unsynced_files);

        new ApiService(getApplicationContext()).getUserApi().currentUserDetails().enqueue(new Callback<CurrentUserDetails>() {
            @Override
            public void onResponse(@NonNull Call<CurrentUserDetails> call, @NonNull Response<CurrentUserDetails> response) {
                if (!response.isSuccessful()) return;
                Optional.ofNullable(response.body()).ifPresent(body -> {
                    int count = new MediaStoreService(getApplicationContext()).getMediaTakenAfter(body.getLastUploadedFileDate()).getCount();
                    unsyncedFiles.setText(String.valueOf(count));
                });
            }

            @Override
            public void onFailure(@NonNull Call<CurrentUserDetails> call, @NonNull Throwable t) {

            }
        });


        new ApiService(getApplicationContext()).getUserApi().currentUserDetails().enqueue(new Callback<CurrentUserDetails>() {
            @Override
            public void onResponse(@NonNull Call<CurrentUserDetails> call, @NonNull Response<CurrentUserDetails> response) {
                if (!response.isSuccessful()) return;
                Optional.ofNullable(response.body())
                        .ifPresent(currentUserDetails ->
                                lastUploadedFileDate.setText(currentUserDetails.getLastUploadedFileDate().toString()));
            }

            @Override
            public void onFailure(@NonNull Call<CurrentUserDetails> call, @NonNull Throwable t) {

            }
        });


    }
}