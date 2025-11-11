package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ApiService apiService;
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (AuthManager.isLoggedIn(this)) {
            apiService = new ApiService();
            fetchAndValidateRole();
            return;
        }

        showLandingScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }

    private void fetchAndValidateRole() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            navigateBasedOnCachedRole();
            return;
        }

        apiService.fetchUserBalance(this, new ApiService.UserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                if (userResponse != null && userResponse.getUser() != null) {
                    String apiRole = userResponse.getUser().getRole();
                    String cachedRole = AuthManager.getUserRole(MainActivity.this);

                    if (!TextUtils.isEmpty(apiRole) && !apiRole.equals(cachedRole)) {
                        Log.d(TAG, "Role changed from " + cachedRole + " to " + apiRole + ", updating...");
                        AuthManager.saveUserRole(MainActivity.this, apiRole);
                        cachedRole = apiRole;
                    }

                    navigateBasedOnRole(cachedRole);
                } else {
                    navigateBasedOnCachedRole();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to fetch user role: " + errorMessage);
                navigateBasedOnCachedRole();
            }
        });
    }

    private void navigateBasedOnCachedRole() {
        String cachedRole = AuthManager.getUserRole(this);
        navigateBasedOnRole(cachedRole);
    }

    private void navigateBasedOnRole(String role) {
        if (isNavigating) return;
        isNavigating = true;

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

    private void showLandingScreen() {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton registerButton = findViewById(R.id.registerButton);
        MaterialButton loginButton = findViewById(R.id.loginButton);

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class))
        );

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
