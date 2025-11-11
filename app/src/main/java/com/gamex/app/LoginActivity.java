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

public class LoginActivity extends AppCompatActivity {

    private static final String LOGIN_URL = "http://api.amazon.web.id/api/auth/login";
    private static final String TAG = "LoginActivity";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private EditText emailInput;
    private EditText passwordInput;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        loginButton = findViewById(R.id.loginSubmitButton);

        loginButton.setOnClickListener(v -> handleLogin());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

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

                ApiClient.ApiResponse response = ApiClient.postJson(LOGIN_URL, requestBody);
                if (response.statusCode == HttpURLConnection.HTTP_OK) {
                    handleLoginSuccess(response.body);
                } else if (response.statusCode == 422) {
                    handleInvalidCredentials(response.body);
                } else {
                    Log.e(TAG, "Login failed. status=" + response.statusCode + " body=" + response.body);
                    showInternalError();
                }
            } catch (IOException | JSONException exception) {
                Log.e(TAG, "Login request error", exception);
                showInternalError();
            } finally {
                runOnUiThread(() -> setLoading(false));
            }
        });
    }

    private void handleLoginSuccess(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String accessToken = json.optString("access_token", null);
            if (TextUtils.isEmpty(accessToken)) {
                showInternalError();
                return;
            }

            AuthManager.saveAccessToken(getApplicationContext(), accessToken);

            String userRole = AuthManager.ROLE_USER;
            JSONObject userObject = json.optJSONObject("user");
            if (userObject != null) {
                userRole = userObject.optString("role", AuthManager.ROLE_USER);
            }
            AuthManager.saveUserRole(getApplicationContext(), userRole);

            String message = json.optString("message", getString(R.string.login_success_message));
            String finalRole = userRole;

            runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                navigateBasedOnRole(finalRole);
            });
        } catch (JSONException exception) {
            showInternalError();
        }
    }

    private void handleInvalidCredentials(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String message = json.optString("message", getString(R.string.auth_credentials_incorrect));
            runOnUiThread(() ->
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show()
            );
        } catch (JSONException exception) {
            showInternalError();
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if (AuthManager.ROLE_ADMIN.equals(role)) {
            intent = new Intent(this, AdminActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showInternalError() {
        runOnUiThread(() ->
                Toast.makeText(LoginActivity.this, R.string.auth_internal_error, Toast.LENGTH_SHORT).show()
        );
    }

    private void setLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
    }
}
