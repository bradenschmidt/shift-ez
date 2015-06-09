package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.ScheduleResponse;

import java.util.HashMap;

/**
 * Created by braden on 15-06-08.
 */
public class ScheduleRetrofitRequest extends RetrofitSpiceRequest<ScheduleResponse, Api> {

    private final int year;
    private String TAG = "ScheduleRetrofitRequest";



    public ScheduleRetrofitRequest(int year) {
        super(ScheduleResponse.class, Api.class);
        this.year = year;
    }

    @Override
    public ScheduleResponse loadDataFromNetwork() throws Exception {
        HashMap<String,String> scheduleParams = new HashMap<String, String>();
        scheduleParams.put("year", String.valueOf(year));

         return getService().getSchedules(scheduleParams);
    }
}
