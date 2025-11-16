package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gamex.app.models.MyTransactionsResponse;
import com.gamex.app.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "OrdersActivity";

    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    private RecyclerView ordersRecyclerView;
    private TextView emptyText;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);

        apiService = new ApiService();

        initializeViews();
        setupListeners();
        loadTransactions();
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
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(transaction -> {
            // Navigate to TransactionStatusActivity
            Intent intent = new Intent(OrdersActivity.this, TransactionStatusActivity.class);
            intent.putExtra("TRANSACTION_ID", transaction.getId());
            startActivity(intent);
        });
        ordersRecyclerView.setAdapter(transactionAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.gamex_green);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadTransactions);
    }

    private void loadTransactions() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadTransactions());
            return;
        }

        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading(true);
        }

        apiService.fetchMyTransactions(this, new ApiService.MyTransactionsCallback() {
            @Override
            public void onSuccess(MyTransactionsResponse myTransactionsResponse) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (myTransactionsResponse.getData() != null && !myTransactionsResponse.getData().isEmpty()) {
                    emptyText.setVisibility(View.GONE);
                    ordersRecyclerView.setVisibility(View.VISIBLE);
                    transactionAdapter.submitList(myTransactionsResponse.getData());
                } else {
                    emptyText.setVisibility(View.VISIBLE);
                    ordersRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Toast.makeText(OrdersActivity.this,
                    "Error: " + errorMessage,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

        private final List<Transaction> transactions = new ArrayList<>();
        private final OnTransactionClickListener clickListener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        interface OnTransactionClickListener {
            void onTransactionClick(Transaction transaction);
        }

        TransactionAdapter(OnTransactionClickListener clickListener) {
            this.clickListener = clickListener;
        }

        void submitList(List<Transaction> newTransactions) {
            transactions.clear();
            transactions.addAll(newTransactions);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            Transaction transaction = transactions.get(position);
            holder.bind(transaction, dateFormat);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class TransactionViewHolder extends RecyclerView.ViewHolder {
            private final TextView productName;
            private final TextView statusBadge;
            private final TextView amount;
            private final TextView target;
            private final TextView date;

            TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                productName = itemView.findViewById(R.id.transactionProductName);
                statusBadge = itemView.findViewById(R.id.transactionStatusBadge);
                amount = itemView.findViewById(R.id.transactionAmount);
                target = itemView.findViewById(R.id.transactionTarget);
                date = itemView.findViewById(R.id.transactionDate);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onTransactionClick(transactions.get(position));
                    }
                });
            }

            void bind(Transaction transaction, SimpleDateFormat dateFormat) {
                if (transaction.getProduct() != null) {
                    productName.setText(transaction.getProduct().getKeterangan());
                } else {
                    productName.setText("Unknown Product");
                }

                amount.setText(CurrencyUtils.formatToRupiah(transaction.getAmountAsInt()));
                target.setText(transaction.getTargetId() != null ? transaction.getTargetId() : "-");

                // Format status
                String status = transaction.getStatus();
                if (status != null) {
                    statusBadge.setText(status.toUpperCase(Locale.ROOT));

                    // Set badge color based on status
                    int backgroundColor;
                    if (transaction.isSuccess()) {
                        backgroundColor = itemView.getContext().getColor(R.color.gamex_green);
                    } else if (transaction.isFailed() || transaction.isRefund()) {
                        backgroundColor = itemView.getContext().getColor(android.R.color.holo_red_light);
                    } else {
                        backgroundColor = itemView.getContext().getColor(android.R.color.holo_orange_light);
                    }
                    statusBadge.setBackgroundColor(backgroundColor);
                } else {
                    statusBadge.setText("UNKNOWN");
                }

                // Format date
                try {
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date createdDate = apiFormat.parse(transaction.getCreatedAt());
                    if (createdDate != null) {
                        date.setText(dateFormat.format(createdDate));
                    } else {
                        date.setText(transaction.getCreatedAt());
                    }
                } catch (Exception e) {
                    date.setText(transaction.getCreatedAt());
                }
            }
        }
    }
}
