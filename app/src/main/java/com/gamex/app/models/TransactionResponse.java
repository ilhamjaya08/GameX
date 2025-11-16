package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("transaction")
    private Transaction transaction;

    @SerializedName("new_balance")
    private String newBalance;

    // For insufficient balance error
    @SerializedName("required")
    private String required;

    @SerializedName("current_balance")
    private String currentBalance;

    @SerializedName("shortage")
    private int shortage;

    public String getMessage() {
        return message;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getNewBalance() {
        return newBalance;
    }

    public String getRequired() {
        return required;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    public int getShortage() {
        return shortage;
    }

    public boolean isInsufficientBalance() {
        return shortage > 0;
    }
}
