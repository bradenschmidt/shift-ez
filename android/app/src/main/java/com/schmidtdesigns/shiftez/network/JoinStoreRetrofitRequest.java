package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.PostResult;

import java.util.Map;

/**
 * Retrofit Request to enable a POST to the server with the given user id and key as storeParams.
 * Result codes and desc show error or success.
 * Can fail if key is invalid or key is too old.
 * If successful then the store found at the given key will be added to the user id's store.
 * Created by braden on 15-06-08.
 */
public class JoinStoreRetrofitRequest extends RetrofitSpiceRequest<PostResult, Api> {

    private String TAG = this.getClass().getSimpleName();
    private Map<String, String> mStoreParams;

    public JoinStoreRetrofitRequest(Map<String, String> storeParams) {
        super(PostResult.class, Api.class);
        mStoreParams = storeParams;
    }

    @Override
    public PostResult loadDataFromNetwork() throws Exception {
        return getService().shareStore(mStoreParams);

    }
}
