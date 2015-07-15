package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.StoreResponse;

/**
 * Created by braden on 15-06-08.
 */
public class AccountStoresRetrofitRequest extends RetrofitSpiceRequest<StoreResponse, Api> {

    private String TAG = this.getClass().getSimpleName();
    private String mUserId;

    public AccountStoresRetrofitRequest(String userId) {
        super(StoreResponse.class, Api.class);
        this.mUserId = userId;
    }

    @Override
    public StoreResponse loadDataFromNetwork() throws Exception {
        return getService().getAccountStores(mUserId);

    }
}
