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
    private static String SESSION_TOKEN = "session_token";
    private static String USERNAME = "username";
    private static String SYNC_DESTINATION = "sync_dest";
    private static String SYNC_PRESET = "normal";

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

    public String getUsername () {
        return preferences.getString(USERNAME, "");
    }

    public void setUsername (String username) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USERNAME, username);
        editor.apply();
    }

    public String getSessionToken() {
        return preferences.getString(SESSION_TOKEN, "");
    }

    public void setSessionToken (String token) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SESSION_TOKEN, token);
        editor.apply();
    }

    public String getRemoteUrl() {
       return preferences.getString(REMOTE_URL, "");
    }

    public void setRemoteUrl (String url) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(REMOTE_URL, url);
        editor.apply();
    }

    public String getSyncDestination () {
        return preferences.getString(SYNC_DESTINATION, "/storage/emulated/0/Music");
    }

    public void setSyncDestination (String dest) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SYNC_DESTINATION, dest);
        editor.apply();
    }

    public String getSyncPreset () {
        return preferences.getString(SYNC_PRESET, "normal");
    }

    public void setSyncPreset (String preset) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SYNC_PRESET, preset);
        editor.apply();
    }
}
