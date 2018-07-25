package lvc.pro.com.free.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.callrecorder.free.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import lvc.pro.com.free.MainActivity;
import lvc.pro.com.free.Splash_Activity;
import lvc.pro.com.free.SqliteDatabase.DatabaseHelper;
import lvc.pro.com.free.constants.Constants;
import lvc.pro.com.free.contacts.ContactProvider;
import lvc.pro.com.free.pojo_classes.Contacts;
import lvc.pro.com.free.utils.StringUtils;

/**
 * Created by ibtisam on 7/12/2017.
 */

public class CallDetectionService extends Service {
    private static final String TAG = CallDetectionService.class.getSimpleName();
    private static final int NOTIFICATION_ID = -1;
    private BroadcastReceiver mReceiver;

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing
    static MediaRecorder recorder = new MediaRecorder();
    static AudioManager audioManager;
    static File audiofile;
    //    Context context;
    public static boolean record = false;
    String formated_number;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "CallDetectionService onCreate()");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "CallDetectionService onStartCommand()");
        showForegroundNotification("Running in background...");
//        Toast.makeText(getApplicationContext(),"LS Running", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "CallDetectionService onDestroy()");
        unregisterReceiver(receiver);
        Intent intent = new Intent("YouWillNeverKillMe");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: ");
        //create a intent that you want to start again..
        Intent intent = new Intent(getApplicationContext(), CallDetectionService.class);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: Called");
//            this.context = context;
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }

                onCallStateChanged(context, state, number);
            }

        }
    };

    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
//
                } else {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //call ended
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // a miss call
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    isIncoming = false;
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }


    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        //incoming call ringing
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //out going call started
        formated_number = StringUtils.prepareContacts(ctx, number);
//        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
//        boolean b=SP.getBoolean("STATE",true);
        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
        boolean b = pref.getBoolean("STATE", true);
        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
            startRecord(ctx, formated_number + "__" + ContactProvider.getCurrentTimeStamp() + "__" + "OUT__2");
            addtoDatabase(ctx, formated_number);
            if (getnotifysetting(ctx)) {
                ContactProvider.sendnotification(ctx);
            }
        }
    }

    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //incoming call answered
        formated_number = StringUtils.prepareContacts(ctx, number);
//        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
//        boolean b=SP.getBoolean("STATE",true);
        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
        boolean b = pref.getBoolean("STATE", true);
        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
            startRecord(ctx, formated_number + "__" + ContactProvider.getCurrentTimeStamp() + "__" + "IN__2");
            addtoDatabase(ctx, formated_number);
            //
            if (getnotifysetting(ctx)) {
                ContactProvider.sendnotification(ctx);
            }
        }
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //incoming call ended
//        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
//        boolean b=SP.getBoolean("STATE",true);
        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
        boolean b = pref.getBoolean("STATE", true);
        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
            stopRecording();
        }
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //outgoing call ended
//        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
//        boolean b=SP.getBoolean("STATE",true);
        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
        boolean b = pref.getBoolean("STATE", true);
        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
            stopRecording();
        }
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
        //miss call
    }


    public void startRecord(Context context, String name) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        // default value is 0 for call recording so as to record call by default
        int startRecording = Integer.parseInt(SP.getString(context.getString(R.string.shared_pref_saving_pref_key), "0"));
        if (startRecording == 1) {
            record = false;
            return;
        }
        int source = Integer.parseInt(SP.getString("RECORDER", "2"));
        Log.d(TAG, " source value: " + source);
        // default value is 0 for call recording so as to record high quality call by default
        int recordingQuality = Integer.parseInt(SP.getString(context.getString(R.string.shared_pref_recording_quality_pref_key), "0"));
        Log.d(TAG, " recording quality " + recordingQuality);
        File sampleDir;
        String dir = ContactProvider.getFolderPath(context);
        if (dir.isEmpty()) {
            sampleDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/CallRecorder");
        } else {
            sampleDir = new File(dir);
        }
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        String file_name = name;
        try {
            switch (recordingQuality) {
                case 0: {
                    audiofile = File.createTempFile(file_name, ".m4a", sampleDir);
                    break;
                }
                default:
                    audiofile = File.createTempFile(file_name, ".3gpp", sampleDir);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (source) {
            case 0:
                try {
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                try {
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setStreamVolume(3, audioManager.getStreamMaxVolume(3), 0);
                    audioManager.setSpeakerphoneOn(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 4:
                try {
                    //recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    // recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    String manufacturer = Build.MANUFACTURER;
                    if (manufacturer.toLowerCase().contains("samsung")) {
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
                    } else {
                        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    }
                } catch (Exception e) {
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    e.printStackTrace();
                }
                break;
            default:
                try {
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        try {
            switch (recordingQuality) {
                case 0: {
                    Log.d(TAG, " recording quality code " + "0");
                    recorder.setAudioSamplingRate(44000);
                    recorder.setAudioEncodingBitRate(96000);
                    break;
                }
                case 1: {
                    Log.d(TAG, " recording quality code " + "1");
                    recorder.setAudioSamplingRate(11000);
                    recorder.setAudioEncodingBitRate(45000);
                    break;
                }
                case 2: {
                    Log.d(TAG, " recording quality code " + "2");
                    recorder.setAudioSamplingRate(8000);
                    recorder.setAudioEncodingBitRate(12200);
                    break;
                }
                default:
                    Log.d(TAG, " recording quality code " + "default ");
                    recorder.setAudioSamplingRate(8000);
                    recorder.setAudioEncodingBitRate(12200);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
       /* try {
            switch (recordingQuality) {
                case 0: {
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    break;
                }
                default:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            }

        } catch (Exception e) {
            try {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            } catch (Exception d) {
                d.printStackTrace();
            }
        }*/
        try {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        } catch (Exception e) {
            try {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            } catch (Exception d) {
                d.printStackTrace();
            }
        }
       /* try {
            switch (recordingQuality) {
                case 0: {
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    break;
                }
                default:
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            recorder.setOutputFile(audiofile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            recorder.prepare();
            recorder.start();
            record = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (record) {
            try {
                recorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(false);
        }
    }

    public void addtoDatabase(Context ctx, String number) {
        DatabaseHelper db = new DatabaseHelper(ctx);
        if (db.isContact(number).getNumber() != null) {

        } else {
            Contacts contacts = new Contacts();
            contacts.setFav(0);
            contacts.setState(0);
            contacts.setNumber(number);
            db.addContact(contacts);
        }
    }

    private boolean getnotifysetting(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("NOTIFY", true);
    }


    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        // Intent showTaskIntent = new Intent(this, MainActivity.class);
        Intent showTaskIntent = new Intent(this, Splash_Activity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentIntent(contentIntent)
                .setContentTitle("Automatic Call Recorder 2018")
                .setTicker("Automatic Call Recorder 2018")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo), 128, 128, false))
//                .setWhen(0)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        //actually run the notification
        startForeground(NOTIFICATION_ID, notification);
    }
}