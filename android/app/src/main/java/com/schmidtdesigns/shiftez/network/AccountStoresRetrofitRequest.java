package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.Store;

/**
 * Created by braden on 15-06-08.
 */
public class AccountStoresRetrofitRequest extends RetrofitSpiceRequest<Store.Response, Api> {

    private String TAG = this.getClass().getSimpleName();
    private String mUserId;

    public AccountStoresRetrofitRequest(String userId) {
        super(Store.Response.class, Api.class);
        this.mUserId = userId;
    }

    @Override
    public Store.Response loadDataFromNetwork() throws Exception {
        return getService().getAccountStores(mUserId);

    }
}
