package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {
    @SerializedName("category")
    private Category category;

    @SerializedName("products")
    private List<Product> products;

    public Category getCategory() {
        return category;
    }

    public List<Product> getProducts() {
        return products;
    }

    public static class Category {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("code")
        private String code;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("products")
        private List<Product> products;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public List<Product> getProducts() {
            return products;
        }
    }
}
