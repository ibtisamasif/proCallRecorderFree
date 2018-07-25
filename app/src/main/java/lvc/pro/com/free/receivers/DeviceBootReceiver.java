package lvc.pro.com.free.receivers;

/**
 * Created by ahmad on 31-Oct-16.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import lvc.pro.com.free.service.CallDetectionService;

public class DeviceBootReceiver extends BroadcastReceiver {
    private static final String TAG = "DeviceBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "LastingSales Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "DeviceBootReceiver onReceive(): ");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(TAG, "DeviceBootReceiver onReceive() Intent Action: BOOT_COMPLETED ");

            context.startService(new Intent(context, CallDetectionService.class));

            Intent intentCallDetectionService = new Intent(context, CallDetectionService.class);
            intentCallDetectionService.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            PendingIntent pendingIntent = PendingIntent.getService(context, 1, intentCallDetectionService, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 3600000, pendingIntent);

            Toast.makeText(context, "LastingSales Started", Toast.LENGTH_LONG).show();
        }
    }
}