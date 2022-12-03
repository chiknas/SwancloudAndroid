package com.chiknas.swancloud;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chiknas.swancloud.api.ApiService;
import com.chiknas.swancloud.api.services.authentication.JwtToken;
import com.chiknas.swancloud.api.services.authentication.LoginRequest;

import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        EditText usernameField = findViewById(R.id.username_field);
        EditText passwordField = findViewById(R.id.password_field);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(usernameField.getText().toString());
            loginRequest.setPassword(passwordField.getText().toString());
            login(loginRequest);
        });
    }

    private void login(LoginRequest loginRequest) {
        new ApiService(getApplicationContext())
                .getAuthenticationApi()
                .authenticateUser(loginRequest)
                .enqueue(new SuccessfulLogin());
    }

    /**
     * Class to handle successful login callback.
     * Responsible to store the tokens and prepare the app.
     */
    class SuccessfulLogin implements Callback<JwtToken> {
        @Override
        public void onResponse(@NonNull Call<JwtToken> call, @NonNull Response<JwtToken> response) {
            // Return on failed login
            Optional<JwtToken> responseBody = Optional.ofNullable(response.body());
            if (!response.isSuccessful() || !responseBody.isPresent()) {
                Toast.makeText(getApplicationContext(), "Nope!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Store JWT to be used from the shared preferences
            JwtToken jwtToken = responseBody.get();
            SharedPreferences swancloudSharedPreferences = getSharedPreferences("swancloud", MODE_PRIVATE);
            swancloudSharedPreferences
                    .edit()
                    .putString("access_token", jwtToken.getToken())
                    .putString("refresh_token", jwtToken.getRefreshToken())
                    .apply();
        }

        @Override
        public void onFailure(@NonNull Call<JwtToken> call, @NonNull Throwable t) {
            Toast.makeText(getApplicationContext(), "Network error!", Toast.LENGTH_SHORT).show();
        }
    }


}