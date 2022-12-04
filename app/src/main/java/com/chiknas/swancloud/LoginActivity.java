package com.chiknas.swancloud;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.chiknas.swancloud.api.ApiService;
import com.chiknas.swancloud.api.services.authentication.RefreshAccessTokenCallback;
import com.chiknas.swancloud.api.services.authentication.RefreshTokenRequest;
import com.chiknas.swancloud.sharedpreferences.AuthenticationSharedPreferences;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            new SyncScanCallback());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button syncButton = findViewById(R.id.sync_button);
        syncButton.setOnClickListener(v -> {
            ScanOptions scanOptions = new ScanOptions();
            scanOptions.setBeepEnabled(false);
            scanOptions.setOrientationLocked(false);
            barcodeLauncher.launch(scanOptions);
        });
    }

    static class QRSyncResponse {

        private String baseServerUrl;
        private String refreshToken;
        private String email;
        // Epoch seconds timestamp when the token expires.
        private long expiryTime;

        public String getBaseServerUrl() {
            return baseServerUrl;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getEmail() {
            return email;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }

    /**
     * Class to handle successful login callback.
     * Responsible to store the tokens and prepare the app.
     */
    class SyncScanCallback implements ActivityResultCallback<ScanIntentResult> {

        @Override
        public void onActivityResult(ScanIntentResult result) {
            // Serialize result
            QRSyncResponse qrSyncResponse = new Gson().fromJson(result.getContents(), QRSyncResponse.class);

            // Save sync qr code data to shared preferences
            SharedPreferences swancloudSharedPreferences = getSharedPreferences("swancloud", MODE_PRIVATE);
            swancloudSharedPreferences
                    .edit()
                    .putString(AuthenticationSharedPreferences.REFRESH_TOKEN, qrSyncResponse.getRefreshToken())
                    .putLong(AuthenticationSharedPreferences.REFRESH_TOKEN_EXPIRY, qrSyncResponse.getExpiryTime())
                    .putString(AuthenticationSharedPreferences.EMAIL, qrSyncResponse.getEmail())
                    .putString(AuthenticationSharedPreferences.BASE_SERVER_URL, qrSyncResponse.getBaseServerUrl())
                    .apply();

            // Get access token from the refresh token
            RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
            refreshTokenRequest.setRefreshToken(qrSyncResponse.getRefreshToken());
            new ApiService(getApplicationContext())
                    .getAuthenticationApi()
                    .refreshAccessToken(refreshTokenRequest)
                    .enqueue(new RefreshAccessTokenCallback(getApplicationContext()));

        }
    }


}