package com.gamex.app;

import android.content.Context;
import android.content.SharedPreferences;

public final class AuthManager {

    private static final String PREFS_NAME = "gamex_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ROLE = "user_role";

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

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

    public static void saveUserRole(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ROLE, role).apply();
    }

    public static String getUserRole(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ROLE, ROLE_USER);
    }

    public static boolean isLoggedIn(Context context) {
        return getAccessToken(context) != null;
    }

    public static boolean isAdmin(Context context) {
        return ROLE_ADMIN.equals(getUserRole(context));
    }

    public static void clearAuth(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_USER_ROLE)
                .apply();
    }

    public static void clearAccessToken(Context context) {
        clearAuth(context);
    }
}
