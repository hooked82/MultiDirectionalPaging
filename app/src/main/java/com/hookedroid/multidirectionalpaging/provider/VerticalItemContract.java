package com.hookedroid.multidirectionalpaging.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class VerticalItemContract extends BaseContract {
    public static String TABLE = "verticalItems";

    public interface Columns extends BasePagingColumns {
        String TITLE = "title";
        String HORIZONTAL_ITEM_UUID = "horizontalItemUuid";
        String DESCRIPTION = "description";
    }

    public static Uri URI = BASE_URI.buildUpon().appendPath(TABLE).build();

    // These are the "types" of data that may be returned for various URIs. The first is when multiple
    // items are returned. For example fetch all states.  The second is used when a specific item is returned.
    public static String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_AUTHORITY + "." + TABLE;

    public static String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_AUTHORITY + "." + TABLE;

    static String CREATE_TABLE =
            "CREATE TABLE " + TABLE + " ( " +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    Columns.UUID + " TEXT NOT NULL UNIQUE, " +
                    Columns.TITLE + " TEXT NOT NULL, " +
                    Columns.HORIZONTAL_ITEM_UUID + " TEXT NOT NULL, " +
                    Columns.DESCRIPTION + " TEXT, " +
                    Columns.UPDATED + " INTEGER NOT NULL" +
                    ")";

    /**
     * The default sort order for queries.
     */
    public static String SORT_ORDER_DEFAULT = Columns.UPDATED + " DESC";
}