package com.schmidtdesigns.shiftez.network;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.schmidtdesigns.shiftez.models.Schedule;

/**
 * Created by braden on 15-06-08.
 */
public class ScheduleRetrofitRequest extends RetrofitSpiceRequest<Schedule.Response, Api> {
    private final boolean mReverse;
    private String TAG = this.getClass().getSimpleName();
    private int mYear = -1;
    private String mUserId;

    public ScheduleRetrofitRequest(String userId, Boolean reverse) {
        super(Schedule.Response.class, Api.class);
        this.mUserId = userId;
        this.mReverse = reverse;
    }

    public ScheduleRetrofitRequest(String userId, int year, Boolean reverse) {
        super(Schedule.Response.class, Api.class);
        this.mUserId = userId;
        this.mYear = year;
        this.mReverse = reverse;
    }

    @Override
    public Schedule.Response loadDataFromNetwork() throws Exception {
        if (mYear == -1) {
            return getService().getSchedules(mUserId, mReverse);
        } else {
            return getService().getSchedules(mUserId, mYear, mReverse);
        }
    }
}
