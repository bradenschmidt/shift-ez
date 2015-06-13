package com.schmidtdesigns.shiftez.models;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by braden on 11/06/15.
 */
public class ImageUploadUrl {
    private static final String TAG = "ImageUploadUrl";
    @SerializedName("upload_url")
    @Expose
    private String uploadUrl;

    public String getPath() {
        URL url = null;
        try {
            url = new URL(uploadUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String urlStr = url.getPath().replace("%2F", "/");
        urlStr = urlStr.substring(1);

        Log.i(TAG, urlStr);

        return urlStr;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String upload_url) {
        this.uploadUrl = upload_url;
    }
}
