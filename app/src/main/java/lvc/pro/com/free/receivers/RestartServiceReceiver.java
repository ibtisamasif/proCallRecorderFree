package lvc.pro.com.free.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import lvc.pro.com.free.service.CallDetectionService;


/**
 * Created by ibtisam on 8/4/2017.
 */

public class RestartServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoRestartApp";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive");
        context.startService(new Intent(context.getApplicationContext(), CallDetectionService.class));

    }

}