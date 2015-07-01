package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Store {

    @Expose
    private List<String> deps = new ArrayList<>();
    @SerializedName("store")
    @Expose
    private String mStoreName;
    @SerializedName("user_id")
    @Expose
    private String userId;

    public Store(String storeName, ArrayList<String> deps) {
        this.mStoreName = storeName;
        this.deps = deps;
    }

    /**
     *
     * @return
     * The deps
     */
    public List<String> getDeps() {
        return deps;
    }

    /**
     *
     * @param deps
     * The deps
     */
    public void setDeps(List<String> deps) {
        this.deps = deps;
    }

    /**
     *
     * @return
     * The storeName
     */
    public String getStoreName() {
        return mStoreName;
    }

    /**
     *
     * @param storeName
     * The storeName
     */
    public void setStoreName(String storeName) {
        this.mStoreName = storeName;
    }

    /**
     *
     * @return
     * The userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     * The user_id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}