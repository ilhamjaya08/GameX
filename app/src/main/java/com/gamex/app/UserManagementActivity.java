package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gamex.app.models.ToggleRoleResponse;
import com.gamex.app.models.UpdateUserRequest;
import com.gamex.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UserManagementActivity extends AppCompatActivity {

    private ApiService apiService;
    private SwipeRefreshLayout swipeRefresh;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout balanceInputLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText balanceInput;
    private TextView roleText;
    private MaterialButton toggleRoleButton;
    private MaterialButton updateButton;
    private MaterialButton deleteButton;

    private int userId;
    private String currentRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        apiService = new ApiService();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        balanceInputLayout = findViewById(R.id.balanceInputLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        balanceInput = findViewById(R.id.balanceInput);
        roleText = findViewById(R.id.roleText);
        toggleRoleButton = findViewById(R.id.toggleRoleButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);

        swipeRefresh.setEnabled(true);
        swipeRefresh.setOnRefreshListener(this::loadUserData);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", 0);
        String userName = intent.getStringExtra("user_name");
        String userEmail = intent.getStringExtra("user_email");
        String userPhone = intent.getStringExtra("user_phone");
        currentRole = intent.getStringExtra("user_role");
        String userBalance = intent.getStringExtra("user_balance");

        if (userId == 0) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate fields
        nameInput.setText(userName);
        emailInput.setText(userEmail);
        phoneInput.setText(userPhone);
        balanceInput.setText(userBalance);
        updateRoleDisplay();

        updateButton.setOnClickListener(v -> updateUser());
        toggleRoleButton.setOnClickListener(v -> toggleUserRole());
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void loadUserData() {
        swipeRefresh.setRefreshing(false);
        // Data sudah dimuat dari intent, jadi tidak perlu reload dari API
        // Kecuali jika ada perubahan
    }

    private void updateRoleDisplay() {
        if ("admin".equals(currentRole)) {
            roleText.setText(getString(R.string.manage_users_role_admin));
            toggleRoleButton.setText(getString(R.string.user_management_make_user));
        } else {
            roleText.setText(getString(R.string.manage_users_role_user));
            toggleRoleButton.setText(getString(R.string.user_management_make_admin));
        }
    }

    private void updateUser() {
        // Clear errors
        nameInputLayout.setError(null);
        emailInputLayout.setError(null);

        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        String balance = balanceInput.getText() != null ? balanceInput.getText().toString().trim() : "";

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.user_management_name_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.user_management_email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.user_management_email_invalid));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> updateUser());
            return;
        }

        updateButton.setEnabled(false);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName(name);
        updateRequest.setEmail(email);
        if (!TextUtils.isEmpty(phone)) {
            updateRequest.setPhone(phone);
        }
        if (!TextUtils.isEmpty(balance)) {
            updateRequest.setBalance(balance);
        }

        apiService.updateUser(this, userId, updateRequest, new ApiService.UserManagementCallback() {
            @Override
            public void onSuccess(User user) {
                updateButton.setEnabled(true);
                Toast.makeText(UserManagementActivity.this,
                    getString(R.string.user_management_update_success), Toast.LENGTH_SHORT).show();

                // Update current data
                if (user.getRole() != null) {
                    currentRole = user.getRole();
                    updateRoleDisplay();
                }
            }

            @Override
            public void onError(String errorMessage) {
                updateButton.setEnabled(true);
                Toast.makeText(UserManagementActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleUserRole() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> toggleUserRole());
            return;
        }

        toggleRoleButton.setEnabled(false);

        apiService.toggleUserRole(this, userId, new ApiService.ToggleRoleCallback() {
            @Override
            public void onSuccess(ToggleRoleResponse response) {
                toggleRoleButton.setEnabled(true);

                if (response != null && response.getUser() != null) {
                    currentRole = response.getUser().getRole();
                    updateRoleDisplay();
                    Toast.makeText(UserManagementActivity.this,
                        getString(R.string.user_management_toggle_role_success), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                toggleRoleButton.setEnabled(true);
                Toast.makeText(UserManagementActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.user_management_delete_confirm_title))
            .setMessage(getString(R.string.user_management_delete_confirm_message))
            .setPositiveButton(getString(R.string.alert_ok), (dialog, which) -> deleteUser())
            .setNegativeButton(getString(R.string.alert_cancel), null)
            .show();
    }

    private void deleteUser() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> deleteUser());
            return;
        }

        deleteButton.setEnabled(false);

        apiService.deleteUser(this, userId, new ApiService.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(UserManagementActivity.this,
                    getString(R.string.user_management_delete_success), Toast.LENGTH_SHORT).show();

                // Navigate back to ManageUsersActivity
                Intent intent = new Intent(UserManagementActivity.this, ManageUsersActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                deleteButton.setEnabled(true);
                Toast.makeText(UserManagementActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }
}
