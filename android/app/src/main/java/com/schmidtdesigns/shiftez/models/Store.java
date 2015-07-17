package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Store {

    @Expose
    private ArrayList<Schedule> schedules = new ArrayList<>();
    @SerializedName("store_name")
    @Expose
    private String storeName;
    @SerializedName("dep_name")
    @Expose
    private String depName;
    @SerializedName("user_id")
    @Expose
    private String userId;

    /**
     *
     * @return
     * The userId
     */
    public String getUserId() {
        return userId;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getDepName() {
        return depName;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public ArrayList<Schedule> getSortedSchedules() {
        ArrayList<Schedule> sortedSchedules = schedules;
        Collections.sort(sortedSchedules, new Comparator<Schedule>() {
            public int compare(Schedule schedule1, Schedule schedule2) {
                return schedule1.getYear().compareTo(schedule2.getYear());
            }
        });
        Collections.sort(sortedSchedules, new Comparator<Schedule>() {
            public int compare(Schedule schedule1, Schedule schedule2) {
                return schedule1.getWeek().compareTo(schedule2.getWeek());
            }
        });


        return sortedSchedules;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public class Response {
        @Expose
        private ArrayList<Store> stores = new ArrayList<>();

        /**
         *
         * @return
         * The stores
         */
        public ArrayList<Store> getStores() {
            return stores;
        }

        /**
         *
         * @param stores
         * The stores
         */
        public void setStores(ArrayList<Store> stores) {
            this.stores = stores;
        }

        public ArrayList<Schedule> getAllSchedules() {
            ArrayList<Schedule> allSchedules = new ArrayList<>();
            for (Store store : stores) {
                allSchedules.addAll(store.getSchedules());
            }
            return allSchedules;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}