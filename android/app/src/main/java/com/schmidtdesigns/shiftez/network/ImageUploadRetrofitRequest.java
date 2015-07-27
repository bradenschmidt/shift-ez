package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.ImageUploadUrl;
import com.schmidtdesigns.shiftez.models.PostResult;

import java.util.Map;

import retrofit.mime.TypedFile;

/**
 * Created by braden on 15-06-08.
 */
public class ImageUploadRetrofitRequest extends RetrofitSpiceRequest<PostResult, Api> {

    private String TAG = this.getClass().getSimpleName();

    private Map<String, String> mImageParams;
    private TypedFile mImage;
    private ImageUploadUrl mImageUploadUrl;

    public ImageUploadRetrofitRequest(ImageUploadUrl imageUploadUrl,
                                      TypedFile image,
                                      Map<String, String> imageParams) {
        super(PostResult.class, Api.class);
        this.mImageParams = imageParams;
        this.mImage = image;
        this.mImageUploadUrl = imageUploadUrl;
    }

    @Override
    public PostResult loadDataFromNetwork() throws Exception {
        return getService().uploadImage(mImageUploadUrl.getPath(), mImage, mImageParams);
    }
}
