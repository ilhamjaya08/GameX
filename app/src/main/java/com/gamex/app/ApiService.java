package com.gamex.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gamex.app.models.CreateUserRequest;
import com.gamex.app.models.DepositRequest;
import com.gamex.app.models.DepositResponse;
import com.gamex.app.models.MessageResponse;
import com.gamex.app.models.MyTransactionsResponse;
import com.gamex.app.models.PaginatedTransactionsResponse;
import com.gamex.app.models.PaginatedUsersResponse;
import com.gamex.app.models.ProductResponse;
import com.gamex.app.models.ToggleRoleResponse;
import com.gamex.app.models.TransactionRequest;
import com.gamex.app.models.TransactionResponse;
import com.gamex.app.models.TransactionStatusResponse;
import com.gamex.app.models.UpdateUserRequest;
import com.gamex.app.models.User;
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

    public interface PaginatedTransactionsCallback {
        void onSuccess(PaginatedTransactionsResponse response);
        void onError(String errorMessage);
    }

    public interface PaginatedUsersCallback {
        void onSuccess(PaginatedUsersResponse response);
        void onError(String errorMessage);
    }

    public interface UserManagementCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface ToggleRoleCallback {
        void onSuccess(ToggleRoleResponse response);
        void onError(String errorMessage);
    }

    public interface MessageCallback {
        void onSuccess(String message);
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

    public void fetchAllTransactions(Context context, int page, PaginatedTransactionsCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getAllTransactionsPageEndpoint(page))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    PaginatedTransactionsResponse paginatedResponse = gson.fromJson(responseBody, PaginatedTransactionsResponse.class);

                    mainHandler.post(() -> callback.onSuccess(paginatedResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void fetchAdminUsers(Context context, int page, PaginatedUsersCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getAdminUsersPageEndpoint(page))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .build();

                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    PaginatedUsersResponse paginatedResponse = gson.fromJson(responseBody, PaginatedUsersResponse.class);

                    mainHandler.post(() -> callback.onSuccess(paginatedResponse));
                } else {
                    String errorMsg = "Error: " + response.code();
                    mainHandler.post(() -> callback.onError(errorMsg));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void updateUser(Context context, int userId, UpdateUserRequest updateRequest, UserManagementCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                String json = gson.toJson(updateRequest);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(json, JSON);

                Request request = new Request.Builder()
                    .url(ApiConfig.getAdminUserEndpoint(userId))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .put(body)
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    User user = gson.fromJson(responseBody, User.class);

                    mainHandler.post(() -> callback.onSuccess(user));
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

    public void toggleUserRole(Context context, int userId, ToggleRoleCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                RequestBody emptyBody = RequestBody.create("", null);

                Request request = new Request.Builder()
                    .url(ApiConfig.getAdminUserToggleRoleEndpoint(userId))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .patch(emptyBody)
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    ToggleRoleResponse toggleResponse = gson.fromJson(responseBody, ToggleRoleResponse.class);

                    mainHandler.post(() -> callback.onSuccess(toggleResponse));
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

    public void createUser(Context context, CreateUserRequest createRequest, UserManagementCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                String json = gson.toJson(createRequest);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(json, JSON);

                Request request = new Request.Builder()
                    .url(ApiConfig.getAdminUsersEndpoint())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .post(body)
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    User user = gson.fromJson(responseBody, User.class);

                    mainHandler.post(() -> callback.onSuccess(user));
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

    public void deleteUser(Context context, int userId, MessageCallback callback) {
        String token = AuthManager.getAccessToken(context);

        if (token == null) {
            mainHandler.post(() -> callback.onError("Not authenticated"));
            return;
        }

        executorService.execute(() -> {
            try {
                Request request = new Request.Builder()
                    .url(ApiConfig.getAdminUserEndpoint(userId))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .delete()
                    .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    MessageResponse messageResponse = gson.fromJson(responseBody, MessageResponse.class);

                    mainHandler.post(() -> callback.onSuccess(messageResponse.getMessage()));
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

    public void shutdown() {
        executorService.shutdown();
    }
}
