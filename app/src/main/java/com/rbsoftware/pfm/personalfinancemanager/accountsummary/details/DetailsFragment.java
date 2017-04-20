package com.rbsoftware.pfm.personalfinancemanager.accountsummary.details;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Holds methods for displaying details fragment
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends DialogFragment {
    private static final String TAG = "DetailsFragment";
    private final int DETAILS_LOADER = 0;
    /**
     * income or expense data
     */
    private int dataType;

    /**
     * recycler view to hold income expense data
     */
    private RecyclerView mRecyclerView;
    /**
     * Sectioned adapter
     */
    private DetailsRecyclerViewAdapter mDetailsRecyclerViewAdapter;
    /**
     * empty view placeholder
     */
    private TextView mEmptyView;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataType = getArguments().getInt("dataType");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_details);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.account_summary_details_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAILS_LOADER, null, loaderCallbacks);
    }

    private final LoaderManager.LoaderCallbacks<LinkedHashMap<String, List<String[]>>> loaderCallbacks = new LoaderManager.LoaderCallbacks<LinkedHashMap<String, List<String[]>>>() {
        @Override
        public Loader<LinkedHashMap<String, List<String[]>>> onCreateLoader(int id, Bundle args) {
            return new DetailsLoader(getContext(), dataType);
        }

        @Override
        public void onLoadFinished(Loader<LinkedHashMap<String, List<String[]>>> loader, LinkedHashMap<String, List<String[]>> data) {
            generateData(data);
        }

        @Override
        public void onLoaderReset(Loader<LinkedHashMap<String, List<String[]>>> loader) {
        }
    };

    /**
     * Maps details data to view
     *
     * @param data details data
     *             key - date
     *             value - list of arrays 0- category, 1- value
     */
    private void generateData(LinkedHashMap<String, List<String[]>> data) {
        mDetailsRecyclerViewAdapter = new DetailsRecyclerViewAdapter(data);
        mDetailsRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mDetailsRecyclerViewAdapter);
            checkAdapterIsEmpty();
        }
    }

    /**
     * Checks whether recycler view is empty
     * And switches to empty view
     */
    private void checkAdapterIsEmpty() {
        if (mDetailsRecyclerViewAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);

        }
    }


}
