package io.compactd.client;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;

import java.io.IOException;

/**
 * Created by Vincent on 01/11/2017.
 */

public class CompactdManager {
    private static final String TAG = CompactdManager.class.getSimpleName();
    private static Manager sInstance;

    public static Manager getInstance(Context context) {
        if (sInstance == null) {
            Log.d(TAG, "getInstance: " + context.getFilesDir() );
            try {
                sInstance = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private CompactdManager() {
    }
}
