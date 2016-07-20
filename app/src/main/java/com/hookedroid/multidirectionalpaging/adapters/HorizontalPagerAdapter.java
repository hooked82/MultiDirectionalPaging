package com.hookedroid.multidirectionalpaging.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hookedroid.multidirectionalpaging.fragments.HorizontalFragment;
import com.hookedroid.multidirectionalpaging.models.HorizontalItemModel;
import com.hookedroid.multidirectionalpaging.provider.PagingProvider;

public class HorizontalPagerAdapter extends FragmentStatePagerAdapter {

    private Cursor mCursor;

    public HorizontalPagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);

        mCursor = cursor;
    }

    @Override
    public Fragment getItem(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            String horizUuid = mCursor.getString(PagingProvider.HORIZ_UUID_POS);

            HorizontalItemModel model = new HorizontalItemModel();
            model.setUuid(horizUuid);

            return HorizontalFragment.newInstance(model);
        }

        return null;
    }

    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    @Override
    public String getPageTitle(int position) {
        if (mCursor == null || mCursor.getCount() == 0) {
            return "";
        }

        mCursor.moveToPosition(position);
        return mCursor.getString(PagingProvider.HORIZ_NAME_POS);
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
            return null;
        }

        Cursor oldCursor = mCursor;

        this.mCursor = newCursor;
        notifyDataSetChanged();

        return oldCursor;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }
}
