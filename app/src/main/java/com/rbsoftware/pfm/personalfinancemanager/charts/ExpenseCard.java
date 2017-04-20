package com.rbsoftware.pfm.personalfinancemanager.charts;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

/**
 * Holds methods for building expense card
 *
 * @author Roman Burzakovskiy
 */
public class ExpenseCard extends Card {

    private HashMap<String, Float> expenseMap;

    public ExpenseCard(Context context, HashMap<String, Float> expenseMap) {
        super(context, R.layout.charts_expense_card_inner_layout);
        this.expenseMap = expenseMap;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        LinearLayout wrapperExpensePieChart = (LinearLayout) view.findViewById(R.id.chart_expense_card_wrapper);
        LinearLayout legendExpensePieChart = (LinearLayout) view.findViewById(R.id.charts_expense_card_legend);
        PieChartView expensePieChart = (PieChartView) view.findViewById(R.id.expense_pie_chart);
        List<SliceValue> values = new ArrayList<>();
        float totalExpense = 0;
        if (expenseMap != null) {
            for (float incomeValue : expenseMap.values()) {
                totalExpense += incomeValue;
            }
        }

        if (totalExpense != 0) {
            wrapperExpensePieChart.setVisibility(View.VISIBLE);
            view.findViewById(R.id.empty_expense_chart).setVisibility(View.GONE);
            int j = 0;

            for (Map.Entry<String, Float> entry : expenseMap.entrySet()) {
                if (entry.getValue() != 0) {
                    float value = (entry.getValue() * 100f) / totalExpense;
                    int color = Utils.getColorPalette(mContext, entry.getKey());
                    SliceValue sliceValue = new SliceValue(1, color);
                    values.add(sliceValue);

                    values.get(j).setTarget(value);

                    String legendText = Utils.keyToString(mContext, entry.getKey());
                    String legendValue = String.format(Locale.getDefault(), "%1$,.1f", value) + "%";
                    legendExpensePieChart.addView(generateLegendTextView(legendText, legendValue, color));

                    j++;

                    PieChartData data = new PieChartData(values);
                    data.setHasCenterCircle(true);
                    data.setCenterCircleScale(0.9f);
                    data.setSlicesSpacing(3);


                    expensePieChart.setPieChartData(data);
                    expensePieChart.startDataAnimation();
                    expensePieChart.setChartRotationEnabled(false);
                    expensePieChart.setClickable(false);

                    String expenseValue = String.format(Locale.getDefault(), "%1$,.2f", totalExpense) + " " + MainActivity.defaultCurrency;
                    ((TextView) view.findViewById(R.id.charts_card_expense_value_text_view)).setText(expenseValue);
                    expensePieChart.setPieChartData(data);
                    expensePieChart.startDataAnimation();
                } else {
                    wrapperExpensePieChart.setVisibility(View.GONE);
                    view.findViewById(R.id.empty_expense_chart).setVisibility(View.VISIBLE);
                    String expenseValue = 0 + " " + MainActivity.defaultCurrency;
                    ((TextView) view.findViewById(R.id.charts_card_expense_value_text_view)).setText(expenseValue);
                }

            }
        }
    }

    /**
     * Creates legend fields
     *
     * @param legendText  name of category
     * @param legendValue value in per cents
     * @param color       color of legend item
     * @return legend row
     */
    private RelativeLayout generateLegendTextView(String legendText, String legendValue, int color) {
        final RelativeLayout relativeLayout = new RelativeLayout(mContext);
        final RelativeLayout.LayoutParams relativeLayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayout.setLayoutParams(relativeLayoutParams);
        final RelativeLayout.LayoutParams legendTextLayoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        legendTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        legendTextLayoutParams.setMargins(0, Utils.dpToPx(getContext(), 4), 0, 0);

        final RelativeLayout.LayoutParams legendValueLayoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        legendValueLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        final TextView tvLegendText = new TextView(mContext);

        tvLegendText.setLayoutParams(legendTextLayoutParams);
        tvLegendText.setText(legendText);

        Drawable legendIndicator = ContextCompat.getDrawable(mContext, R.drawable.charts_card_legend_indicator);
        legendIndicator.setBounds(0, 0, Utils.dpToPx(mContext, 10), Utils.dpToPx(mContext, 10));
        if (legendIndicator instanceof ShapeDrawable) {
            ((ShapeDrawable) legendIndicator).getPaint().setColor(color);
        } else if (legendIndicator instanceof GradientDrawable) {
            ((GradientDrawable) legendIndicator).setColor(color);
        } else if (legendIndicator instanceof ColorDrawable) {
            ((ColorDrawable) legendIndicator).setColor(color);
        }
        tvLegendText.setCompoundDrawables(legendIndicator, null, null, null);
        tvLegendText.setCompoundDrawablePadding(Utils.dpToPx(mContext, 8));

        final TextView tvLegendValue = new TextView(mContext);

        tvLegendValue.setLayoutParams(legendValueLayoutParams);
        tvLegendValue.setText(legendValue);

        relativeLayout.addView(tvLegendText);
        relativeLayout.addView(tvLegendValue);
        return relativeLayout;
    }
}

