package com.gamex.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gamex.app.models.DepositRequest;
import com.gamex.app.models.DepositResponse;
import com.gamex.app.models.MyTransactionsResponse;
import com.gamex.app.models.ProductResponse;
import com.gamex.app.models.TransactionRequest;
import com.gamex.app.models.TransactionResponse;
import com.gamex.app.models.TransactionStatusResponse;
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

    public interface ProductCallback {
        void onSuccess(ProductResponse productResponse);
        void onError(String errorMessage);
    }

    public interface TransactionCallback {
        void onSuccess(TransactionResponse transactionResponse);
        void onError(String errorMessage, int statusCode);
    }

    public interface TransactionStatusCallback {
        void onSuccess(TransactionStatusResponse transactionStatusResponse);
        void onError(String errorMessage);
    }

    public interface MyTransactionsCallback {
        void onSuccess(MyTransactionsResponse myTransactionsResponse);
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

    public void fetchProducts(int categoryId, ProductCallback callback) {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getProductsEndpoint(categoryId))
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    android.util.Log.d("ApiService", "fetchProducts response for category " + categoryId + ": " + responseBody);
                    ProductResponse productResponse = gson.fromJson(responseBody, ProductResponse.class);

                    if (productResponse != null && productResponse.getProducts() != null) {
                        android.util.Log.d("ApiService", "Parsed " + productResponse.getProducts().size() + " products");
                    }

                    mainHandler.post(() -> callback.onSuccess(productResponse));
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
            } catch (Exception e) {
                android.util.Log.e("ApiService", "Error parsing products", e);
                mainHandler.post(() -> callback.onError("Parse error: " + e.getMessage()));
            }
        });
    }

    public void createTransaction(Context context, int productId, String targetId, TransactionCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated", 401));
            return;
        }

        executorService.execute(() -> {
            try {
                TransactionRequest transactionRequest = new TransactionRequest(productId, targetId);
                String json = gson.toJson(transactionRequest);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(json, JSON);

                Request request = new Request.Builder()
                    .url(ApiConfig.getTransactionsEndpoint())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

                Response response = client.newCall(request).execute();
                int statusCode = response.code();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    TransactionResponse transactionResponse = gson.fromJson(responseBody, TransactionResponse.class);

                    if (statusCode == 201 || statusCode == 200) {
                        mainHandler.post(() -> callback.onSuccess(transactionResponse));
                    } else {
                        mainHandler.post(() -> callback.onError(
                            transactionResponse.getMessage() != null ? transactionResponse.getMessage() : "Error: " + statusCode,
                            statusCode
                        ));
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Error: " + statusCode, statusCode));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage(), -1));
            }
        });
    }

    public void fetchTransactionStatus(Context context, int transactionId, TransactionStatusCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getTransactionStatusEndpoint(transactionId))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    TransactionStatusResponse transactionStatusResponse = gson.fromJson(responseBody, TransactionStatusResponse.class);

                    mainHandler.post(() -> callback.onSuccess(transactionStatusResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void refreshTransactionStatus(Context context, int transactionId, TransactionStatusCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getTransactionRefreshStatusEndpoint(transactionId))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    TransactionStatusResponse transactionStatusResponse = gson.fromJson(responseBody, TransactionStatusResponse.class);

                    mainHandler.post(() -> callback.onSuccess(transactionStatusResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void fetchMyTransactions(Context context, MyTransactionsCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getMyTransactionsEndpoint())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    MyTransactionsResponse myTransactionsResponse = gson.fromJson(responseBody, MyTransactionsResponse.class);

                    mainHandler.post(() -> callback.onSuccess(myTransactionsResponse));
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
