package com.gamex.app;

public final class ApiConfig {

    private static final String BASE_URL = "https://api.amazon.web.id/";

    private ApiConfig() {
        // Utility class.
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getBalanceEndpoint() {
        return BASE_URL + "api/auth/me";
    }

    public static String getProfileEndpoint() {
        return BASE_URL + "api/auth/me";
    }

    public static String getTopupEndpoint() {
        return BASE_URL + "api/topup";
    }

    public static String getDepositsEndpoint() {
        return BASE_URL + "api/deposits";
    }

    public static String getDepositStatusEndpoint(int depositId) {
        return BASE_URL + "api/deposits/" + depositId + "/refresh-status";
    }

    public static String getProductsEndpoint(int categoryId) {
        return BASE_URL + "api/categories/" + categoryId + "/products";
    }

    public static String getTransactionsEndpoint() {
        return BASE_URL + "api/transactions";
    }

    public static String getTransactionStatusEndpoint(int transactionId) {
        return BASE_URL + "api/transactions/" + transactionId;
    }

    public static String getTransactionRefreshStatusEndpoint(int transactionId) {
        return BASE_URL + "api/transactions/" + transactionId + "/refresh-status";
    }

    public static String getMyTransactionsEndpoint() {
        return BASE_URL + "api/transactions/my";
    }
}
