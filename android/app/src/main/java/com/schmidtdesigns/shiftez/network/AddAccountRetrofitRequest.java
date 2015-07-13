package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.PostResult;

import java.util.Map;

/**
 * Created by braden on 15-07-03.
 */
public class AddAccountRetrofitRequest extends RetrofitSpiceRequest<PostResult, Api> {
    private String TAG = this.getClass().getSimpleName();

    private Map<String, String> mAccountParams;

    public AddAccountRetrofitRequest(Map<String, String> accountParams) {
        super(PostResult.class, Api.class);
        this.mAccountParams = accountParams;
    }

    @Override
    public PostResult loadDataFromNetwork() throws Exception {

        return getService().addAccount(mAccountParams);
    }

}
