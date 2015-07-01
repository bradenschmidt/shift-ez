package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.StoreResponse;

/**
 * Created by braden on 15-06-08.
 */
public class StoreRetrofitRequest extends RetrofitSpiceRequest<StoreResponse, Api> {

    private String TAG = "ScheduleRetrofitRequest";
    private String mUserId;

    public StoreRetrofitRequest(String userId) {
        super(StoreResponse.class, Api.class);
        this.mUserId = userId;
    }

    @Override
    public StoreResponse loadDataFromNetwork() throws Exception {
        return getService().getStores(mUserId);

    }
}
