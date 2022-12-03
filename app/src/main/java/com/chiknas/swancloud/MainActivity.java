package com.chiknas.swancloud;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.chiknas.swancloud.api.ApiTest;
import com.chiknas.swancloud.api.services.JwtToken;
import com.chiknas.swancloud.api.services.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        new ApiTest().getAuthentication().authenticateUser(loginRequest).enqueue(new Callback<JwtToken>() {
            @Override
            public void onResponse(Call<JwtToken> call, Response<JwtToken> response) {
                System.out.println(response.body().getUsername());
                System.out.println(response.body().getToken());
            }

            @Override
            public void onFailure(Call<JwtToken> call, Throwable t) {
                System.out.println(t);
            }
        });
    }


}