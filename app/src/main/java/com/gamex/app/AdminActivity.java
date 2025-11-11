package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private long lastBackPressedTime = 0L;
    private ApiService apiService;
    private TextView adminName;
    private TextView totalUsers;
    private TextView totalRevenue;
    private TextView totalOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        apiService = new ApiService();

        adminName = findViewById(R.id.adminName);
        totalUsers = findViewById(R.id.totalUsers);
        totalRevenue = findViewById(R.id.totalRevenue);
        totalOrders = findViewById(R.id.totalOrders);

        ImageView accountButton = findViewById(R.id.adminAccountButton);
        accountButton.setOnClickListener(v -> openProfileActivity());

        CardView usersCard = findViewById(R.id.usersCard);
        CardView revenueCard = findViewById(R.id.revenueCard);
        CardView ordersCard = findViewById(R.id.ordersCard);
        CardView gamesCard = findViewById(R.id.gamesCard);

        usersCard.setOnClickListener(v ->
                Toast.makeText(this, "Users Management", Toast.LENGTH_SHORT).show()
        );
        revenueCard.setOnClickListener(v ->
                Toast.makeText(this, "Revenue Reports", Toast.LENGTH_SHORT).show()
        );
        ordersCard.setOnClickListener(v ->
                Toast.makeText(this, "Orders Management", Toast.LENGTH_SHORT).show()
        );
        gamesCard.setOnClickListener(v ->
                Toast.makeText(this, "Games Management", Toast.LENGTH_SHORT).show()
        );

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
        totalUsers.setText("1,234");
        totalRevenue.setText(CurrencyUtils.formatToRupiah(15000000));
        totalOrders.setText("5,678");
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
