package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;

    @SerializedName("kode")
    private String kode;

    @SerializedName("nama")
    private String nama;

    @SerializedName("keterangan")
    private String keterangan;

    @SerializedName("harga")
    private String harga;

    @SerializedName("status")
    private boolean status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("category_id")
    private int categoryId;

    public int getId() {
        return id;
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public String getHarga() {
        return harga;
    }

    public int getHargaAsInt() {
        try {
            return (int) Double.parseDouble(harga);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getCategoryId() {
        return categoryId;
    }
}
