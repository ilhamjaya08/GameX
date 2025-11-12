package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class Deposit {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("amount")
    private String amount;

    @SerializedName("random_amount")
    private int randomAmount;

    @SerializedName("total_amount")
    private String totalAmount;

    @SerializedName("qris_code")
    private String qrisCode;

    @SerializedName("qris_image")
    private String qrisImage;

    @SerializedName("status")
    private String status;

    @SerializedName("paid_at")
    private String paidAt;

    @SerializedName("cancelled_at")
    private String cancelledAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
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

    public int getRandomAmount() {
        return randomAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public int getTotalAmountAsInt() {
        try {
            return (int) Double.parseDouble(totalAmount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getQrisCode() {
        return qrisCode;
    }

    public String getQrisImage() {
        return qrisImage;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isSuccess() {
        return "success".equals(status);
    }

    public boolean isCancelled() {
        return "cancelled".equals(status);
    }

    public String getPaidAt() {
        return paidAt;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
