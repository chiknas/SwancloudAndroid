package com.chiknas.swancloud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.chiknas.swancloud.sharedpreferences.AuthenticationSharedPreferences;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class AutoLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login);

        SharedPreferences swancloudSharedPreferences = getApplicationContext().getSharedPreferences("swancloud", MODE_PRIVATE);
        long refreshTokenExpiry = swancloudSharedPreferences.getLong(AuthenticationSharedPreferences.REFRESH_TOKEN_EXPIRY, 0);

        LocalDateTime refreshTokenExpiryDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(refreshTokenExpiry),
                        TimeZone.getDefault().toZoneId());

        boolean isSessionExpired = refreshTokenExpiryDate.isBefore(LocalDateTime.now());
        if(isSessionExpired){
            startActivity(new Intent(this, LoginActivity.class));
        }

        startActivity(new Intent(this, HomeActivity.class));
    }
}