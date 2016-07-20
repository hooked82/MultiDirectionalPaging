package com.hookedroid.multidirectionalpaging.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

public class PagingProvider extends ContentProvider {

    private static String TAG = PagingProvider.class.getSimpleName();

    final static int HORIZONTAL_ITEMS = 1000;
    final static int HORIZONTAL_ITEMS_UUID = 1001;
    final static int HORIZONTAL_ITEMS_UUID_VERTICAL_ITEMS = 1002;

    final static int VERTICAL_ITEMS = 2000;
    final static int VERTICAL_ITEMS_UUID = 2001;

    private static UriMatcher sUriMatcher;

    static{
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(BaseContract.AUTHORITY, "horizontalItems", HORIZONTAL_ITEMS);
        sUriMatcher.addURI(BaseContract.AUTHORITY, "horizontalItems/*", HORIZONTAL_ITEMS_UUID);
        sUriMatcher.addURI(BaseContract.AUTHORITY, "horizontalItems/*/verticalItems", HORIZONTAL_ITEMS_UUID_VERTICAL_ITEMS);

        sUriMatcher.addURI(BaseContract.AUTHORITY, "verticalItems", VERTICAL_ITEMS);
        sUriMatcher.addURI(BaseContract.AUTHORITY, "verticalItems/*", VERTICAL_ITEMS_UUID);
    }

    public static String[] HORIZONTAL_ITEM_PROJECTION = new String[] {
            HorizontalItemContract.Columns.UUID,
            HorizontalItemContract.Columns.NAME,
            HorizontalItemContract.Columns.UPDATED,
            HorizontalItemContract.Columns._ID
    };

    public static String[] VERTICAL_ITEM_PROJECTION = new String[]{
            VerticalItemContract.Columns.UUID,
            VerticalItemContract.Columns.TITLE,
            VerticalItemContract.Columns.HORIZONTAL_ITEM_UUID,
            VerticalItemContract.Columns.DESCRIPTION,
            VerticalItemContract.Columns.UPDATED,
            VerticalItemContract.Columns._ID
    };

    public static int HORIZ_UUID_POS = 0;
    public static int HORIZ_NAME_POS = 1;
    public static int HORIZ_UPDATED_POS = 2;
    public static int HORIZ_ID_POS = 3;

    public static int VERT_UUID_POS = 0;
    public static int VERT_TITLE_POS = 1;
    public static int VERT_HORIZ_UUID_POS = 2;
    public static int VERT_DESC_POS = 3;
    public static int VERT_UPDATED_POS = 4;
    public static int VERT_ID_POS = 5;

    private static ProjectionMap sHorizontalItemsProjectionMap = ProjectionMap.builder()
            .add(HORIZONTAL_ITEM_PROJECTION[HORIZ_UUID_POS])
            .add(HORIZONTAL_ITEM_PROJECTION[HORIZ_NAME_POS])
            .add(HORIZONTAL_ITEM_PROJECTION[HORIZ_UPDATED_POS])
            .add(HORIZONTAL_ITEM_PROJECTION[HORIZ_ID_POS])
            .build();

    private static ProjectionMap sVerticalItemsProjectionMap = ProjectionMap.builder()
            .add(VERTICAL_ITEM_PROJECTION[VERT_UUID_POS])
            .add(VERTICAL_ITEM_PROJECTION[VERT_TITLE_POS])
            .add(VERTICAL_ITEM_PROJECTION[VERT_HORIZ_UUID_POS])
            .add(VERTICAL_ITEM_PROJECTION[VERT_DESC_POS])
            .add(VERTICAL_ITEM_PROJECTION[VERT_UPDATED_POS])
            .add(VERTICAL_ITEM_PROJECTION[VERT_ID_POS])
            .build();


    private DatabaseHelper mOpenHelper;

