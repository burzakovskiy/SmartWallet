package com.rbsoftware.pfm.personalfinancemanager.charts;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.ExportData;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


/**
 * A simple {@link Fragment} subclass.
 * Hold line chart data
 *
 * @author Roman Burzakovskiy
 */
public class TrendsChart extends Fragment {
    private final String TAG = "TrendsChart";
    private final int TRENDS_CHART_LOADER_ID = 1;
    private RelativeLayout mTrendsChardWrapper;
    private String selectedPeriod; //position of selected item in popup menu
    private LineChartView mLineChart;
    private TextView mTextViewPeriod, mTextViewChartType;
    private HashMap<String, List<String>> mCustomCategoriesMap;
    private int customIncomeCategoriesNumber = 0;// number of custom income categories
    private List<String> menuItemsList;
    private String lineId;
    private int lineColor;

    public TrendsChart() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        lineId = Utils.readFromSharedPreferences(getActivity(),
                "checkedLine", "-2");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trends_chart, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //reading custom categories data
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        mCustomCategoriesMap = categoryDocument.getCategoriesMap();
        for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                customIncomeCategoriesNumber++;
            }
        }
        //builds dynamic menu items list with custom categories
        initLineMenuList();
        if (mTrendsChardWrapper == null) {
            mTrendsChardWrapper = (RelativeLayout) getActivity().findViewById(R.id.trends_chart_wrapper);
        }
        if (mTextViewChartType == null) {
            mTextViewChartType = (TextView) getActivity().findViewById(R.id.trends_chart_type);
        }
        if (mTextViewPeriod == null) {
            mTextViewPeriod = (TextView) getActivity().findViewById(R.id.tv_period_trend);
        }
        if (mLineChart == null) {
            mLineChart = (LineChartView) getActivity().findViewById(R.id.trends_chart);
        }


        getLoaderManager().initLoader(TRENDS_CHART_LOADER_ID, null, loaderCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();

        mTextViewChartType.setText(Utils.readFromSharedPreferences(getActivity(),
                "chartType",
                getResources().getString(R.string.balance)));
        lineColor = Utils.getColorPalette(getContext(), lineId);
        setLegendItemColor(lineColor);
        mTextViewPeriod.setText(Utils.readFromSharedPreferences(getActivity(),
                "periodTextTrend",
                getResources().getString(R.string.this_week)));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.chart_trends_menu, menu);
        int status = getContext().getSharedPreferences("material_showcaseview_prefs", Context.MODE_PRIVATE)
                .getInt("status_" + TAG, 0);
        if (status != -1) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startShowcase();
                }
            }, 100);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_filter:
                showPopupPeriod();
                return true;
            case R.id.action_line:
                showPopupLine();
                return true;
            case R.id.document_share:
                try {
                    ExportData.exportChartAsPng(getContext(), new View[]{mTrendsChardWrapper});
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
     * Shows period chart_trends_menu popup menu
     */

    private void showPopupPeriod() {
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

                        selectedPeriod = "thisWeek";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_week));
                        break;
                    case R.id.thisMonth:

                        selectedPeriod = "thisMonth";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_month));

                        break;
                    case R.id.lastWeek:

                        selectedPeriod = "lastWeek";
                        mTextViewPeriod.setText(getResources().getString(R.string.last_week));

                        break;
                    case R.id.lastMonth:

                        selectedPeriod = "lastMonth";
                        mTextViewPeriod.setText(getResources().getString(R.string.last_month));

                        break;
                    case R.id.thisYear:

                        selectedPeriod = "thisYear";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_year));

                        break;
                }
                Utils.saveToSharedPreferences(getActivity(), "periodTrend", selectedPeriod);
                Utils.saveToSharedPreferences(getActivity(), "periodTextTrend", mTextViewPeriod.getText().toString());
                updateChart();
                return false;
            }
        });
        popup.show();

    }

    /**
     * Shows lines option menu
     */
    private void showPopupLine() {

        final ArrayAdapter<CharSequence> menuAdapter = new ArrayAdapter<>(getContext(), R.layout.select_default_currency_list_item);
        menuAdapter.addAll(menuItemsList);

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.select_category))
                .setSingleChoiceItems(
                        menuAdapter,
                        0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mTextViewChartType.setText(menuItemsList.get(which));
                                lineId = getItemId(which);
                                lineColor = Utils.getColorPalette(getContext(), lineId);
                                setLegendItemColor(lineColor);
                                //setting line id to e passed as loader parameter
                                Utils.saveToSharedPreferences(getActivity(), "chartType",
                                        mTextViewChartType.getText().toString());
                                Utils.saveToSharedPreferences(getActivity(), "checkedLine",
                                        lineId);
                                updateChart();
                            }
                        }
                )
                .show();


    }

    /**
     * Gets category id from its position in a list
     *
     * @param position of category in a list
     * @return of category
     */
    private String getItemId(int position) {
        if (position <= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + 2)
            return String.valueOf(position - 2);

        if (position > FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + 2 + customIncomeCategoriesNumber &&
                position <= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + customIncomeCategoriesNumber + FinanceDocument.NUMBER_OF_EXPENSE_CATEGORIES + 2) {
            return String.valueOf(position - 2 - customIncomeCategoriesNumber);
        } else {
            for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
                if (menuItemsList.get(position).equals(entry.getValue().get(0))) {
                    return entry.getKey();
                }
            }
        }
        return String.valueOf(-2);

    }

    /**
     * Builds category lines menu list
     */
    private void initLineMenuList() {
        menuItemsList = new ArrayList<>();

        menuItemsList.add(getString(R.string.balance));
        menuItemsList.add(getString(R.string.income));

        menuItemsList.add(getString(R.string.expense));

        List<String> defaultCategoriesList = Arrays.asList(getContext().getResources().getStringArray(R.array.report_activity_category_spinner));
        List<String> incomeCategoriesList = new ArrayList<>(defaultCategoriesList.subList(
                0,
                FinanceDocument.NUMBER_OF_INCOME_CATEGORIES));

        List<String> expenseCategoriesList = new ArrayList<>(defaultCategoriesList.subList(
                FinanceDocument.NUMBER_OF_INCOME_CATEGORIES,
                FinanceDocument.NUMBER_OF_CATEGORIES));

        for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                incomeCategoriesList.add(entry.getValue().get(0));
            } else {
                expenseCategoriesList.add(entry.getValue().get(0));
            }
        }
        menuItemsList.addAll(incomeCategoriesList);
        menuItemsList.addAll(expenseCategoriesList);
    }

    /**
     * Sets color to legend indicator
     *
     * @param color of category
     */
    private void setLegendItemColor(int color) {
        Drawable legendIndicator = ContextCompat.getDrawable(getContext(), R.drawable.charts_card_legend_indicator);
        legendIndicator.setBounds(0, 0, Utils.dpToPx(getContext(), 10), Utils.dpToPx(getContext(), 10));
        if (legendIndicator instanceof ShapeDrawable) {
            ((ShapeDrawable) legendIndicator).getPaint().setColor(color);
        } else if (legendIndicator instanceof GradientDrawable) {
            ((GradientDrawable) legendIndicator).setColor(color);
        } else if (legendIndicator instanceof ColorDrawable) {
            ((ColorDrawable) legendIndicator).setColor(color);
        }
        mTextViewChartType.setCompoundDrawables(legendIndicator, null, null, null);
        mTextViewChartType.setCompoundDrawablePadding(Utils.dpToPx(getContext(), 8));
    }

    /**
     * Fills LineChart with data
     *
     * @param docData array ofvalues and dates 0- values and 1- dates
     */

    private void generateLineChartData(List<String[]> docData) {
        List<AxisValue> axisValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();


        if (docData.size() > 1) {
            mLineChart.setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.empty_trends).setVisibility(View.GONE);
            List<PointValue> values = new ArrayList<>();
            axisValues.clear();
            int size = docData.size();
            for (int j = 0; j < size; ++j) {
                values.add(new PointValue(j, Float.valueOf(docData.get(j)[0])));
                axisValues.add(new AxisValue(j).setLabel(docData.get(j)[1]));
            }

            Line line = new Line(values);
            line.setColor(lineColor);
            line.setShape(ValueShape.CIRCLE);
            line.setCubic(true);

            line.setHasLabels(true);
            line.setHasLines(true);
            line.setHasPoints(true);

            lines.add(line);


            LineChartData data = new LineChartData(lines);


            Axis axisX = new Axis(axisValues);
            Axis axisY = new Axis().setHasLines(true);
            axisX.setTextColor(Color.GRAY);
            axisY.setTextColor(Color.GRAY);
            axisX.setName(getResources().getString(R.string.period));
            axisY.setName(getResources().getString(R.string.value));
            axisY.setMaxLabelChars(6);

            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
            mLineChart.setLineChartData(data);
            Viewport v = new Viewport(mLineChart.getMaximumViewport());
            float delta = Math.max(Math.abs(v.top), Math.abs(v.bottom));
            v.bottom = v.bottom - delta * 0.1f;
            v.top = v.top + delta * 0.2f;
            v.right = (v.right < 10) ? v.right * 1.05f : v.right * 1.01f;
            mLineChart.setMaximumViewport(v);
            mLineChart.setCurrentViewportWithAnimation(v);

        } else {
            mLineChart.setVisibility(View.GONE);
            getActivity().findViewById(R.id.empty_trends).setVisibility(View.VISIBLE);
        }
    }


    /**
     * Sends broadcast intent to update charts
     */
    public void updateChart() {
        Intent intent = new Intent(TrendsChartLoader.ACTION);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }


    private final LoaderManager.LoaderCallbacks<List<String[]>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<String[]>>() {
        @Override
        public Loader<List<String[]>> onCreateLoader(int id, Bundle args) {
            return new TrendsChartLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<String[]>> loader, List<String[]> data) {

            generateLineChartData(data);
        }

        @Override
        public void onLoaderReset(Loader<List<String[]>> loader) {

        }
    };

    /**
     * Runs showcase presentation on fragment start
     */

    private void startShowcase() {
        if (getActivity().findViewById(R.id.action_line) != null) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view
            config.setDismissTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), TAG);
            sequence.setConfig(config);
            sequence.addSequenceItem(getActivity().findViewById(R.id.action_line), getString(R.string.action_line), getString(R.string.ok));
            sequence.start();
        }
    }
}
