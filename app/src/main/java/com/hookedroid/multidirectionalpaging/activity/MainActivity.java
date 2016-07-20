package com.hookedroid.multidirectionalpaging.activity;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.hookedroid.multidirectionalpaging.R;
import com.hookedroid.multidirectionalpaging.adapters.HorizontalPagerAdapter;
import com.hookedroid.multidirectionalpaging.provider.BaseContract;
import com.hookedroid.multidirectionalpaging.provider.HorizontalItemContract;
import com.hookedroid.multidirectionalpaging.provider.PagingProvider;
import com.hookedroid.multidirectionalpaging.provider.VerticalItemContract;
import com.hookedroid.multidirectionalpaging.view.ToolbarPagerTitleStrip;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String TAG = MainActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private HorizontalPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize the Categories Adapter with a null cursor
        mAdapter = new HorizontalPagerAdapter(getSupportFragmentManager(), null);

        //Initialize and set the adapter on the Horizontal View Pager
        mViewPager = (ViewPager) findViewById(R.id.horizontal_pager);
        mViewPager.setAdapter(mAdapter);

        //Initialize and set the view pager used by the custom Toolbar Pager Title Strip
        ToolbarPagerTitleStrip titleStrip =
                (ToolbarPagerTitleStrip) findViewById(R.id.toolbar_pagertitlestrip);

        titleStrip.setViewPager(mViewPager);

        //Initialize the Loader
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 1:
                return new CursorLoader(
                        this,
                        HorizontalItemContract.URI,
                        PagingProvider.HORIZONTAL_ITEM_PROJECTION,
                        null,
                        null,
                        null
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            mAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_sync) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            long dateTime = new Date().getTime();

            for (int i = 0; i < 8; i++) {
                String horizUuid = UUID.randomUUID().toString();
                String horizName = "Horiz-" + i;

                ops.add(ContentProviderOperation
                        .newInsert(HorizontalItemContract.URI)
                        .withValue(HorizontalItemContract.Columns.UUID, horizUuid)
                        .withValue(HorizontalItemContract.Columns.NAME, horizName)
                        .withValue(HorizontalItemContract.Columns.UPDATED, dateTime)
                        .build());

                for (int j = 0; j < 30; j++) {
                    String vertUuid = UUID.randomUUID().toString();
                    String vertTitle = "Horiz-" + i + ", Vertical-" + j;
                    ops.add(ContentProviderOperation
                            .newInsert(VerticalItemContract.URI)
                            .withValue(VerticalItemContract.Columns.UUID, vertUuid)
                            .withValue(VerticalItemContract.Columns.TITLE, vertTitle)
                            .withValue(VerticalItemContract.Columns.HORIZONTAL_ITEM_UUID, horizUuid)
                            .withValue(VerticalItemContract.Columns.UPDATED, dateTime)
                            .build());
                }
            }

            try {
                getContentResolver().applyBatch(BaseContract.AUTHORITY, ops);
            } catch (RemoteException ex) {
                Log.e(TAG, "Error applying batch", ex);
            } catch (OperationApplicationException ex) {
                Log.e(TAG, "Error applying batch operation", ex);
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
