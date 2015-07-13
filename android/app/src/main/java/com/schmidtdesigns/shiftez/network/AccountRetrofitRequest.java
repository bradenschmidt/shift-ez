package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.Account;

/**
 * Created by braden on 15-06-08.
 */
public class AccountRetrofitRequest extends RetrofitSpiceRequest<Account.Response, Api> {

    private String TAG = this.getClass().getSimpleName();
    private String mUserId;

    public AccountRetrofitRequest(String userId) {
        super(Account.Response.class, Api.class);
        this.mUserId = userId;
    }

    @Override
    public Account.Response loadDataFromNetwork() throws Exception {
        return getService().getAccount(mUserId);
    }
}
