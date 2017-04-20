package com.rbsoftware.pfm.personalfinancemanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by burzakovskiy on 1/25/2016.
 * Class for checking network state
 */
public class ConnectionDetector {
    public static final String TAG = "ConnectionDetector";
    private final Context _context;
    private ConnectivityManager connectivity;

    /**
     * Constructor
     * @param context application context
     */
    public ConnectionDetector(Context context) {

        this._context = context;
        connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Check network access
     * @return true if network is available or false if not
     */
    public boolean isConnectingToInternet() {

        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {

                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }

            }

        }
        return false;
    }

    /**
     * Check if wifi network is connected
     * @return true if wifi is connected
     */
    public boolean isWifiConnected(){
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        if(info != null){
            if(info.getType() == ConnectivityManager.TYPE_WIFI){

                return true;
            }
        }
        return false;

    }
}
