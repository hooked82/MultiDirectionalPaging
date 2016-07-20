package com.hookedroid.multidirectionalpaging.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hookedroid.multidirectionalpaging.R;
import com.hookedroid.multidirectionalpaging.models.VerticalItemModel;

public class VerticalFragment extends Fragment {

//    private VerticalItemObserver mObserver;

    private VerticalItemModel mModel;

    private TextView mTitleText;
//    private TextView mDescText;
//    private EditText mDescEditText;
//    private Button mUpdateBtn;

    public static VerticalFragment newInstance(VerticalItemModel model) {
        VerticalFragment fragment = new VerticalFragment();

        Bundle args = new Bundle();
        args.putParcelable("MODEL", model);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mModel = savedInstanceState.getParcelable("MODEL");
        } else {
            mModel = getArguments().getParcelable("MODEL");
        }

//        mObserver = new VerticalItemObserver(new Handler());
    }

    public VerticalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vertical, container, false);

        mTitleText = (TextView) view.findViewById(R.id.vertical_item_text);
//        mDescText = (TextView) view.findViewById(R.id.vertical_item_desc);
//        mDescEditText = (EditText) view.findViewById(R.id.vertical_item_desc_txt);
//        mUpdateBtn = (Button) view.findViewById(R.id.vertical_item_update_btn);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTitleText.setText(mModel.getTitle());
//        mDescText.setText(mModel.getDescription());
//
//        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String description = mDescEditText.getText().toString();
//                ContentValues values = new ContentValues();
//                values.put(VerticalItemContract.Columns.DESCRIPTION, description);
//
//                getContext().getContentResolver().update(
//                        VerticalItemContract.URI.buildUpon().appendPath(mModel.getUuid()).build(),
//                        values,
//                        null,
//                        null
//                );
//            }
//        });
    }

    @Override
    public void onResume() {
        super.onResume();

//        getContext().getContentResolver().registerContentObserver(
//                VerticalItemContract.URI.buildUpon().appendPath(mModel.getUuid()).build(),
//                true,
//                mObserver
//        );
    }

    @Override
    public void onPause() {
        super.onPause();

//        getContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("MODEL", mModel);
    }
}
