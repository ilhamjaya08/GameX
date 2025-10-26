package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

    private long lastBackPressedTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        AuthManager.clearAccessToken(getApplicationContext());
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBackPressedTime < 2000) {
            finishAffinity();
        } else {
            lastBackPressedTime = currentTime;
            Toast.makeText(this, R.string.home_back_press_exit, Toast.LENGTH_SHORT).show();
        }
    }
}
