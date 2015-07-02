package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.ShareStore;

import java.util.Map;

/**
 * Retrofit Request to enable a POST to the server with the given store name and dep as storeParams.
 * Returns a key in {@link ShareStore} which can be shared to other users to allow them to join the
 * store.
 * Created by braden on 15-06-08.
 */
public class ShareStoreRetrofitRequest extends RetrofitSpiceRequest<ShareStore, Api> {

    private String TAG = this.getClass().getSimpleName();
    private Map<String, String> mStoreParams;

    public ShareStoreRetrofitRequest(Map<String, String> storeParams) {
        super(ShareStore.class, Api.class);
        mStoreParams = storeParams;
    }

    @Override
    public ShareStore loadDataFromNetwork() throws Exception {
        return getService().shareStore(mStoreParams);

    }
}
