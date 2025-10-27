package com.gamex.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int GRID_SPAN_COUNT = 3;

    private long lastBackPressedTime = 0L;
    private GameAdapter gameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> handleLogout());

        ImageView ordersButton = findViewById(R.id.ordersButton);
        ImageView accountButton = findViewById(R.id.accountButton);
        ordersButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.home_action_orders, Toast.LENGTH_SHORT).show()
        );
        accountButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.home_action_account, Toast.LENGTH_SHORT).show()
        );

        TextView footerText = findViewById(R.id.footerText);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        footerText.setText(getString(R.string.home_footer, year));

        RecyclerView gameRecyclerView = findViewById(R.id.gameRecyclerView);
        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, GRID_SPAN_COUNT));
        gameRecyclerView.setHasFixedSize(true);
        int spacing = getResources().getDimensionPixelSize(R.dimen.game_tile_spacing);
        gameRecyclerView.addItemDecoration(new GridSpacingItemDecoration(GRID_SPAN_COUNT, spacing));
        gameRecyclerView.setItemAnimator(null);
        gameAdapter = new GameAdapter(this::openGameDetail);
        gameRecyclerView.setAdapter(gameAdapter);

        loadGames();
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

    private void loadGames() {
        try (InputStream inputStream = getAssets().open("games.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONArray array = new JSONArray(jsonBuilder.toString());
            List<GameItem> items = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String drawableName = object.optString("drawable");
                String id = object.optString("id", drawableName);
                String name = object.optString("name");
                int drawableRes = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                if (drawableRes == 0) {
                    Log.w(TAG, "Drawable not found for name: " + drawableName);
                    continue;
                }
                items.add(new GameItem(id, name, drawableRes));
            }
            gameAdapter.submitList(items);
        } catch (IOException | JSONException exception) {
            Log.e(TAG, "Failed to load games", exception);
            Toast.makeText(this, R.string.auth_internal_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void openGameDetail(GameItem item) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_GAME_ID, item.id);
        startActivity(intent);
    }
}
