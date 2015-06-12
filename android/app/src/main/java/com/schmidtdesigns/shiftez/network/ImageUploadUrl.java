package com.schmidtdesigns.shiftez.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by braden on 11/06/15.
 */
public class ImageUploadUrl {
    @SerializedName("upload_url")
    @Expose
    private String uploadUrl;

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String upload_url) {
        this.uploadUrl = upload_url;
    }
}
