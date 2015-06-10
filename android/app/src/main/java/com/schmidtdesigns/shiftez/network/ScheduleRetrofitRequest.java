package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;

import java.util.HashMap;

/**
 * Created by braden on 15-06-08.
 */
public class ScheduleRetrofitRequest extends RetrofitSpiceRequest<ScheduleResponse, Api> {

    private String TAG = "ScheduleRetrofitRequest";

    private final int mYear;
    private final boolean mReverse;

    public ScheduleRetrofitRequest(int year, Boolean reverse) {
        super(ScheduleResponse.class, Api.class);
        this.mYear = year;
        this.mReverse = reverse;
    }

    @Override
    public ScheduleResponse loadDataFromNetwork() throws Exception {
        HashMap<String,String> scheduleParams = new HashMap<String, String>();
        scheduleParams.put("year", String.valueOf(mYear));
        scheduleParams.put("reverse", String.valueOf(mReverse));

         return getService().getSchedules(scheduleParams);
    }
}
