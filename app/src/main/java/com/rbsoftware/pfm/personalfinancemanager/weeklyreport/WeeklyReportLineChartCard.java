package com.rbsoftware.pfm.personalfinancemanager.weeklyreport;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Holds methods for building line chart card
 * Created by Roman Burzakovskiy on 7/17/2016.
 */
public class WeeklyReportLineChartCard extends Card {
    private static final String TAG = "ReportLineChartCard";
    /**
     * Line chart data, 0-value,1- date
     */
    private List<String[]> incomeChartThisWeek;
    private List<String[]> expenseChartThisWeek;

    public WeeklyReportLineChartCard(Context context,
                                     List<String[]> incomeChartThisWeek,

                                     List<String[]> expenseChartThisWeek) {
        super(context, R.layout.weekly_report_line_chart_card_main_inner_layout);
        this.incomeChartThisWeek = incomeChartThisWeek;
        this.expenseChartThisWeek = expenseChartThisWeek;
        WeeklyReportLineChartCardHeader header = new WeeklyReportLineChartCardHeader(context);
        addCardHeader(header);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        if (incomeChartThisWeek.size() > 1) {


            LineChartView incomeLineChartView = (LineChartView) view.findViewById(R.id.weekly_report_line_chart_income_chart);
            setupChartData(incomeLineChartView, FinanceDocument.CUSTOM_INCOME);

        } else {
            view.findViewById(R.id.weekly_report_line_chart_empty_income_textview).setVisibility(View.VISIBLE);
        }

        if (expenseChartThisWeek.size() > 1) {


            LineChartView expenseLineChartView = (LineChartView) view.findViewById(R.id.weekly_report_line_chart_expense_chart);
            setupChartData(expenseLineChartView, FinanceDocument.CUSTOM_EXPENSE);
        } else {
            view.findViewById(R.id.weekly_report_line_chart_empty_expense_textview).setVisibility(View.VISIBLE);
        }

    }


    private void setupChartData(LineChartView chartView, int dataType) {
        chartView.setVisibility(View.VISIBLE);
        List<String[]> thisWeekData;


        List<AxisValue> axisValues = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        List<PointValue> values = new ArrayList<>();
        axisValues.clear();

        if (dataType == FinanceDocument.CUSTOM_INCOME) {
            thisWeekData = incomeChartThisWeek;
        } else {
            thisWeekData = expenseChartThisWeek;
        }

        //building this week line
        for (int j = 0; j < thisWeekData.size(); ++j) {
            values.add(new PointValue(j, Float.valueOf(thisWeekData.get(j)[0])));
            axisValues.add(new AxisValue(j).setLabel(thisWeekData.get(j)[1]));
        }
        Line thisWeekLine = new Line(values);
        if (dataType == FinanceDocument.CUSTOM_INCOME) {
            thisWeekLine.setColor(ContextCompat.getColor(getContext(), R.color.income));
        } else {
            thisWeekLine.setColor(ContextCompat.getColor(getContext(), R.color.expense));
        }
        thisWeekLine.setShape(ValueShape.CIRCLE);
        thisWeekLine.setCubic(true);

        thisWeekLine.setHasLabels(true);
        thisWeekLine.setHasLines(true);
        thisWeekLine.setHasPoints(true);

        lines.add(thisWeekLine);


        //building chart
        LineChartData data = new LineChartData(lines);

        Axis axisX = new Axis(axisValues);
        Axis axisY = new Axis().setHasLines(true);
        axisX.setTextColor(Color.GRAY);
        axisY.setTextColor(Color.GRAY);
        axisX.setName(getContext().getString(R.string.period));
        axisY.setName(getContext().getString(R.string.value));
        axisY.setMaxLabelChars(4);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        chartView.setLineChartData(data);
        Viewport viewport = new Viewport(chartView.getMaximumViewport());
        float delta = Math.max(Math.abs(viewport.top), Math.abs(viewport.bottom));
        viewport.bottom = viewport.bottom - delta * 0.1f;
        viewport.top = viewport.top + delta * 0.2f;
        viewport.right = (viewport.right < 10) ? viewport.right * 1.05f : viewport.right * 1.01f;
        chartView.setMaximumViewport(viewport);
        chartView.setCurrentViewportWithAnimation(viewport);


    }


    private class WeeklyReportLineChartCardHeader extends CardHeader {
        public WeeklyReportLineChartCardHeader(Context context) {
            super(context, R.layout.weekly_report_line_chart_card_header_inner_layout);
        }
    }
}
