package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.PostAccount;

import java.util.Map;

/**
 * Created by braden on 15-07-03.
 */
public class AddAccountRetrofitRequest extends RetrofitSpiceRequest<PostAccount, Api> {
    private String TAG = this.getClass().getSimpleName();

    private Map<String, String> mAccountParams;
    private String mUserId;

    public AddAccountRetrofitRequest(Map<String, String> accountParams, String userId) {
        super(PostAccount.class, Api.class);
        this.mAccountParams = accountParams;
        mUserId = userId;
    }

    @Override
    public PostAccount loadDataFromNetwork() throws Exception {
        return getService().addAccount(mUserId, mAccountParams);
    }

}
