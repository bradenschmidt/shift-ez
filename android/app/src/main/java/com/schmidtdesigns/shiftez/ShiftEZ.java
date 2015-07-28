package com.schmidtdesigns.shiftez;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.schmidtdesigns.shiftez.models.Account;
import com.squareup.picasso.Picasso;

import io.fabric.sdk.android.Fabric;

/**
 * Created by braden on 15-06-22.
 */
public class ShiftEZ extends Application {
    private static ShiftEZ singleInstance = null;
    private Account account;

    public static ShiftEZ getInstance()
    {
        return singleInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        singleInstance = this;
        DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                placeholder = getResources().getDrawable(R.drawable.ic_action_account_circle);
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return null;
            }
        });
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
