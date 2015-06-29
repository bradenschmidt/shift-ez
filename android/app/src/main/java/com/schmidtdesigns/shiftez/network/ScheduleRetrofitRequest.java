package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.ShiftEZ;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;

import java.util.HashMap;

/**
 * Created by braden on 15-06-08.
 */
public class ScheduleRetrofitRequest extends RetrofitSpiceRequest<ScheduleResponse, Api> {

    private final boolean mReverse;
    private String TAG = "ScheduleRetrofitRequest";
    private int mYear = -1;

    public ScheduleRetrofitRequest(Boolean reverse) {
        super(ScheduleResponse.class, Api.class);
        this.mReverse = reverse;
    }

    public ScheduleRetrofitRequest(int year, Boolean reverse) {
        super(ScheduleResponse.class, Api.class);
        this.mYear = year;
        this.mReverse = reverse;
    }

    @Override
    public ScheduleResponse loadDataFromNetwork() throws Exception {
        HashMap<String, String> scheduleParams = new HashMap<>();
        scheduleParams.put("user_id", ShiftEZ.getInstance().getAccount().getEmail());
        scheduleParams.put("reverse", String.valueOf(mReverse));

        if (mYear == -1) {
            return getService().getSchedules(scheduleParams);
        } else {
            return getService().getSchedules(mYear, scheduleParams);
        }
    }
}
