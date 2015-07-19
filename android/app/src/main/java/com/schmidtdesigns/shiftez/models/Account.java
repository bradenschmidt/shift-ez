package com.schmidtdesigns.shiftez.models;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by braden on 15-06-22.
 */
public class Account {
    @Expose
    @SerializedName("user_name")
    private String name;
    @Expose
    @SerializedName("user_id")
    private String email;
    @Expose
    @SerializedName("user_image_url")
    private String userImageUrl;
    @Expose
    @SerializedName("storeDeps")
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

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public ArrayList<Store> getStores() {
        return mStores;
    }

    public void setStores(ArrayList<Store> mStores) {
        this.mStores = mStores;
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

    public Store getStoreByStoreDep(String storeName, String depName) {
        for (Store store : mStores) {
            if (store.getStoreName().equals(storeName) && store.getDepName().equals(depName)) {
                return store;
            }
        }
        return null;
    }

    public static class Response {
        @Expose
        private Account account;

        /**
         *
         * @return
         * The account
         */
        public Account getAccount() {
            return account;
        }

        /**
         *
         * @param account
         * The account
         */
        public void setAccount(Account account) {
            this.account = account;
        }
    }
}
