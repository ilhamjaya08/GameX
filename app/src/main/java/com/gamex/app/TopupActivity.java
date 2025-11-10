package com.gamex.app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamex.app.models.PaymentMethod;
import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TopupActivity extends AppCompatActivity {

    private static final String TAG = "TopupActivity";

    private ApiService apiService;
    private TextView currentBalanceAmount;
    private TextInputEditText topupAmountInput;
    private PaymentMethodAdapter paymentMethodAdapter;
    private MaterialButton submitButton;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_topup);

        apiService = new ApiService();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        currentBalanceAmount = findViewById(R.id.currentBalanceAmount);
        topupAmountInput = findViewById(R.id.topupAmountInput);
        submitButton = findViewById(R.id.submitTopupButton);

        RecyclerView paymentMethodRecyclerView = findViewById(R.id.paymentMethodRecyclerView);
        paymentMethodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodAdapter = new PaymentMethodAdapter(this::onPaymentMethodSelected);
        paymentMethodRecyclerView.setAdapter(paymentMethodAdapter);

        loadPaymentMethods();
        loadBalance();

        submitButton.setOnClickListener(v -> handleTopupSubmit());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void loadBalance() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetDialog();
            currentBalanceAmount.setText(R.string.home_balance_error);
            return;
        }

        currentBalanceAmount.setText(R.string.home_balance_loading);

        apiService.fetchUserBalance(this, new ApiService.UserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                if (userResponse != null && userResponse.getUser() != null) {
                    int balance = userResponse.getUser().getBalanceAsInt();
                    String formattedBalance = CurrencyUtils.formatToRupiah(balance);
                    currentBalanceAmount.setText(formattedBalance);
                }
            }

            @Override
            public void onError(String errorMessage) {
                currentBalanceAmount.setText(R.string.home_balance_error);
                Toast.makeText(TopupActivity.this, R.string.home_balance_error_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPaymentMethods() {
        List<PaymentMethod> methods = new ArrayList<>();

        methods.add(new PaymentMethod(
            "qris",
            "QRIS",
            "Bayar dengan scan QR Code",
            R.drawable.logo_qris,
            true
        ));

        methods.add(new PaymentMethod(
            "gopay",
            "GoPay",
            "Bayar dengan GoPay",
            R.drawable.logo_gopay,
            false
        ));

        methods.add(new PaymentMethod(
            "dana",
            "DANA",
            "Bayar dengan DANA",
            R.drawable.logo_dana,
            false
        ));

        methods.add(new PaymentMethod(
            "ovo",
            "OVO",
            "Bayar dengan OVO",
            R.drawable.logo_ovo,
            false
        ));

        paymentMethodAdapter.submitList(methods);
    }

    private void onPaymentMethodSelected(PaymentMethod paymentMethod) {
        // Payment method selected
    }

    private void handleTopupSubmit() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showNoInternetDialog();
            return;
        }

        String amountText = topupAmountInput.getText() != null ?
            topupAmountInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(amountText)) {
            Toast.makeText(this, R.string.topup_amount_required, Toast.LENGTH_SHORT).show();
            topupAmountInput.requestFocus();
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.topup_amount_invalid, Toast.LENGTH_SHORT).show();
            topupAmountInput.requestFocus();
            return;
        }

        if (amount < 10000) {
            Toast.makeText(this, R.string.topup_amount_minimum, Toast.LENGTH_SHORT).show();
            topupAmountInput.requestFocus();
            return;
        }

        PaymentMethod selectedMethod = paymentMethodAdapter.getSelectedPaymentMethod();
        if (selectedMethod == null) {
            Toast.makeText(this, R.string.topup_payment_required, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingDialog();

        // Simulate topup submission
        new android.os.Handler().postDelayed(() -> {
            hideLoadingDialog();
            showSuccessDialog(amount);
        }, 2000);
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage(getString(R.string.topup_processing));
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showSuccessDialog(int amount) {
        DialogUtils.showSuccessDialog(
            this,
            getString(R.string.topup_success_title),
            getString(R.string.topup_success_message, CurrencyUtils.formatToRupiah(amount)),
            (dialog, which) -> finish()
        );
    }

    private void showNoInternetDialog() {
        DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadBalance());
    }
}
