package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gamex.app.models.CreateUserRequest;
import com.gamex.app.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateUserActivity extends AppCompatActivity {

    private ApiService apiService;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText phoneInput;
    private RadioGroup roleRadioGroup;
    private MaterialButton createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        apiService = new ApiService();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        createButton = findViewById(R.id.createButton);

        createButton.setOnClickListener(v -> createUser());
    }

    private void createUser() {
        // Clear errors
        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

        // Determine role
        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
        String role = "user";
        if (selectedRoleId == R.id.roleAdminRadio) {
            role = "admin";
        }

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.create_user_name_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.create_user_email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.create_user_email_invalid));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.create_user_password_required));
            isValid = false;
        } else if (password.length() < 8) {
            passwordInputLayout.setError(getString(R.string.create_user_password_length));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> createUser());
            return;
        }

        createButton.setEnabled(false);

        CreateUserRequest createRequest = new CreateUserRequest(
            name,
            email,
            password,
            phone.isEmpty() ? null : phone,
            role
        );

        apiService.createUser(this, createRequest, new ApiService.UserManagementCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(CreateUserActivity.this,
                    getString(R.string.create_user_success), Toast.LENGTH_SHORT).show();

                // Navigate back to ManageUsersActivity
                Intent intent = new Intent(CreateUserActivity.this, ManageUsersActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                createButton.setEnabled(true);
                Toast.makeText(CreateUserActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
