package com.gamex.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MyTransactionsResponse {
    @SerializedName("current_page")
    private int currentPage;

    @SerializedName("data")
    private List<Transaction> data;

    @SerializedName("first_page_url")
    private String firstPageUrl;

    @SerializedName("from")
    private Integer from;

    @SerializedName("last_page")
    private int lastPage;

    @SerializedName("last_page_url")
    private String lastPageUrl;

    @SerializedName("next_page_url")
    private String nextPageUrl;

    @SerializedName("path")
    private String path;

    @SerializedName("per_page")
    private int perPage;

    @SerializedName("prev_page_url")
    private String prevPageUrl;

    @SerializedName("to")
    private Integer to;

    @SerializedName("total")
    private int total;

    public int getCurrentPage() {
        return currentPage;
    }

    public List<Transaction> getData() {
        return data;
    }

    public String getFirstPageUrl() {
        return firstPageUrl;
    }

    public Integer getFrom() {
        return from;
    }

    public int getLastPage() {
        return lastPage;
    }

    public String getLastPageUrl() {
        return lastPageUrl;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public String getPath() {
        return path;
    }

    public int getPerPage() {
        return perPage;
    }

    public String getPrevPageUrl() {
        return prevPageUrl;
    }

    public Integer getTo() {
        return to;
    }

    public int getTotal() {
        return total;
    }

    public boolean hasNextPage() {
        return nextPageUrl != null;
    }

    public boolean hasPrevPage() {
        return prevPageUrl != null;
    }
}
