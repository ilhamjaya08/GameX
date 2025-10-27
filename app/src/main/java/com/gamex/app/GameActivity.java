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
import java.util.Arrays;
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

    private PaymentMethodAdapter paymentMethodAdapter;
    private NominalAdapter nominalAdapter;

    private String selectedPaymentId;
    private String selectedPaymentName;
    private NominalOption selectedNominal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        initViews();
        setupRecyclerViews();
        setupListeners();

        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);
        if (gameId == null || gameId.isEmpty()) {
            Toast.makeText(this, R.string.game_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadGameData(gameId);
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
    }

    private void setupRecyclerViews() {
        RecyclerView paymentRecyclerView = findViewById(R.id.paymentRecyclerView);
        paymentRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        paymentRecyclerView.setHasFixedSize(true);
        paymentRecyclerView.setItemAnimator(null);

        paymentMethodAdapter = new PaymentMethodAdapter(method -> {
            selectedPaymentId = method.id;
            selectedPaymentName = method.displayName;
        });
        paymentRecyclerView.setAdapter(paymentMethodAdapter);
        paymentMethodAdapter.submitList(createPaymentMethods());

        RecyclerView nominalRecyclerView = findViewById(R.id.nominalRecyclerView);
        nominalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        nominalRecyclerView.setHasFixedSize(true);
        nominalRecyclerView.setItemAnimator(null);

        nominalAdapter = new NominalAdapter(option -> selectedNominal = option);
        nominalRecyclerView.setAdapter(nominalAdapter);
        nominalAdapter.submitList(createNominalOptions());
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
            String nominalText = selectedNominal != null ? selectedNominal.displayLabel : "";
            String priceText = selectedNominal != null ? formatCurrency(selectedNominal.price) : "";
            Toast.makeText(
                    this,
                    getString(R.string.game_submit_success, gameTitle.getText(), nominalText, priceText, selectedPaymentName),
                    Toast.LENGTH_SHORT
            ).show();
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
        } catch (IOException | JSONException exception) {
            Log.e(TAG, "Failed to load game data", exception);
            Toast.makeText(this, R.string.auth_internal_error, Toast.LENGTH_SHORT).show();
            finish();
        }
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

        if (selectedPaymentId == null) {
            Toast.makeText(this, R.string.game_payment_not_selected, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedNominal == null) {
            Toast.makeText(this, R.string.game_nominal_not_selected, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private List<PaymentMethod> createPaymentMethods() {
        return Arrays.asList(
                new PaymentMethod("qris", "QRIS", R.drawable.logo_qris),
                new PaymentMethod("gopay", "GoPay", R.drawable.logo_gopay),
                new PaymentMethod("dana", "DANA", R.drawable.logo_dana),
                new PaymentMethod("ovo", "OVO", R.drawable.logo_ovo)
        );
    }

    private List<NominalOption> createNominalOptions() {
        return Arrays.asList(
                new NominalOption("diamond_5", "5 Diamonds", 1000, "Paket hemat harian."),
                new NominalOption("diamond_12", "12 Diamonds", 2000, "Boost stamina tambahan."),
                new NominalOption("diamond_70", "70 Diamonds", 10000, "Favorit buat weekly quest."),
                new NominalOption("diamond_355", "355 Diamonds", 50000, "Pas untuk battle pass premium."),
                new NominalOption("diamond_720", "720 Diamonds", 100000, "Satu langkah menuju koleksi langka.")
        );
    }

    private static String formatCurrency(int amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    private static final class PaymentMethod {
        final String id;
        final String displayName;
        final int iconRes;

        PaymentMethod(String id, String displayName, int iconRes) {
            this.id = id;
            this.displayName = displayName;
            this.iconRes = iconRes;
        }
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

    private static final class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

        private final List<PaymentMethod> items = new ArrayList<>();
        private final OnPaymentClickListener clickListener;
        @Nullable
        private String selectedId;

        PaymentMethodAdapter(OnPaymentClickListener clickListener) {
            this.clickListener = clickListener;
        }

        void submitList(List<PaymentMethod> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payment_method, parent, false);
            return new PaymentMethodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
            PaymentMethod method = items.get(position);
            holder.bind(method, method.id.equals(selectedId));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        interface OnPaymentClickListener {
            void onPaymentClick(PaymentMethod method);
        }

        final class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView container;
            private final ImageView icon;
            private final TextView title;

            PaymentMethodViewHolder(@NonNull View itemView) {
                super(itemView);
                container = (MaterialCardView) itemView;
                icon = itemView.findViewById(R.id.paymentIcon);
                title = itemView.findViewById(R.id.paymentName);
                itemView.setOnClickListener(v -> {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) {
                        return;
                    }
                    PaymentMethod method = items.get(adapterPosition);
                    selectedId = method.id;
                    notifyDataSetChanged();
                    clickListener.onPaymentClick(method);
                });
            }

            void bind(PaymentMethod method, boolean isSelected) {
                icon.setImageResource(method.iconRes);
                title.setText(method.displayName);
                container.setStrokeWidth(isSelected ? 3 : 1);
                container.setStrokeColor(container.getContext().getColor(
                        isSelected ? R.color.gamex_green : R.color.gamex_card_stroke
                ));
                container.setCardBackgroundColor(container.getContext().getColor(
                        isSelected ? R.color.gamex_surface_dark : R.color.gamex_surface_alt
                ));
                title.setTextColor(container.getContext().getColor(
                        isSelected ? R.color.gamex_text_primary : R.color.gamex_text_secondary
                ));
            }
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
