package lvc.pro.com.free;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.callrecorder.free.R;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.chaos.view.PinView;

import java.util.concurrent.CompletionService;

import lvc.pro.com.free.constants.Constants;
import lvc.pro.com.free.utility.CommonUtility;
import lvc.pro.com.free.utility.SharedPreferenceUtility;

public class NewPinLock extends AppCompatActivity {
    PinLockView mPinLockView;
    IndicatorDots mIndicatorDots;
    String TAG = "JUST";
    private String pin;
    private boolean sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = getSharedPreferences("LOCK", MODE_PRIVATE);
        pin = sharedPreferences.getString("PIN", "");
        sets = getIntent().getBooleanExtra("SET", false);
        final boolean onlySetPin = getIntent().getBooleanExtra(Constants.sKEY_FOR_ONLY_SET_PIN, false);
        if (onlySetPin) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_set_pin), Toast.LENGTH_LONG).show();
        }
        if (pin.isEmpty()) {
            setContentView(R.layout.setuppinlayout);
            //code for setting new password
            Button set, cancel;
            final PinView pinEntry = (PinView) findViewById(R.id.txt_pin_entry);
            final PinView pinEntry2 = (PinView) findViewById(R.id.txt_pin_entry2);
            set = (Button) findViewById(R.id.set);
            cancel = (Button) findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pinEntry.setText(null);
                    pinEntry2.setText(null);
                }
            });
            set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pinEntry.getText().toString().equals(pinEntry2.getText().toString())) {
                        if (pinEntry.length() == 4) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("PIN", pinEntry.getText().toString());
                            editor.apply();
                            Toast.makeText(NewPinLock.this, getString(R.string.pin_saved), Toast.LENGTH_SHORT).show();
                            if (!onlySetPin) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("AUTH", true);
                                finish();
                                startActivity(intent);
                            } else {
                                finish();
                            }

                        } else {
                            pinEntry.requestFocus();
                            pinEntry.setError(getString(R.string.enter_four_digit_pin));
                        }
                    } else {
                        pinEntry2.requestFocus();
                        pinEntry2.setError(getString(R.string.pin_mismatch));
                        pinEntry2.setText(null);
                    }
                }
            });
        } else if (sets) {
            setContentView(R.layout.setuppinlayout);
            //code for setting new password
            Button set, cancel;
            final PinView pinEntry = (PinView) findViewById(R.id.txt_pin_entry);
            final PinView pinEntry2 = (PinView) findViewById(R.id.txt_pin_entry2);
            set = (Button) findViewById(R.id.set);
            cancel = (Button) findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pinEntry.setText(null);
                    pinEntry2.setText(null);
                }
            });
            pinEntry2.setVisibility(View.VISIBLE);
            set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pinEntry.getText().toString().equals(pinEntry2.getText().toString())) {
                        if (pinEntry.length() == 4) {
                            //write to shared prefrence
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("PIN", pinEntry.getText().toString());
                            editor.apply();
                            Toast.makeText(NewPinLock.this, getString(R.string.pin_updated), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("AUTH", true);
                            finish();
                            startActivity(intent);
                        } else {
                            pinEntry.requestFocus();
                            pinEntry.setError(getString(R.string.enter_four_digit_pin));
                        }
                    } else {
                        pinEntry2.requestFocus();
                        pinEntry2.setError(getString(R.string.pin_mismatch));
                        pinEntry2.setText(null);
                    }
                }
            });
        } else {
            //code for entering or setting password
            setContentView(R.layout.pinlocknew);
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean b = SP.getBoolean("LOCK", false);
            if (b) {
                //authenticate user
                mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
                mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
                mPinLockView.attachIndicatorDots(mIndicatorDots);
                mPinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin1) {
//                            Log.d(TAG, "Pin complete: " + pin);
                        if (pin.toString().equals(pin1)) {
                            if (Constants.sIS_FROM_BACKGROUND) {
                                Constants.sIS_FROM_BACKGROUND = false;
                                Constants.sIs_FROM_PIN_TO_PIN = false;
                                SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
                                finish();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("AUTH", true);
                                finish();
                                startActivity(intent);
                            }

                        } else {
                            mPinLockView.resetPinLockView();
                            Toast.makeText(NewPinLock.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onEmpty() {
//                            Log.d(TAG, "Pin empty");
                    }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) {
//                            Log.d(TAG, "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
                    }
                });

            } else {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("AUTH", true);
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constants.sIs_FROM_PIN_TO_PIN = false;
        if (Constants.sIS_FROM_BACKGROUND) {
            Constants.sIS_FROM_BACKGROUND = false;
            SharedPreferenceUtility.setBackgroundStatus(getApplicationContext(), false);
            finishAffinity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ((sets) && (!Constants.sIS_FROM_BACKGROUND)) {
            Constants.sIs_FROM_PIN_TO_PIN = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.sIs_FROM_PIN_TO_PIN) {
            Constants.sIS_FROM_BACKGROUND = true;
            Constants.sIs_FROM_PIN_TO_PIN = false;
            Intent intent = new Intent(NewPinLock.this, NewPinLock.class);
            intent.putExtra("SET", false);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.sIs_FROM_PIN_TO_PIN = false;
    }
}
