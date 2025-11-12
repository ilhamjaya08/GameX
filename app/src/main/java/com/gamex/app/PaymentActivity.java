package com.gamex.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gamex.app.models.Deposit;
import com.gamex.app.models.DepositResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    private ApiService apiService;
    private int depositId;
    private int totalAmount;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    private MaterialCardView statusCard;
    private MaterialCardView amountCard;
    private MaterialCardView qrisCard;
    private MaterialCardView instructionsCard;

    private ImageView statusIcon;
    private TextView statusText;
    private TextView statusMessage;
    private TextView totalAmountValue;
    private TextView baseAmountValue;
    private TextView uniqueCodeValue;
    private MaterialButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        apiService = new ApiService();

        depositId = getIntent().getIntExtra("DEPOSIT_ID", -1);
        totalAmount = getIntent().getIntExtra("TOTAL_AMOUNT", 0);

        if (depositId == -1) {
            Toast.makeText(this, "Invalid deposit ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        loadPaymentStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }

    private void initializeViews() {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        statusCard = findViewById(R.id.statusCard);
        amountCard = findViewById(R.id.amountCard);
        qrisCard = findViewById(R.id.qrisCard);
        instructionsCard = findViewById(R.id.instructionsCard);

        statusIcon = findViewById(R.id.statusIcon);
        statusText = findViewById(R.id.statusText);
        statusMessage = findViewById(R.id.statusMessage);
        totalAmountValue = findViewById(R.id.totalAmountValue);
        baseAmountValue = findViewById(R.id.baseAmountValue);
        uniqueCodeValue = findViewById(R.id.uniqueCodeValue);
        refreshButton = findViewById(R.id.refreshButton);

        swipeRefreshLayout.setColorSchemeResources(R.color.gamex_green);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPaymentStatus);
        refreshButton.setOnClickListener(v -> loadPaymentStatus());
    }

    private void loadPaymentStatus() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadPaymentStatus());
            return;
        }

        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        apiService.refreshDepositStatus(this, depositId, new ApiService.DepositCallback() {
            @Override
            public void onSuccess(DepositResponse depositResponse) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (depositResponse.getData() != null && depositResponse.getData().getDeposit() != null) {
                    Deposit deposit = depositResponse.getData().getDeposit();
                    updateUI(deposit, depositResponse.getData().getStatus());
                } else {
                    Toast.makeText(PaymentActivity.this,
                        "Failed to load payment status",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Toast.makeText(PaymentActivity.this,
                    "Error: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Deposit deposit, String status) {
        totalAmountValue.setText(CurrencyUtils.formatToRupiah(deposit.getTotalAmountAsInt()));
        baseAmountValue.setText(CurrencyUtils.formatToRupiah(deposit.getAmountAsInt()));
        uniqueCodeValue.setText(CurrencyUtils.formatToRupiah(deposit.getRandomAmount()));

        if ("pending".equals(status) || deposit.isPending()) {
            showPendingStatus();
        } else if ("success".equals(status) || deposit.isSuccess()) {
            showSuccessStatus(deposit);
        } else if ("cancelled".equals(status) || deposit.isCancelled()) {
            showCancelledStatus();
        }
    }

    private void showPendingStatus() {
        statusText.setText(R.string.payment_status_pending);
        statusMessage.setText(R.string.payment_status_pending_message);
        statusText.setTextColor(getResources().getColor(R.color.gamex_green, null));

        qrisCard.setVisibility(View.VISIBLE);
        instructionsCard.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
    }

    private void showSuccessStatus(Deposit deposit) {
        statusText.setText(R.string.payment_status_success);
        statusMessage.setText(getString(R.string.payment_status_success_message,
            CurrencyUtils.formatToRupiah(deposit.getAmountAsInt())));
        statusText.setTextColor(getResources().getColor(R.color.gamex_green, null));

        qrisCard.setVisibility(View.GONE);
        instructionsCard.setVisibility(View.GONE);
        refreshButton.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(PaymentActivity.this,
                R.string.payment_success_redirect,
                Toast.LENGTH_SHORT).show();
            finish();
        }, 3000);
    }

    private void showCancelledStatus() {
        statusText.setText(R.string.payment_status_cancelled);
        statusMessage.setText(R.string.payment_status_cancelled_message);
        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));

        qrisCard.setVisibility(View.GONE);
        instructionsCard.setVisibility(View.GONE);
        refreshButton.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(this::finish, 3000);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        refreshButton.setEnabled(!show);
    }
}
