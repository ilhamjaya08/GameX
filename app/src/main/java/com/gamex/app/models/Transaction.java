package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("product_id")
    private int productId;

    @SerializedName("target_id")
    private String targetId;

    @SerializedName("server_id")
    private String serverId;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("amount")
    private String amount;

    @SerializedName("status")
    private String status;

    @SerializedName("provider_trx_id")
    private String providerTrxId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("product")
    private Product product;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getServerId() {
        return serverId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getAmount() {
        return amount;
    }

    public int getAmountAsInt() {
        try {
            return (int) Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getStatus() {
        return status;
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isPaid() {
        return "paid".equals(status);
    }

    public boolean isProcess() {
        return "process".equals(status);
    }

    public boolean isSuccess() {
        return "success".equals(status);
    }

    public boolean isFailed() {
        return "failed".equals(status);
    }

    public boolean isRefund() {
        return "refund".equals(status);
    }

    public String getProviderTrxId() {
        return providerTrxId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Product getProduct() {
        return product;
    }
}
