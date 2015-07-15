package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Store {

    @Expose
    private List<Schedule> schedules = new ArrayList<>();
    @SerializedName("store_name")
    @Expose
    private String storeName;
    @SerializedName("dep_name")
    @Expose
    private String depName;
    @SerializedName("user_id")
    @Expose
    private String userId;

    /**
     *
     * @return
     * The userId
     */
    public String getUserId() {
        return userId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getDepName() {
        return depName;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}