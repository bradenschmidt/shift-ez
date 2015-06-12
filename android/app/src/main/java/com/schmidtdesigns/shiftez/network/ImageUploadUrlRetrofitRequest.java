package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

/**
 * Created by braden on 15-06-08.
 */
public class ImageUploadUrlRetrofitRequest extends RetrofitSpiceRequest<ImageUploadUrl, Api> {

    private String TAG = "ImageUploadURLRetrofitRequest";

    public ImageUploadUrlRetrofitRequest() {
        super(ImageUploadUrl.class, Api.class);

    }

    @Override
    public ImageUploadUrl loadDataFromNetwork() throws Exception {

        return getService().getImageUploadURL();
    }
}
