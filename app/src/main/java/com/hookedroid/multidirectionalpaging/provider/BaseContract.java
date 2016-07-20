package com.hookedroid.multidirectionalpaging.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.hookedroid.multidirectionalpaging.BuildConfig;

public class BaseContract {
    static String BASE_AUTHORITY = BuildConfig.APPLICATION_ID;
    public static String AUTHORITY = BASE_AUTHORITY + ".provider.PagingProvider";

    static Uri BASE_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();

    public interface BasePagingColumns extends BaseColumns {
        String UUID = "uuid";
        String UPDATED = "updated";
    }
}
