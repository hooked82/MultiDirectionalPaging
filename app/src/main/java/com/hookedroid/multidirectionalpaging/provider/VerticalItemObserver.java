package com.hookedroid.multidirectionalpaging.provider;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class VerticalItemObserver extends ContentObserver {

    private static String TAG = VerticalItemObserver.class.getSimpleName();

    public VerticalItemObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "Vertical Item Changed - " + uri.toString());
    }
}
