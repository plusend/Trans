package com.plusend.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.lenovo.sharesdk.ShareWrapper;

public class AnalyticsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        ConnectivityManager connectMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isMobileConnected = mobNetInfo.isConnected();
        boolean isWifiConnected = wifiNetInfo.isConnected();

        if (isMobileConnected || isWifiConnected) {
            ShareWrapper.dataCollection(context);
        }
    }

}
