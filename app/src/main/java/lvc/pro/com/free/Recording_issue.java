package lvc.pro.com.free;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.callrecorder.free.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jaredrummler.android.device.DeviceName;

import lvc.pro.com.free.constants.Constants;
import lvc.pro.com.free.utility.CommonUtility;
import lvc.pro.com.free.utility.SharedPreferenceUtility;
import lvc.pro.com.free.utils.HuaweiProtectedAppsModule;

public class Recording_issue extends AppCompatActivity {
    private AdView mAdView;
    public static boolean mIsDestroying=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_issue);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String deviceName = DeviceName.getDeviceName();
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(deviceName);
        TextView tvAddToProtectedApp = (TextView) findViewById(R.id.tvAddToProtectedApp);
        tvAddToProtectedApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HuaweiProtectedAppsModule(Recording_issue.this).AlertIfHuaweiDevice("Huawei Protected Apps", "This app requires to be enables in 'Protected Apps' to work in background", "Dont show again", "PROTECTED APPS", "CANCEL");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsDestroying=true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPreferenceUtility.getLockActivatedStatus(getApplicationContext())) {
            if ((SharedPreferenceUtility.getBackgroundStatus(getApplicationContext())) && (!(Constants.sIS_FROM_ANOTHER_ACTIVITY))) {
                Constants.sIS_FROM_BACKGROUND = true;
                Intent intent = new Intent(Recording_issue.this, NewPinLock.class);
                startActivity(intent);
            }
        }
        Constants.sIS_FROM_ANOTHER_ACTIVITY = false;
        mIsDestroying=false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
        SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Constants.sIS_FROM_ANOTHER_ACTIVITY = true;
                SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
