package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class TransactionStatusResponse {
    @SerializedName("transaction")
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }
}
