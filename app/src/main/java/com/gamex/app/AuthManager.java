package com.gamex.app;

import android.content.Context;
import android.content.SharedPreferences;

public final class AuthManager {

    private static final String PREFS_NAME = "gamex_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private AuthManager() {
        // Utility class.
    }

    public static void saveAccessToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public static boolean isLoggedIn(Context context) {
        return getAccessToken(context) != null;
    }

    public static void clearAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
    }
}
