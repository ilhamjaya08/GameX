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

import com.gamex.app.models.Transaction;
import com.gamex.app.models.TransactionStatusResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class TransactionStatusActivity extends AppCompatActivity {

    private static final String TAG = "TransactionStatusActivity";

    private ApiService apiService;
    private int transactionId;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    private MaterialCardView statusCard;
    private MaterialCardView detailsCard;

    private ImageView statusIcon;
    private TextView statusText;
    private TextView statusMessage;
    private TextView productName;
    private TextView transactionAmount;
    private TextView targetId;
    private TextView providerTrxId;
    private MaterialButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_status);

        apiService = new ApiService();

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);

        if (transactionId == -1) {
            Toast.makeText(this, "Invalid transaction ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        loadTransactionStatus();
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
        detailsCard = findViewById(R.id.detailsCard);

        statusIcon = findViewById(R.id.statusIcon);
        statusText = findViewById(R.id.statusText);
        statusMessage = findViewById(R.id.statusMessage);
        productName = findViewById(R.id.productName);
        transactionAmount = findViewById(R.id.transactionAmount);
        targetId = findViewById(R.id.targetId);
        providerTrxId = findViewById(R.id.providerTrxId);
        refreshButton = findViewById(R.id.refreshButton);

        swipeRefreshLayout.setColorSchemeResources(R.color.gamex_green);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshTransactionStatus);
        refreshButton.setOnClickListener(v -> refreshTransactionStatus());
    }

    private void loadTransactionStatus() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadTransactionStatus());
            return;
        }

        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        apiService.fetchTransactionStatus(this, transactionId, new ApiService.TransactionStatusCallback() {
            @Override
            public void onSuccess(TransactionStatusResponse transactionStatusResponse) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (transactionStatusResponse.getTransaction() != null) {
                    Transaction transaction = transactionStatusResponse.getTransaction();
                    updateUI(transaction);
                } else {
                    Toast.makeText(TransactionStatusActivity.this,
                        "Failed to load transaction status",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Toast.makeText(TransactionStatusActivity.this,
                    "Error: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshTransactionStatus() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> refreshTransactionStatus());
            return;
        }

        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        apiService.refreshTransactionStatus(this, transactionId, new ApiService.TransactionStatusCallback() {
            @Override
            public void onSuccess(TransactionStatusResponse transactionStatusResponse) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (transactionStatusResponse.getTransaction() != null) {
                    Transaction transaction = transactionStatusResponse.getTransaction();
                    updateUI(transaction);
                } else {
                    Toast.makeText(TransactionStatusActivity.this,
                        "Failed to refresh transaction status",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Toast.makeText(TransactionStatusActivity.this,
                    "Error: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Transaction transaction) {
        // Update product details
        if (transaction.getProduct() != null) {
            productName.setText(transaction.getProduct().getKeterangan());
        } else {
            productName.setText("-");
        }

        transactionAmount.setText(CurrencyUtils.formatToRupiah(transaction.getAmountAsInt()));
        targetId.setText(transaction.getTargetId() != null ? transaction.getTargetId() : "-");
        providerTrxId.setText(transaction.getProviderTrxId() != null ? transaction.getProviderTrxId() : "-");

        // Update status
        String status = transaction.getStatus();
        if (status != null) {
            switch (status) {
                case "pending":
                    showPendingStatus();
                    break;
                case "paid":
                    showPaidStatus();
                    break;
                case "process":
                    showProcessStatus();
                    break;
                case "success":
                    showSuccessStatus();
                    break;
                case "failed":
                    showFailedStatus();
                    break;
                case "refund":
                    showRefundStatus();
                    break;
                default:
                    showUnknownStatus(status);
                    break;
            }
        }
    }

    private void showPendingStatus() {
        statusText.setText("Pending");
        statusMessage.setText("Your transaction is waiting for payment");
        statusText.setTextColor(getResources().getColor(R.color.gamex_green, null));
        refreshButton.setVisibility(View.VISIBLE);
    }

    private void showPaidStatus() {
        statusText.setText("Paid");
        statusMessage.setText("Payment received, processing your order");
        statusText.setTextColor(getResources().getColor(R.color.gamex_green, null));
        refreshButton.setVisibility(View.VISIBLE);
    }

    private void showProcessStatus() {
        statusText.setText("Processing");
        statusMessage.setText("Your transaction is being processed");
        statusText.setTextColor(getResources().getColor(R.color.gamex_green, null));
        refreshButton.setVisibility(View.VISIBLE);
    }

    private void showSuccessStatus() {
        statusText.setText("Success");
        statusMessage.setText("Your transaction completed successfully!");
        statusText.setTextColor(getResources().getColor(R.color.gamex_green, null));
        refreshButton.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(TransactionStatusActivity.this,
                "Transaction successful! Redirecting...",
                Toast.LENGTH_SHORT).show();
            finish();
        }, 3000);
    }

    private void showFailedStatus() {
        statusText.setText("Failed");
        statusMessage.setText("Your transaction has failed");
        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));
        refreshButton.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(this::finish, 3000);
    }

    private void showRefundStatus() {
        statusText.setText("Refunded");
        statusMessage.setText("Your transaction has been refunded");
        statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_light, null));
        refreshButton.setVisibility(View.GONE);

        new android.os.Handler().postDelayed(this::finish, 3000);
    }

    private void showUnknownStatus(String status) {
        statusText.setText(status);
        statusMessage.setText("Transaction status: " + status);
        statusText.setTextColor(getResources().getColor(R.color.gamex_text_secondary, null));
        refreshButton.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        refreshButton.setEnabled(!show);
    }
}
