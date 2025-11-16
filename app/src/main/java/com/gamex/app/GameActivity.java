package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamex.app.models.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    static final String EXTRA_GAME_ID = "extra_game_id";
    private static final String TAG = "GameActivity";
    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    static {
        CURRENCY_FORMAT.setMaximumFractionDigits(0);
        CURRENCY_FORMAT.setMinimumFractionDigits(0);
    }

    private ImageView gameLogo;
    private TextView gameTitle;
    private TextView gameDescription;
    private TextInputLayout playerIdLayout;
    private TextInputLayout serverZoneLayout;
    private TextInputEditText playerIdInput;
    private TextInputEditText serverZoneInput;
    private MaterialButton submitButton;
    private TextView currentBalanceAmount;

    private ApiService apiService;
    private NominalAdapter nominalAdapter;
    private RecyclerView nominalRecyclerView;

    private NominalOption selectedNominal;
    private int categoryNo = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        apiService = new ApiService();

        initViews();
        setupRecyclerViews();
        setupListeners();
        loadBalance();

        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        if (gameId == null || gameId.isEmpty()) {
            Toast.makeText(this, R.string.game_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadGameData(gameId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiService != null) {
            apiService.shutdown();
        }
    }

    private void initViews() {
        gameLogo = findViewById(R.id.gameLogo);
        gameTitle = findViewById(R.id.gameTitle);
        gameDescription = findViewById(R.id.gameDescription);
        playerIdLayout = findViewById(R.id.playerIdLayout);
        serverZoneLayout = findViewById(R.id.serverZoneLayout);
        playerIdInput = findViewById(R.id.playerIdInput);
        serverZoneInput = findViewById(R.id.serverZoneInput);
        submitButton = findViewById(R.id.submitButton);
        currentBalanceAmount = findViewById(R.id.currentBalanceAmount);
    }

    private void setupRecyclerViews() {
        nominalRecyclerView = findViewById(R.id.nominalRecyclerView);
        nominalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nominalRecyclerView.setNestedScrollingEnabled(false);
        nominalRecyclerView.setItemAnimator(null);

        nominalAdapter = new NominalAdapter(option -> selectedNominal = option);
        nominalRecyclerView.setAdapter(nominalAdapter);
    }

    private void setupListeners() {
        MaterialButton checkButton = findViewById(R.id.checkButton);
        checkButton.setOnClickListener(v -> {
            if (playerIdInput.getText() == null || playerIdInput.getText().toString().trim().isEmpty()) {
                playerIdLayout.setError(getString(R.string.game_uid_required));
                return;
            }
            playerIdLayout.setError(null);
            Toast.makeText(this, R.string.game_username_check_placeholder, Toast.LENGTH_SHORT).show();
        });

        submitButton.setOnClickListener(v -> {
            if (!validateForm()) {
                return;
            }
            processTransaction();
        });

        if (playerIdInput != null) {
            playerIdInput.addTextChangedListener(new SimpleTextWatcher(() -> playerIdLayout.setError(null)));
        }
        if (serverZoneInput != null) {
            serverZoneInput.addTextChangedListener(new SimpleTextWatcher(() -> serverZoneLayout.setError(null)));
        }
    }

    private void loadGameData(@NonNull String gameId) {
        try (InputStream inputStream = getAssets().open("games.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONArray games = new JSONArray(jsonBuilder.toString());
            JSONObject matched = null;
            for (int i = 0; i < games.length(); i++) {
                JSONObject object = games.getJSONObject(i);
                String id = object.optString("id", object.optString("drawable"));
                if (gameId.equals(id)) {
                    matched = object;
                    break;
                }
            }

            if (matched == null) {
                Toast.makeText(this, R.string.game_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String name = matched.optString("name");
            String description = matched.optString("description");
            String drawableName = matched.optString("drawable");
            boolean requiresZone = matched.optBoolean("isUsingServerZone", false);
            categoryNo = matched.optInt("no", -1);

            if (!drawableName.isEmpty()) {
                int drawableRes = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                if (drawableRes != 0) {
                    gameLogo.setImageResource(drawableRes);
                } else {
                    Log.w(TAG, "Drawable not found for name: " + drawableName);
                }
            }

            gameTitle.setText(name);
            if (description == null || description.trim().isEmpty()) {
                gameDescription.setVisibility(View.GONE);
            } else {
                gameDescription.setVisibility(View.VISIBLE);
                gameDescription.setText(description);
            }
            serverZoneLayout.setVisibility(requiresZone ? View.VISIBLE : View.GONE);
            if (!requiresZone) {
                serverZoneLayout.setError(null);
                if (serverZoneInput != null) {
                    serverZoneInput.setText("");
                }
            }

            // Load products from API
            if (categoryNo != -1) {
                loadProducts(categoryNo);
            } else {
                Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | JSONException exception) {
            Log.e(TAG, "Failed to load game data", exception);
            Toast.makeText(this, R.string.auth_internal_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProducts(int categoryId) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.fetchProducts(categoryId, new ApiService.ProductCallback() {
            @Override
            public void onSuccess(com.gamex.app.models.ProductResponse productResponse) {
                if (productResponse.getProducts() != null && !productResponse.getProducts().isEmpty()) {
                    List<NominalOption> options = new ArrayList<>();
                    for (com.gamex.app.models.Product product : productResponse.getProducts()) {
                        options.add(new NominalOption(
                            String.valueOf(product.getId()),
                            product.getKeterangan(),
                            product.getHargaAsInt(),
                            product.getNama()
                        ));
                    }
                    Log.d(TAG, "Loaded " + options.size() + " products for category " + categoryId);
                    nominalAdapter.submitList(options);

                    // Force RecyclerView to expand to fit all items
                    // Each item is approximately 108dp (92dp minHeight + 16dp marginBottom from layout)
                    int itemHeightDp = 108;
                    float density = getResources().getDisplayMetrics().density;
                    int totalHeightPx = (int) (options.size() * itemHeightDp * density);

                    nominalRecyclerView.post(() -> {
                        ViewGroup.LayoutParams params = nominalRecyclerView.getLayoutParams();
                        params.height = totalHeightPx;
                        nominalRecyclerView.setLayoutParams(params);
                        nominalRecyclerView.requestLayout();
                    });
                } else {
                    Toast.makeText(GameActivity.this, "No products available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to load products: " + errorMessage);
                Toast.makeText(GameActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateForm() {
        String playerId = playerIdInput.getText() != null ? playerIdInput.getText().toString().trim() : "";
        if (playerId.isEmpty()) {
            playerIdLayout.setError(getString(R.string.game_uid_required));
            return false;
        }

        if (serverZoneLayout.getVisibility() == View.VISIBLE) {
            String zone = serverZoneInput.getText() != null ? serverZoneInput.getText().toString().trim() : "";
            if (zone.isEmpty()) {
                serverZoneLayout.setError(getString(R.string.game_zone_required));
                return false;
            }
        }

        if (selectedNominal == null) {
            Toast.makeText(this, R.string.game_nominal_not_selected, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void loadBalance() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
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
            }
        });
    }

    private void processTransaction() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String playerId = playerIdInput.getText() != null ? playerIdInput.getText().toString().trim() : "";
        int productId = selectedNominal != null ? Integer.parseInt(selectedNominal.id) : -1;

        if (productId == -1) {
            Toast.makeText(this, "Invalid product selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        submitButton.setEnabled(false);
        submitButton.setText("Processing...");

        apiService.createTransaction(this, productId, playerId, new ApiService.TransactionCallback() {
            @Override
            public void onSuccess(com.gamex.app.models.TransactionResponse transactionResponse) {
                submitButton.setEnabled(true);
                submitButton.setText("Submit");

                if (transactionResponse.getTransaction() != null) {
                    // Show loading animation for 3 seconds
                    Toast.makeText(GameActivity.this, "Transaction created successfully!", Toast.LENGTH_SHORT).show();

                    new android.os.Handler().postDelayed(() -> {
                        // Navigate to TransactionStatusActivity
                        Intent intent = new Intent(GameActivity.this, TransactionStatusActivity.class);
                        intent.putExtra("TRANSACTION_ID", transactionResponse.getTransaction().getId());
                        startActivity(intent);
                        finish();
                    }, 3000);
                }
            }

            @Override
            public void onError(String errorMessage, int statusCode) {
                submitButton.setEnabled(true);
                submitButton.setText("Submit");

                if (statusCode == 422) {
                    // Insufficient balance
                    DialogUtils.showErrorDialog(GameActivity.this, "Saldo Tidak Mencukupi", errorMessage);
                } else {
                    Toast.makeText(GameActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private static String formatCurrency(int amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    private static final class NominalOption {
        final String id;
        final String displayLabel;
        final int price;
        final String detail;

        NominalOption(String id, String displayLabel, int price, String detail) {
            this.id = id;
            this.displayLabel = displayLabel;
            this.price = price;
            this.detail = detail;
        }
    }

    private static final class NominalAdapter extends RecyclerView.Adapter<NominalAdapter.NominalViewHolder> {

        private final List<NominalOption> items = new ArrayList<>();
        private final OnNominalClickListener clickListener;
        @Nullable
        private String selectedId;

        NominalAdapter(OnNominalClickListener clickListener) {
            this.clickListener = clickListener;
        }

        void submitList(List<NominalOption> newItems) {
            items.clear();
            items.addAll(newItems);
            Log.d(TAG, "NominalAdapter: submitList called with " + newItems.size() + " items, total items now: " + items.size());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public NominalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nominal_option, parent, false);
            return new NominalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NominalViewHolder holder, int position) {
            NominalOption option = items.get(position);
            holder.bind(option, option.id.equals(selectedId));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        interface OnNominalClickListener {
            void onNominalClick(NominalOption option);
        }

        final class NominalViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView container;
            private final TextView label;
            private final TextView detail;
            private final TextView price;

            NominalViewHolder(@NonNull View itemView) {
                super(itemView);
                container = (MaterialCardView) itemView;
                label = itemView.findViewById(R.id.nominalLabel);
                detail = itemView.findViewById(R.id.nominalInfo);
                price = itemView.findViewById(R.id.nominalPrice);
                itemView.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    NominalOption option = items.get(adapterPosition);
                    selectedId = option.id;
                    notifyDataSetChanged();
                    clickListener.onNominalClick(option);
                });
            }

            void bind(NominalOption option, boolean isSelected) {
                label.setText(option.displayLabel);
                detail.setText(option.detail);
                price.setText(GameActivity.formatCurrency(option.price));

                int strokeColor = container.getContext().getColor(
                        isSelected ? R.color.gamex_green : R.color.gamex_card_stroke
                );
                container.setStrokeColor(strokeColor);
                container.setStrokeWidth(isSelected ? 3 : 1);
                container.setCardBackgroundColor(container.getContext().getColor(
                        isSelected ? R.color.gamex_surface_dark : R.color.gamex_surface_alt
                ));
            }
        }
    }

    private static final class SimpleTextWatcher implements TextWatcher {

        private final Runnable afterChange;

        SimpleTextWatcher(Runnable afterChange) {
            this.afterChange = afterChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // no-op
        }

        @Override
        public void afterTextChanged(Editable s) {
            afterChange.run();
        }
    }
}
