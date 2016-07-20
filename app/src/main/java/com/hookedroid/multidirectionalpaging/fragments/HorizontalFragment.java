package com.hookedroid.multidirectionalpaging.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hookedroid.multidirectionalpaging.R;
import com.hookedroid.multidirectionalpaging.adapters.VerticalPagerAdapter;
import com.hookedroid.multidirectionalpaging.models.HorizontalItemModel;
import com.hookedroid.multidirectionalpaging.provider.HorizontalItemContract;
import com.hookedroid.multidirectionalpaging.provider.PagingProvider;
import com.hookedroid.multidirectionalpaging.provider.VerticalItemContract;
import com.hookedroid.multidirectionalpaging.view.VerticalViewPager;

public class HorizontalFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String TAG = HorizontalFragment.class.getSimpleName();

    private VerticalViewPager mVerticalPager;
    private VerticalPagerAdapter mAdapter;

    private String mUuid;
    private int mSelectedPage;

    public static HorizontalFragment newInstance(HorizontalItemModel model) {
        HorizontalFragment fragment = new HorizontalFragment();

        Bundle args = new Bundle();
        args.putString("UUID", model.getUuid());

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mUuid = savedInstanceState.getString("UUID");
            mSelectedPage = savedInstanceState.getInt("POS", 0);
        } else {
            mUuid = getArguments().getString("UUID");
            mSelectedPage = getArguments().getInt("POS");
        }
    }

    public HorizontalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horizontal, container, false);

        mVerticalPager = (VerticalViewPager)view.findViewById(R.id.vertical_pager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Setup the Adapter with a null Cursor, which will get swapped out when the data is loaded
        mAdapter = new VerticalPagerAdapter(getChildFragmentManager(), null);

        mVerticalPager.setAdapter(mAdapter);
        mVerticalPager.setCurrentItem(mSelectedPage);

        prepLoader(true, getArguments(), 2);
    }

    /**
     * Used when prepping the Content Loader
     * @param startNew Determines whether or not this call should start or restart a Loader
     */
    private void prepLoader(boolean startNew, Bundle args, int loaderId) {
        if (startNew) {
            getLoaderManager().initLoader(loaderId, args, this);
        } else {
            getLoaderManager().restartLoader(loaderId, args, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 2:
                // Returns a new Cursor loader
                return new CursorLoader(
                        getActivity(),
                        HorizontalItemContract.URI
                                .buildUpon()
                                .appendPath(mUuid)
                                .appendPath(VerticalItemContract.TABLE)
                                .build(),
                        PagingProvider.VERTICAL_ITEM_PROJECTION,
                        null,
                        null,
                        VerticalItemContract.SORT_ORDER_DEFAULT
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        mAdapter.closeCursor();
        mAdapter = new VerticalPagerAdapter(getChildFragmentManager(), data);
        mVerticalPager.setAdapter(mAdapter);
        mVerticalPager.setCurrentItem(mSelectedPage);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("UUID", mUuid);

        if (mVerticalPager != null) {
            outState.putInt("POS", mVerticalPager.getCurrentItem());
        }
    }
}
