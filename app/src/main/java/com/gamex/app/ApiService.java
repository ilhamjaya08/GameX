package com.gamex.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gamex.app.models.DepositRequest;
import com.gamex.app.models.DepositResponse;
import com.gamex.app.models.UserResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final OkHttpClient client;
    private final Gson gson;

    public ApiService() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public interface UserCallback {
        void onSuccess(UserResponse userResponse);
        void onError(String errorMessage);
    }

    public interface DepositCallback {
        void onSuccess(DepositResponse depositResponse);
        void onError(String errorMessage);
    }

    public void fetchUserBalance(Context context, UserCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getBalanceEndpoint())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    UserResponse userResponse = gson.fromJson(responseBody, UserResponse.class);

                    mainHandler.post(() -> callback.onSuccess(userResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    if (response.body() != null) {
                        errorMsg += " - " + response.body().string();
                    }
                    final String finalErrorMsg = errorMsg;
                    mainHandler.post(() -> callback.onError(finalErrorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void createDeposit(Context context, int amount, DepositCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                DepositRequest depositRequest = new DepositRequest(amount);
                String json = gson.toJson(depositRequest);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(json, JSON);

                Request request = new Request.Builder()
                    .url(ApiConfig.getDepositsEndpoint())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    DepositResponse depositResponse = gson.fromJson(responseBody, DepositResponse.class);

                    mainHandler.post(() -> callback.onSuccess(depositResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    if (response.body() != null) {
                        errorMsg += " - " + response.body().string();
                    }
                    final String finalErrorMsg = errorMsg;
                    mainHandler.post(() -> callback.onError(finalErrorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void refreshDepositStatus(Context context, int depositId, DepositCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getDepositStatusEndpoint(depositId))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    DepositResponse depositResponse = gson.fromJson(responseBody, DepositResponse.class);

                    mainHandler.post(() -> callback.onSuccess(depositResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
