package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.Expose;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

/**
 * Created by braden on 15-06-30.
 */
public class StoreResponse {

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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
