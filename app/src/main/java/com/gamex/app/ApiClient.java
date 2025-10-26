package com.gamex.app;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Small helper for making JSON POST requests without adding extra dependencies.
 */
public final class ApiClient {

    private static final int TIMEOUT_MS = 15000;

    private ApiClient() {
        // No instances.
    }

    public static ApiResponse postJson(String urlString, JSONObject payload) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body);
                os.flush();
            }

            int statusCode = connection.getResponseCode();
            InputStream stream = statusCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream()
                    : connection.getInputStream();
            String responseBody = stream != null ? readStream(stream) : "";
            return new ApiResponse(statusCode, responseBody);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public static final class ApiResponse {
        public final int statusCode;
        public final String body;

        ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
