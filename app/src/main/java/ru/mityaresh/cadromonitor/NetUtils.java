// NetUtils.java -- Network utilites.

package ru.mityaresh.cadromonitor;

import java.net.InetAddress;
import android.content.Context;
import android.net.ConnectivityManager;

class NetUtils {
    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return ! ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }

    }
}
