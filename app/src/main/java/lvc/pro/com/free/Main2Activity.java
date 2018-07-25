package lvc.pro.com.free;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.callrecorder.free.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lvc.pro.com.free.constants.Constants;
import lvc.pro.com.free.utility.CommonUtility;
import lvc.pro.com.free.utility.SharedPreferenceUtility;

import static lvc.pro.com.free.contacts.ContactProvider.getFolderPath;

public class Main2Activity extends AppCompatActivity {
    public static final String TAG = "Main2Activity";

    FloatingActionButton buttonPlayPause;
    SeekBar seekBarProgress;
    MediaPlayer mediaPlayer;
    Handler seekHandler = new Handler();
    private int mediaFileLengthInMilliseconds;
    TextView title;
    String path;
    public static boolean mIsDestroying = false;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.musicplayer);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        path = getIntent().getStringExtra("PATH");
        title = findViewById(R.id.name);
        title.setText(path);
        getInit();
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void getInit() {
        buttonPlayPause = (FloatingActionButton) findViewById(R.id.button1);
        seekBarProgress = (SeekBar) findViewById(R.id.seekBar2);
        seekBarProgress.setMax(99);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getFolderPath(this) + "/" + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekUpdation();
            mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!mediaPlayer.isPlaying()) {
            buttonPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        } else {
            buttonPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
        }
        buttonPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    buttonPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                } else {
                    mediaPlayer.pause();
                    buttonPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                }
                seekUpdation();
            }
        });
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!mediaPlayer.isPlaying()) {
                    int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * seekBarProgress.getProgress();
                    mediaPlayer.seekTo(playPositionInMillisecconds);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.start();
                if (!mediaPlayer.isPlaying()) {
                    buttonPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                } else {
                    buttonPlayPause.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                }
                seekUpdation();
            }
        });
    }

    private void seekUpdation() {
        seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    seekUpdation();
                }
            };
            seekHandler.postDelayed(notification, 1000);
        } else {
            buttonPlayPause.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsDestroying = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Constants.sFROM_LISTEN_TO_MAIN) {
            if (SharedPreferenceUtility.getLockActivatedStatus(getApplicationContext())) {
                if ((SharedPreferenceUtility.getBackgroundStatus(getApplicationContext()))
                        && (!(Constants.sIS_FROM_ANOTHER_ACTIVITY))) {
                    Constants.sIS_FROM_BACKGROUND = true;
                    Intent intent = new Intent(Main2Activity.this, NewPinLock.class);
                    startActivity(intent);
                }
            }
        }
        Constants.sFROM_LISTEN_TO_MAIN = false;
        Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
        mIsDestroying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
        // mIsDestroying=false;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
    }
}