    static String DATABASE_NAME = "Paging.db";
    static int DATABASE_VERSION = 1;

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(HorizontalItemContract.CREATE_TABLE);
            db.execSQL(VerticalItemContract.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  HorizontalItemContract.TABLE);
            db.execSQL("DROP TABLE IF EXISTS " +  VerticalItemContract.TABLE);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());

        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table;
        Uri contentUri;
        switch (sUriMatcher.match(uri)) {
            case HORIZONTAL_ITEMS:
                table = HorizontalItemContract.TABLE;
                contentUri = HorizontalItemContract.URI;

                break;

            case VERTICAL_ITEMS:
                table = VerticalItemContract.TABLE;
                contentUri = VerticalItemContract.URI;

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();

        if (rowId > 0) {
            Uri _uri = ContentUris.withAppendedId(contentUri, rowId);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        String table;

        switch (sUriMatcher.match(uri)) {
            case HORIZONTAL_ITEMS:
                table = HorizontalItemContract.TABLE;

                break;
            case HORIZONTAL_ITEMS_UUID_VERTICAL_ITEMS:
                table = VerticalItemContract.TABLE;

                break;
            case VERTICAL_ITEMS:
                table = VerticalItemContract.TABLE;

                break;
            default:
                throw new IllegalArgumentException("Unknown Bulk Insert URI " + uri);
        }

        boolean success = false;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            for (ContentValues value : values) {
                db.insertWithOnConflict(table, null, value, SQLiteDatabase.CONFLICT_REPLACE);
            }

            success = true;
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            //TODO
        } finally {
            db.endTransaction();
            db.close();
        }

        if (success) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d(TAG, "Cursor Notification Uri: " + uri.toString());

            return values.length;
        } else {
            return 0;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case HORIZONTAL_ITEMS:
                qb.setTables(HorizontalItemContract.TABLE);
                qb.setProjectionMap(sHorizontalItemsProjectionMap);
                break;

            case HORIZONTAL_ITEMS_UUID:
                qb.setTables(HorizontalItemContract.TABLE);
                qb.appendWhere( HorizontalItemContract.Columns.UUID + " = '" + uri.getPathSegments().get(1) + "'");
                break;

            case HORIZONTAL_ITEMS_UUID_VERTICAL_ITEMS:
                qb.setTables(VerticalItemContract.TABLE);
                qb.appendWhere(VerticalItemContract.Columns.HORIZONTAL_ITEM_UUID + " = '" + uri.getPathSegments().get(1) + "'");
                break;

            case VERTICAL_ITEMS_UUID:
                qb.setTables(VerticalItemContract.TABLE);
                qb.appendWhere(VerticalItemContract.Columns.UUID + " = '" + uri.getPathSegments().get(1) + "'");

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = qb.query(mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

//        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//
//        switch (sUriMatcher.match(uri)){
//            case HORIZONTAL_ITEMS:
//                count = db.delete(HorizontalItemContract.TABLE, selection, selectionArgs);
//                break;
//
//            case HORIZONTAL_ITEMS_UUID:
//                String id = uri.getPathSegments().get(1);
//                count = db.delete(HorizontalItemContract.TABLE, HorizontalItemContract.Columns._ID +  " = " + id +
//                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
//                break;
//
//            case VERTICAL_ITEMS:
//                count = db.delete(VerticalItemContract.TABLE, selection, selectionArgs);
//                break;
//
//            default:
//                db.close();
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
//
//        db.close();
//
//        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)){
            case HORIZONTAL_ITEMS:
                count = db.update(HorizontalItemContract.TABLE, values, selection, selectionArgs);
                break;

            case HORIZONTAL_ITEMS_UUID:
                count = db.update(HorizontalItemContract.TABLE, values, HorizontalItemContract.Columns._ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            case VERTICAL_ITEMS_UUID:
                count = db.update(VerticalItemContract.TABLE, values, VerticalItemContract.Columns.UUID + " = '" + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? "' AND (" + selection + ')' : "'"), selectionArgs);

                break;
            default:
                db.close();
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        db.close();

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)){
            case HORIZONTAL_ITEMS:
                return HorizontalItemContract.CONTENT_TYPE;
            case HORIZONTAL_ITEMS_UUID:
                return HorizontalItemContract.CONTENT_ITEM_TYPE;
            case HORIZONTAL_ITEMS_UUID_VERTICAL_ITEMS:
                return HorizontalItemContract.CONTENT_ITEM_TYPE_VERTICAL_ITEMS;

            case VERTICAL_ITEMS:
                return VerticalItemContract.CONTENT_TYPE;
            case VERTICAL_ITEMS_UUID:
                return VerticalItemContract.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}