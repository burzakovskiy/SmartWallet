package com.rbsoftware.pfm.personalfinancemanager.charts;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.ExportData;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.IOException;
import java.util.HashMap;

import it.gmariotti.cardslib.library.view.CardViewNative;


/**
 * A simple {@link Fragment} subclass.
 * Child class of (@link Charts) class
 * Holds pie chart data
 */
public class IncomeExpenseChart extends Fragment {

    private final String TAG = "IncomeExpenseChart";

    private final int INCOME_EXPENSE_CHART_LOADER_ID = 0;
    private String selectedItem; //position of selected item in popup menu
    private TextView mTextViewPeriod;
    private CardViewNative mIncomeCardView;
    private CardViewNative mExpenseCardView;

    public IncomeExpenseChart() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_income_expense_chart, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mTextViewPeriod == null) {
            mTextViewPeriod = (TextView) getActivity().findViewById(R.id.tv_period);
        }
        if (mIncomeCardView == null) {
            mIncomeCardView = (CardViewNative) getActivity().findViewById(R.id.charts_income_card);
        }
        if (mExpenseCardView == null) {
            mExpenseCardView = (CardViewNative) getActivity().findViewById(R.id.charts_expense_card);
        }


        getLoaderManager().initLoader(INCOME_EXPENSE_CHART_LOADER_ID, null, loaderCallbacks);


    }

    @Override
    public void onResume() {
        super.onResume();
        mTextViewPeriod.setText(Utils.readFromSharedPreferences(getActivity(), "periodText", getResources().getString(R.string.this_week)));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.chart_income_expense_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_filter:
                showPopup();
                return true;
            case R.id.document_share:
                try {
                    ExportData.exportChartAsPng(getContext(), new View[]{mIncomeCardView, mExpenseCardView});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }


    //Helper methods

    /**
     * Shows chart_income_expense_menu popup menu
     */

    private void showPopup() {
        View menuItemView = getActivity().findViewById(R.id.action_filter);
        PopupMenu popup = new PopupMenu(getActivity(), menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.period, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                switch (id) {
                    case R.id.thisWeek:
                        selectedItem = "thisWeek";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_week));
                        break;
                    case R.id.thisMonth:
                        selectedItem = "thisMonth";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_month));

                        break;
                    case R.id.lastWeek:
                        selectedItem = "lastWeek";
                        mTextViewPeriod.setText(getResources().getString(R.string.last_week));

                        break;
                    case R.id.lastMonth:
                        selectedItem = "lastMonth";
                        mTextViewPeriod.setText(getResources().getString(R.string.last_month));

                        break;
                    case R.id.thisYear:
                        selectedItem = "thisYear";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_year));

                        break;
                }
                Utils.saveToSharedPreferences(getActivity(), "period", selectedItem);
                Utils.saveToSharedPreferences(getActivity(), "periodText", mTextViewPeriod.getText().toString());
                updateChart();
                return false;
            }
        });
        popup.show();

    }

    /**
     * fills mPieChart with data
     *
     * @param valuesMap hash map of data types and values
     */

    private void generateChartData( HashMap<Integer, HashMap<String, Float>> valuesMap) {
        HashMap<String, Float> incomeMap = valuesMap.get(FinanceDocument.CUSTOM_INCOME);
        HashMap<String, Float> expenseMap = valuesMap.get(FinanceDocument.CUSTOM_EXPENSE);

        IncomeCard incomeCard = new IncomeCard(getContext(), incomeMap);
        if (mIncomeCardView.getCard() == null) {
            mIncomeCardView.setCard(incomeCard);
        } else {
            mIncomeCardView.replaceCard(incomeCard);
        }

        ExpenseCard expenseCard = new ExpenseCard(getContext(), expenseMap);
        if (mExpenseCardView.getCard() == null) {
            mExpenseCardView.setCard(expenseCard);
        } else {
            mExpenseCardView.replaceCard(expenseCard);
        }


    }

    /**
     * Sends broadcast intent to update charts
     */
    public void updateChart() {
        Intent intent = new Intent(IncomeExpenseChartLoader.ACTION);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }


    private final LoaderManager.LoaderCallbacks< HashMap<Integer, HashMap<String, Float>>> loaderCallbacks = new LoaderManager.LoaderCallbacks< HashMap<Integer, HashMap<String, Float>>>() {
        @Override
        public Loader< HashMap<Integer, HashMap<String, Float>>> onCreateLoader(int id, Bundle args) {
            return new IncomeExpenseChartLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader< HashMap<Integer, HashMap<String, Float>>> loader,  HashMap<Integer, HashMap<String, Float>> data) {
            generateChartData(data);
        }

        @Override
        public void onLoaderReset(Loader< HashMap<Integer, HashMap<String, Float>>> loader) {
        }
    };


}
