package io.compactd.player.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

/**
 * Created by vinz243 on 27/11/2017.
 */

public class PreferenceUtil {
    private static PreferenceUtil sInstance;
    private static String REMOTE_URL = "remote_url";
    private final SharedPreferences preferences;

    private PreferenceUtil(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance (@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(context);
        }
        return sInstance;
    }

    public String getRemoteUrl() {
       return preferences.getString(REMOTE_URL, "");
    }

    public void setRemoteUrl (String url) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(REMOTE_URL, url);
        editor.apply();
    }
}
