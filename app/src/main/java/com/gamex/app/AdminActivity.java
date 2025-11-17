package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.gamex.app.models.PaginatedTransactionsResponse;
import com.gamex.app.models.PaginatedUsersResponse;
import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private long lastBackPressedTime = 0L;
    private ApiService apiService;
    private TextView adminName;
    private TextView totalUsers;
    private TextView totalOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        apiService = new ApiService();

        adminName = findViewById(R.id.adminName);
        totalUsers = findViewById(R.id.totalUsers);
        totalOrders = findViewById(R.id.totalOrders);

        ImageView accountButton = findViewById(R.id.adminAccountButton);
        accountButton.setOnClickListener(v -> openProfileActivity());

        MaterialCardView viewAllTransactionsCard = findViewById(R.id.viewAllTransactionsCard);
        MaterialCardView manageAllUsersCard = findViewById(R.id.manageAllUsersCard);
        MaterialCardView createUserCard = findViewById(R.id.createUserCard);

        viewAllTransactionsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllTransactionsActivity.class);
            startActivity(intent);
        });

        manageAllUsersCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageUsersActivity.class);
            startActivity(intent);
        });

        createUserCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateUserActivity.class);
            startActivity(intent);
        });

        TextView footerText = findViewById(R.id.adminFooterText);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        footerText.setText(getString(R.string.home_footer, year));

        loadAdminProfile();
        loadDashboardStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }

    private void loadAdminProfile() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            adminName.setText(R.string.admin_welcome_error);
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadAdminProfile());
            return;
        }

        apiService.fetchUserBalance(this, new ApiService.UserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                if (userResponse != null && userResponse.getUser() != null) {
                    String name = userResponse.getUser().getName();
                    adminName.setText(getString(R.string.admin_welcome, name != null ? name : "Admin"));
                }
            }

            @Override
            public void onError(String errorMessage) {
                adminName.setText(R.string.admin_welcome_error);
            }
        });
    }

    private void loadDashboardStats() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            totalUsers.setText("0");
            totalOrders.setText("0");
            return;
        }

        // Load total transactions
        apiService.fetchAllTransactions(this, 1, new ApiService.PaginatedTransactionsCallback() {
            @Override
            public void onSuccess(PaginatedTransactionsResponse response) {
                if (response != null) {
                    totalOrders.setText(String.valueOf(response.getTotal()));
                }
            }

            @Override
            public void onError(String errorMessage) {
                totalOrders.setText("0");
            }
        });

        // Load total users
        apiService.fetchAdminUsers(this, 1, new ApiService.PaginatedUsersCallback() {
            @Override
            public void onSuccess(PaginatedUsersResponse response) {
                if (response != null) {
                    totalUsers.setText(String.valueOf(response.getTotal()));
                }
            }

            @Override
            public void onError(String errorMessage) {
                totalUsers.setText("0");
            }
        });
    }

    private void openProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
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
