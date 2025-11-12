package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DepositResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private DepositData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public DepositData getData() {
        return data;
    }

    public static class DepositData {
        @SerializedName("deposit")
        private Deposit deposit;

        @SerializedName("qris_image_url")
        private String qrisImageUrl;

        @SerializedName("instructions")
        private List<String> instructions;

        @SerializedName("status")
        private String status;

        public Deposit getDeposit() {
            return deposit;
        }

        public String getQrisImageUrl() {
            return qrisImageUrl;
        }

        public List<String> getInstructions() {
            return instructions;
        }

        public String getStatus() {
            return status;
        }
    }
}
