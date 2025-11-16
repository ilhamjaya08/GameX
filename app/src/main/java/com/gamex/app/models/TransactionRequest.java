package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    @SerializedName("product_id")
    private int productId;

    @SerializedName("target_id")
    private String targetId;

    public TransactionRequest(int productId, String targetId) {
        this.productId = productId;
        this.targetId = targetId;
    }

    public int getProductId() {
        return productId;
    }

    public String getTargetId() {
        return targetId;
    }
}
