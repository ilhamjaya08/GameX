package com.gamex.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.registerEmailInput);
        passwordInput = findViewById(R.id.registerPasswordInput);

        MaterialButton registerButton = findViewById(R.id.registerSubmitButton);
        registerButton.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError(getString(R.string.auth_email_required));
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError(getString(R.string.auth_password_required));
            passwordInput.requestFocus();
            return;
        }

        Toast.makeText(this, R.string.register_placeholder_message, Toast.LENGTH_SHORT).show();
    }
}
