package com.hookedroid.multidirectionalpaging.adapters;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hookedroid.multidirectionalpaging.fragments.VerticalFragment;
import com.hookedroid.multidirectionalpaging.models.VerticalItemModel;
import com.hookedroid.multidirectionalpaging.provider.PagingProvider;

public class VerticalPagerAdapter extends FragmentStatePagerAdapter {

    private Cursor mCursor;

    public VerticalPagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);

        mCursor = cursor;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            String title = mCursor.getString(PagingProvider.VERT_TITLE_POS);
            String uuid = mCursor.getString(PagingProvider.VERT_UUID_POS);
            String description = mCursor.getString(PagingProvider.VERT_DESC_POS);

            VerticalItemModel model = new VerticalItemModel();
            model.setTitle(title);
            model.setUuid(uuid);
            model.setDescription(description);

            return VerticalFragment.newInstance(model);
        }

        return null;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    public void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
}
