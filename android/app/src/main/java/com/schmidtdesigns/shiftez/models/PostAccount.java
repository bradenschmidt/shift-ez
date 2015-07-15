package com.schmidtdesigns.shiftez.models;

/**
 * Created by braden on 15-07-14.
 */
public class PostAccount extends PostResult {
    private Account account;

    public Account getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "PostAccount{" +
                "account=" + account +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }
}
