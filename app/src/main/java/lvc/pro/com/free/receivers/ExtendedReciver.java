//package lvc.pro.com.app1.BroadcastReciver;
//
//import android.app.NotificationManager;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.media.AudioManager;
//import android.media.MediaRecorder;
//import android.os.Environment;
//import android.preference.PreferenceManager;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Date;
//
//import lvc.pro.com.app1.SqliteDatabase.DatabaseHelper;
//import lvc.pro.com.app1.contacts.ContactProvider;
//import lvc.pro.com.app1.pojo_classes.Contacts;
//import lvc.pro.com.app1.utils.StringUtils;
//
///**
// * Created by LVC on 22-Aug-17.
// */
//
//public class ExtendedReciver extends MyReceiver {
//    String formated_number;
//
//    @Override
//    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
//        //incoming call ringing
//    }
//
//    @Override
//    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
//        //out going call started
//        formated_number = StringUtils.prepareContacts(ctx, number);
////        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
////        boolean b=SP.getBoolean("STATE",true);
//        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
//        boolean b = pref.getBoolean("STATE", true);
//        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
//            startRecord(formated_number + "__" + ContactProvider.getCurrentTimeStamp() + "__" + "OUT__2");
//            addtoDatabase(ctx, formated_number);
//            if (getnotifysetting()) {
//                ContactProvider.sendnotification(ctx);
//            }
//        }
//    }
//
//    @Override
//    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
//        //incoming call answered
//        formated_number = StringUtils.prepareContacts(ctx, number);
////        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
////        boolean b=SP.getBoolean("STATE",true);
//        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
//        boolean b = pref.getBoolean("STATE", true);
//        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
//            startRecord(formated_number + "__" + ContactProvider.getCurrentTimeStamp() + "__" + "IN__2");
//            addtoDatabase(ctx, formated_number);
//            //
//            if (getnotifysetting()) {
//                ContactProvider.sendnotification(ctx);
//            }
//        }
//    }
//
//    @Override
//    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
//        //incoming call ended
////        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
////        boolean b=SP.getBoolean("STATE",true);
//        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
//        boolean b = pref.getBoolean("STATE", true);
//        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
//            stopRecording();
//        }
//        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancelAll();
//    }
//
//    @Override
//    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
//        //outgoing call ended
////        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(ctx);
////        boolean b=SP.getBoolean("STATE",true);
//        SharedPreferences pref = ctx.getSharedPreferences("TOGGLE", Context.MODE_PRIVATE);
//        boolean b = pref.getBoolean("STATE", true);
//        if (b && ContactProvider.checkContactStateToRecord(ctx, number)) {
//            stopRecording();
//        }
//        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancelAll();
//    }
//
//    @Override
//    protected void onMissedCall(Context ctx, String number, Date start) {
//        //miss call
//    }
//
//
//    public void startRecord(String name) {
//        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
//        int source = Integer.parseInt(SP.getString("RECORDER", "2"));
//        File sampleDir;
//        String dir = ContactProvider.getFolderPath(context);
//        if (dir.isEmpty()) {
//            sampleDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/CallRecorder");
//        } else {
//            sampleDir = new File(dir);
//        }
//        if (!sampleDir.exists()) {
//            sampleDir.mkdirs();
//        }
//        String file_name = name;
//        try {
//            audiofile = File.createTempFile(file_name, ".3gpp", sampleDir);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        switch (source) {
//            case 0:
//                try {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 1:
//                try {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                    audioManager.setStreamVolume(3, audioManager.getStreamMaxVolume(3), 0);
//                    audioManager.setSpeakerphoneOn(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 2:
//                try {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 3:
//                try {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 4:
//                try {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
//                } catch (Exception e) {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                }
//                break;
//            default:
//                try {
//                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//        }
//        try {
//            recorder.setAudioSamplingRate(8000);
//            recorder.setAudioEncodingBitRate(12200);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        } catch (Exception e) {
//            try {
//                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
//            } catch (Exception d) {
//                d.printStackTrace();
//            }
//        }
//        try {
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            recorder.setOutputFile(audiofile.getAbsolutePath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            recorder.prepare();
//            recorder.start();
//            record = true;
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void stopRecording() {
//        if (record) {
//            try {
//                recorder.stop();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (audioManager != null) {
//            audioManager.setSpeakerphoneOn(false);
//        }
//    }
//
//    public void addtoDatabase(Context ctx, String number) {
//        DatabaseHelper db = new DatabaseHelper(ctx);
//        if (db.isContact(number).getNumber() != null) {
//
//        } else {
//            Contacts contacts = new Contacts();
//            contacts.setFav(0);
//            contacts.setState(0);
//            contacts.setNumber(number);
//            db.addContact(contacts);
//        }
//    }
//
//    private boolean getnotifysetting() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        return sharedPreferences.getBoolean("NOTIFY", true);
//    }
//}
//
