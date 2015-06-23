package com.schmidtdesigns.shiftez;

import android.app.Application;

import com.schmidtdesigns.shiftez.models.Account;

/**
 * Created by braden on 15-06-22.
 */
public class ShiftEZ extends Application {
    private static ShiftEZ singleInstance = null;

    public static ShiftEZ getInstance()
    {
        return singleInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleInstance = this;
    }

    private Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
