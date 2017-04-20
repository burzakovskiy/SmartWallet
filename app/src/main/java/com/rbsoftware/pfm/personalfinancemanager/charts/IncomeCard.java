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
 * Holds methods for building income card
 *
 * @author Roman Burzakovskiy
 */
public class IncomeCard extends Card {
    private HashMap<String, Float>  incomeMap;

    public IncomeCard(Context context, HashMap<String, Float> incomeMap) {
        super(context, R.layout.charts_income_card_inner_layout);
        this.incomeMap = incomeMap;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        LinearLayout wrapperIncomePieChart = (LinearLayout) view.findViewById(R.id.chart_income_card_wrapper);
        LinearLayout legendIncomePieChart = (LinearLayout) view.findViewById(R.id.charts_income_card_legend);
        PieChartView mIncomePieChart = (PieChartView) view.findViewById(R.id.income_pie_chart);
        List<SliceValue> values = new ArrayList<>();
        float totalIncome = 0;
        if (incomeMap != null) {
            for (float incomeValue : incomeMap.values()) {
                totalIncome += incomeValue;
            }
        }

        if (totalIncome != 0) {
            wrapperIncomePieChart.setVisibility(View.VISIBLE);
            view.findViewById(R.id.empty_income_chart).setVisibility(View.GONE);
            int j = 0;
            for (Map.Entry<String, Float> entry : incomeMap.entrySet()) {
                if (entry.getValue() != 0) {
                    float value = (entry.getValue() * 100f) / totalIncome;
                    int color = Utils.getColorPalette(mContext, entry.getKey());
                    SliceValue sliceValue = new SliceValue(1, color);
                    values.add(sliceValue);

                    values.get(j).setTarget(value);

                    String legendText = Utils.keyToString(mContext, entry.getKey());
                    String legendValue = String.format(Locale.getDefault(), "%1$,.1f", value) + "%";
                    legendIncomePieChart.addView(generateLegendTextView(legendText, legendValue, color));

                    j++;


                    PieChartData data = new PieChartData(values);
                    data.setHasCenterCircle(true);
                    data.setCenterCircleScale(0.9f);
                    data.setSlicesSpacing(3);


                    mIncomePieChart.setPieChartData(data);
                    mIncomePieChart.startDataAnimation();
                    mIncomePieChart.setChartRotationEnabled(false);
                    mIncomePieChart.setClickable(false);

                    String incomeValue = String.format(Locale.getDefault(), "%1$,.2f", totalIncome) + " " + MainActivity.defaultCurrency;
                    ((TextView) view.findViewById(R.id.charts_card_income_value_text_view)).setText(incomeValue);
                } else {
                    wrapperIncomePieChart.setVisibility(View.GONE);
                    view.findViewById(R.id.empty_income_chart).setVisibility(View.VISIBLE);
                    String incomeValue = 0 + " " + MainActivity.defaultCurrency;
                    ((TextView) view.findViewById(R.id.charts_card_income_value_text_view)).setText(incomeValue);
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
