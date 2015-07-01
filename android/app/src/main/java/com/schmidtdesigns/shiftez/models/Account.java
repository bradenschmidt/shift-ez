package com.schmidtdesigns.shiftez.models;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by braden on 15-06-22.
 */
public class Account {
    private String name;
    private String email;
    private ArrayList<Store> mStores;

    public Account(String name, String email, ArrayList<Store> stores) {
        this.name = name;
        this.email = email;
        this.mStores = stores;
    }

    /**
     * Serialize an Account object into a json string.
     */
    public static String serializeToJson(Account a) {
        Gson gson = new Gson();
        return gson.toJson(a);
    }

    /**
     * Use gson to deserialize json string to Account object.
     */
    public static Account deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Account.class);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Store> getStores() {
        return mStores;
    }

    public ArrayList<String> getStoresAsStrings() {
        ArrayList<String> storesStr = new ArrayList<>();

        for (Store s : mStores) {
            storesStr.add(s.getStoreName());
        }
        return storesStr;
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", stores=" + mStores +
                '}';
    }
}
