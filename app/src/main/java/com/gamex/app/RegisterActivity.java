package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private static final String REGISTER_URL = "http://api.amazon.web.id/api/auth/register";
    private static final String TAG = "RegisterActivity";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private MaterialButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.registerNameInput);
        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        registerButton = findViewById(R.id.registerSubmitButton);

        registerButton.setOnClickListener(v -> handleRegister());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void handleRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError(getString(R.string.auth_name_required));
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError(getString(R.string.auth_email_required));
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError(getString(R.string.auth_email_invalid));
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.auth_password_required));
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 8) {
            passwordInput.setError(getString(R.string.auth_password_length));
            passwordInput.requestFocus();
            return;
        }

        setLoading(true);

        executor.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("email", email);
                requestBody.put("password", password);
                requestBody.put("name", name);

                ApiClient.ApiResponse response = ApiClient.postJson(REGISTER_URL, requestBody);
                if (isSuccess(response.statusCode)) {
                    handleRegisterSuccess(response.body);
                } else {
                    Log.e(TAG, "Register failed. status=" + response.statusCode + " body=" + response.body);
                    showInternalError();
                }
            } catch (IOException | JSONException exception) {
                Log.e(TAG, "Register request error", exception);
                showInternalError();
            } finally {
                runOnUiThread(() -> setLoading(false));
            }
        });
    }

    private void handleRegisterSuccess(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String accessToken = json.optString("access_token", null);
            if (TextUtils.isEmpty(accessToken)) {
                showInternalError();
                return;
            }

            AuthManager.saveAccessToken(getApplicationContext(), accessToken);
            String message = json.optString("message", getString(R.string.register_success_message));

            runOnUiThread(() -> {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                navigateToHome();
            });
        } catch (JSONException exception) {
            showInternalError();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showInternalError() {
        runOnUiThread(() ->
                Toast.makeText(RegisterActivity.this, R.string.auth_internal_error, Toast.LENGTH_SHORT).show()
        );
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= HttpURLConnection.HTTP_OK && statusCode < HttpURLConnection.HTTP_MULT_CHOICE;
    }

    private void setLoading(boolean isLoading) {
        registerButton.setEnabled(!isLoading);
    }
}
