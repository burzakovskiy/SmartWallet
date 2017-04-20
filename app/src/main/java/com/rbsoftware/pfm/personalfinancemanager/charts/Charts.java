package com.rbsoftware.pfm.personalfinancemanager.charts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;


/**
 * A simple {@link Fragment} subclass.
 * Charts fragment that holds child fragments
 **/
public class Charts extends Fragment {
    private final static String TAG = "Charts";
    private Fragment mFragment;
    private ViewPager mPager;
    private CollectionPagerAdapter adapter;
    private ConnectionDetector mConnectionDetector;

    public Charts() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager FM = getChildFragmentManager();
        if (adapter == null) {
            adapter = new CollectionPagerAdapter(FM);
        }


    }

    /**
     * Get view pager adapter
     *
     * @return view pager adapter
     */
    public CollectionPagerAdapter getAdapter() {
        return adapter;
    }

    /**
     * Get view pager
     *
     * @return view pager
     */
    public ViewPager getPager() {
        return mPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[3]);

        mPager = (ViewPager) getActivity().findViewById(R.id.charts_viewpager);
        mPager.setAdapter(adapter);

        mConnectionDetector = new ConnectionDetector(getContext());
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sendTracker(position);
            }
        });

    }

    /**
     * Send google analytics tracker
     *
     * @param position child fragment position
     */
    private void sendTracker(int position) {
        if (mConnectionDetector.isConnectingToInternet()) {
            if (position == 0) {
                MainActivity.mTracker.setScreenName("IncomeExpenseChart");
            }
            if (position == 1) {
                MainActivity.mTracker.setScreenName("TrendsChart");
            }
            //check if network is available and send analytics tracker


            MainActivity.mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("Open").build());
        }

    }


    public class CollectionPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                mFragment = new IncomeExpenseChart();
            }
            if (position == 1) {
                mFragment = new TrendsChart();
            }
            return mFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "";
            if (position == 0) {
                title = getResources().getString(R.string.overview);
            }
            if (position == 1) {
                title = getResources().getString(R.string.trends);

            }
            return title;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }


}
