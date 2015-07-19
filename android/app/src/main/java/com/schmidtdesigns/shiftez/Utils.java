package com.schmidtdesigns.shiftez;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.List;

/**
 * Created by braden on 15-06-30.
 */
public class Utils {

    /**
     * Return the last item in the list
     *
     * @param list List to retrieve item from
     * @return Last item or null if list is empty or null
     */
    public static Object getLastItem(List<?> list) {
        if (list != null && !list.isEmpty()) {
            return list.get(list.size() - 1);
        }
        return null;
    }

    public static boolean checkForPlayServices(Activity activity) {
        int state = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);

        if (state == ConnectionResult.SUCCESS) {
            Toast.makeText(activity, "SUCCESS", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(state, activity, -1);
            dialog.show();
            return false;
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void spiceErrorCheck(SpiceException spiceException, Context context) {
        if (spiceException instanceof NoNetworkException) {
            Toast.makeText(context, R.string.no_network, Toast.LENGTH_LONG).show();
        }
    }
}
