package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;

/**
 * Created by braden on 15-06-08.
 */
public class ImageUploadUrlRetrofitRequest extends RetrofitSpiceRequest<ImageUploadUrl, Api> {

    private String TAG = this.getClass().getSimpleName();
    private String mUserId;

    public ImageUploadUrlRetrofitRequest(String userId) {
        super(ImageUploadUrl.class, Api.class);

        mUserId = userId;
    }

    @Override
    public ImageUploadUrl loadDataFromNetwork() throws Exception {
        return getService().getImageUploadURL(mUserId);
    }
}
