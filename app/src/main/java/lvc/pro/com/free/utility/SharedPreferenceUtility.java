package lvc.pro.com.free.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import lvc.pro.com.free.constants.Constants;

/**
 * Created by chicmic on 3/1/18.
 */

public class SharedPreferenceUtility {
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;

    public static void initSharedPreferences(Context pContext) {
        mSharedPreferences = pContext.getSharedPreferences(Constants.sSHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void initSharedPreferencesEditor(Context pContext) {
        if (mSharedPreferences == null) {
            initSharedPreferences(pContext);
        }
        mEditor = mSharedPreferences.edit();
    }

    public static void putProtectedModeStatus(Context pContext) {
        if (mEditor == null) {
            initSharedPreferencesEditor(pContext);
        }
        mEditor.putBoolean(Constants.sKEY_FOR_PROTECTED_MODE, true);
        mEditor.apply();
    }

    public static boolean getProtectedModeStatus(Context pContext) {
        if (mSharedPreferences == null) {
            initSharedPreferences(pContext);
        }
        return mSharedPreferences.getBoolean(Constants.sKEY_FOR_PROTECTED_MODE, false);

    }

    public static void setBackgroundStatus(Context pContext, boolean pStatus) {
        if (mEditor == null) {
            initSharedPreferencesEditor(pContext);
        }
        mEditor.putBoolean(Constants.sKEY_FOR_BACKGROUND_STATUS, pStatus);
        mEditor.commit();
    }

    public static boolean getBackgroundStatus(Context pContext) {
        if (mSharedPreferences == null) {
            initSharedPreferencesEditor(pContext);
        }
        return mSharedPreferences.getBoolean(Constants.sKEY_FOR_BACKGROUND_STATUS, false);
    }

    public static boolean getLockActivatedStatus(Context pContext) {
        SharedPreferences SP1 = PreferenceManager.getDefaultSharedPreferences(pContext);
        return SP1.getBoolean("LOCK", false);
    }


}
