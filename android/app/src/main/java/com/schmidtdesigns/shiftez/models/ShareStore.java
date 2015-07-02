package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.Expose;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by braden on 15-07-02.
 */
public class ShareStore extends PostResult {

    @Expose
    private String key;

    /**
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key The key
     */
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
