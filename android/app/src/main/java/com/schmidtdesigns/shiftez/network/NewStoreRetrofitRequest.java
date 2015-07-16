package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.PostResult;

import java.util.Map;

/**
 * Created by braden on 15-06-08.
 */
public class NewStoreRetrofitRequest extends RetrofitSpiceRequest<PostResult, Api> {

    private String TAG = this.getClass().getSimpleName();

    private String mUserId;
    private Map<String, String> mStoreParams;

    public NewStoreRetrofitRequest(String userId, Map<String, String> storeParams) {
        super(PostResult.class, Api.class);
        this.mUserId = userId;
        this.mStoreParams = storeParams;
    }

    @Override
    public PostResult loadDataFromNetwork() throws Exception {

        return getService().addNewStore(mUserId, mStoreParams);
    }
}
