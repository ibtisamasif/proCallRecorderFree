package lvc.pro.com.free.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import lvc.pro.com.free.service.CallDetectionService;

/**
 * Created by ibtisam on 2/18/2017.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    public static final String TAG = "NetworkStateReceiver";

    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Network connectivity change");

        if (intent.getExtras() != null) {

            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {

                Log.d(TAG, "Network " + ni.getTypeName() + " connected");

                context.startService(new Intent(context, CallDetectionService.class));

            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {

                Log.d(TAG, "There's no network connectivity");

            }
        }

    }
}