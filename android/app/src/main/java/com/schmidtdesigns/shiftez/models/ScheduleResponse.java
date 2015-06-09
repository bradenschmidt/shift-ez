package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by braden on 15-06-09.
 */
public class ScheduleResponse {

    @SerializedName("schedules")
    private Schedule.List schedules;

    public Schedule.List getSchedule() {
        return schedules;
    }

    public void setSchedule(Schedule.List schedules) {
        this.schedules = schedules;
    }

    @Override
    public String toString() {
        return "ScheduleResponse{" +
                "schedules=" + schedules +
                '}';
    }
}
