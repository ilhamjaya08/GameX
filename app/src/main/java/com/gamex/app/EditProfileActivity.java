package com.gamex.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ApiService apiService;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        apiService = new ApiService();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);

        MaterialButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveProfile());

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
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadProfile());
            return;
        }

        apiService.fetchUserBalance(this, new ApiService.UserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                if (userResponse != null && userResponse.getUser() != null) {
                    nameInput.setText(userResponse.getUser().getName());
                    emailInput.setText(userResponse.getUser().getEmail());
                    phoneInput.setText(userResponse.getUser().getPhone());
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EditProfileActivity.this, R.string.profile_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        // Dummy implementation - just show success message
        Toast.makeText(this, R.string.edit_profile_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
