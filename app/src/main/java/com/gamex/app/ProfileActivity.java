package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ApiService apiService;
    private TextView profileName;
    private TextView profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        apiService = new ApiService();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);

        LinearLayout editProfileButton = findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(v -> openEditProfile());

        LinearLayout aboutButton = findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(v -> openAbout());

        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        loadProfile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }

    private void loadProfile() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            profileName.setText(R.string.profile_error);
            profileEmail.setText("");
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadProfile());
            return;
        }

        profileName.setText(R.string.profile_loading);
        profileEmail.setText("");

        apiService.fetchUserBalance(this, new ApiService.UserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                if (userResponse != null && userResponse.getUser() != null) {
                    String name = userResponse.getUser().getName();
                    String email = userResponse.getUser().getEmail();

                    profileName.setText(name != null && !name.isEmpty() ? name : "User");
                    profileEmail.setText(email != null && !email.isEmpty() ? email : "");
                }
            }

            @Override
            public void onError(String errorMessage) {
                profileName.setText(R.string.profile_error);
                profileEmail.setText("");
                Toast.makeText(ProfileActivity.this, R.string.profile_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_logout_confirm_title)
                .setMessage(R.string.profile_logout_confirm_message)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.alert_cancel, null)
                .show();
    }

    private void performLogout() {
        AuthManager.clearAccessToken(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
