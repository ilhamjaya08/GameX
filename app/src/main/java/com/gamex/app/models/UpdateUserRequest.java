package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("balance")
    private String balance;

    public UpdateUserRequest() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
