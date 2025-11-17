package com.gamex.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gamex.app.models.PaginatedTransactionsResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class AllTransactionsActivity extends AppCompatActivity {

    private ApiService apiService;
    private TransactionAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ProgressBar loadingView;
    private MaterialButton btnPrevious;
    private MaterialButton btnNext;
    private TextView pageInfo;

    private int currentPage = 1;
    private int lastPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        apiService = new ApiService();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.transactionsRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        loadingView = findViewById(R.id.loadingView);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        pageInfo = findViewById(R.id.pageInfo);

        adapter = new TransactionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> loadTransactions(currentPage));

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                loadTransactions(currentPage - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < lastPage) {
                loadTransactions(currentPage + 1);
            }
        });

        loadTransactions(1);
    }

    private void loadTransactions(int page) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefresh.setRefreshing(false);
            DialogUtils.showNoInternetDialog(this, (dialog, which) -> loadTransactions(page));
            return;
        }

        if (!swipeRefresh.isRefreshing()) {
            loadingView.setVisibility(View.VISIBLE);
        }
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        apiService.fetchAllTransactions(this, page, new ApiService.PaginatedTransactionsCallback() {
            @Override
            public void onSuccess(PaginatedTransactionsResponse response) {
                swipeRefresh.setRefreshing(false);
                loadingView.setVisibility(View.GONE);

                if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                    adapter.setTransactions(response.getData());
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);

                    currentPage = response.getCurrentPage();
                    lastPage = response.getLastPage();

                    updatePaginationControls();
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                swipeRefresh.setRefreshing(false);
                loadingView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(AllTransactionsActivity.this,
                    getString(R.string.all_transactions_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePaginationControls() {
        pageInfo.setText(getString(R.string.pagination_page_info, currentPage, lastPage));
        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < lastPage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }
}
