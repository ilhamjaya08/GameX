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
}
