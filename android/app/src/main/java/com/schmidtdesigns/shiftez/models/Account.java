package com.schmidtdesigns.shiftez.models;

import com.google.gson.Gson;

/**
 * Created by braden on 15-06-22.
 */
public class Account {
    private String name;
    private String email;

    public Account(String name, String email) {
        this.name = name;
        this.email = email;
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

    // Serialize a single object.
    public static String serializeToJson(Account a) {
        Gson gson = new Gson();
        return gson.toJson(a);
    }

    //Using gson to deserialize to a single object.
    // Deserialize to single object.
    public static Account deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Account.class);
    }
}
