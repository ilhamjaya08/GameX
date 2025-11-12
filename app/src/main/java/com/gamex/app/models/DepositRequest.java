package com.gamex.app.models;

public class DepositRequest {
    private int amount;

    public DepositRequest(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
