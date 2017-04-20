package com.rbsoftware.pfm.personalfinancemanager.budget;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.Locale;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds method for budget card interface
 *
 * @author Roman Burzakovskiy
 */
public class BudgetCard extends Card {

    private final BudgetDocument doc;
    private final float[] totalExpenseIncomeData;

    /**
     * Constructor of budget card
     *
     * @param context          of application
     * @param doc              budget document
     * @param totalExpenseIncomeData expense and income data
     */
    public BudgetCard(Context context, BudgetDocument doc, float[] totalExpenseIncomeData) {

        super(context, R.layout.budget_card_main_inner_layout);
        this.doc = doc;
        this.totalExpenseIncomeData = totalExpenseIncomeData;
        this.setHeader();

    }

    /**
     * Gets budget document
     *
     * @return budget document
     */
    public BudgetDocument getDocument() {
        return doc;
    }

    /**
     * Adds header to card
     *
     */
    private void setHeader() {
        //Create a CardHeader
        BudgetHeaderInnerCard header = new BudgetHeaderInnerCard(mContext, doc.getName(), doc.getConvertedValue());
        this.addCardHeader(header);
    }

    /**
     * Get estimated budget value
     * @return estimated budget value
     */
    public String getEstimatedBudgetValue(){
        return ((BudgetHeaderInnerCard)this.getCardHeader()).estimatedBalance.getText().toString();
    }

    /**
     * Gets expense and income date for progress calculation
     * @return [0] - this week
     * [1] - last week
     * [2] - 2 weeks ago
     * [3] - total income this week
     * [4] - this month
     * [5] - last month
     * [6] - 2 month ago
     * [7] - total income this month
     */
    public float[] getTotalExpenseIncomeData(){
        return totalExpenseIncomeData;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.budget_card_content_wrapper);
        if (linearLayout != null) {
            linearLayout.removeAllViewsInLayout();
            addProgressIndicators(linearLayout);
        }
    }


    /**
     * Adds budget execution indicators
     *
     * @param linearLayout of wrapper
     */
    private void addProgressIndicators(LinearLayout linearLayout) {
        if (doc.getPeriod().equals(BudgetDocument.PERIOD_WEEKLY)) {
            for (int i = 0; i < 3; i++) {
                if (totalExpenseIncomeData[i] != 0) {
                    linearLayout.addView(createNewProgressRow(mContext.getResources().getStringArray(R.array.budget_card_periods)[i], totalExpenseIncomeData[i]));
                }
            }
        } else {
            for (int i = 4; i < 7; i++) {
                if (totalExpenseIncomeData[i] != 0) {
                    linearLayout.addView(createNewProgressRow(mContext.getResources().getStringArray(R.array.budget_card_periods)[i - 1], totalExpenseIncomeData[i]));
                }
            }
        }

    }

    /**
     * Generates budget execution row
     *
     * @param period   of budget
     * @param progress of budget
     * @return layout with progress views
     */
    private LinearLayout createNewProgressRow(String period, float progress) {
        final LinearLayout linearLayout = new LinearLayout(mContext);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);

        final LinearLayout.LayoutParams layoutParamsPeriod = new LinearLayout.LayoutParams(0, Utils.dpToPx(mContext, 40), 30f);
        layoutParamsPeriod.setMargins(0, Utils.dpToPx(getContext(), 8), 0, 0);
        final TextView tvPeriod = new TextView(mContext);
        tvPeriod.setLayoutParams(layoutParamsPeriod);
        tvPeriod.setText(period);
        tvPeriod.setTextColor(Color.GRAY);
        linearLayout.addView(tvPeriod);

        final LinearLayout.LayoutParams layoutParamsProgress = new LinearLayout.LayoutParams(0, Utils.dpToPx(mContext, 40), 70f);
        if (progress <= doc.getConvertedValue()) {
            final NumberProgressBar progressBar = new NumberProgressBar(mContext);
            progressBar.setLayoutParams(layoutParamsProgress);
            progressBar.setPadding(Utils.dpToPx(getContext(), 8), 0, Utils.dpToPx(getContext(), 8), 0);
            progressBar.setMax(Math.round(doc.getConvertedValue()));
            progressBar.setProgress(Math.round(progress));
            int threshold = Math.round(doc.getConvertedValue() * 0.75f);
            progressBar.setReachedBarColor(Utils.getProgressColor(getContext(), progressBar.getMax(), threshold, progressBar.getProgress()));
            progressBar.setProgressTextColor(Utils.getProgressColor(getContext(), progressBar.getMax(), threshold, progressBar.getProgress()));
            progressBar.setProgressTextSize(Utils.dpToPx(getContext(), 14));
            linearLayout.addView(progressBar);
        } else {
            layoutParamsProgress.setMargins(0, Utils.dpToPx(getContext(), 8), 0, 0);
            final TextView tvBudgetExceeded = new TextView(mContext);
            tvBudgetExceeded.setLayoutParams(layoutParamsProgress);
            tvBudgetExceeded.setPadding(Utils.dpToPx(getContext(), 8), 0, Utils.dpToPx(getContext(), 8), 0);
            tvBudgetExceeded.setGravity(Gravity.CENTER_HORIZONTAL);
            int exceed = Math.round( progress / doc.getConvertedValue() * 100f - 100.0f);
            String text = mContext.getString(R.string.budget_exceeded) + " " + exceed + "%";
            tvBudgetExceeded.setText(text);
            tvBudgetExceeded.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            linearLayout.addView(tvBudgetExceeded);

        }

        return linearLayout;
    }


    /**
     * Helper class to customize card header
     */

    private class BudgetHeaderInnerCard extends CardHeader {

        private final String name;
        private final float budgetValue;

        private TextView estimatedBalance;

        /**
         * Budget card header constructor
         * @param context application context
         * @param name budget name
         * @param value budget value
         */
        public BudgetHeaderInnerCard(Context context, String name, float value) {
            super(context, R.layout.budget_card_header_inner_layout);
            this.name = name;
            this.budgetValue = value;


        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);
            if (view != null) {

                TextView nameView = (TextView) view.findViewById(R.id.tv_budget_card_header_name);
                if (nameView != null)
                    nameView.setText(name);


                TextView valueView = (TextView) view.findViewById(R.id.tv_budget_card_header_value);
                if (valueView != null) {
                    String valueText = String.format(Locale.getDefault(), "%1$,.2f", budgetValue) + " " + MainActivity.defaultCurrency;
                    valueView.setText(valueText);
                }


                estimatedBalance = (TextView) view.findViewById(R.id.tv_budget_card_header_estimated_balance);

                if (doc.getPeriod().equals(BudgetDocument.PERIOD_WEEKLY)) {
                    float estimatedBalanceValue;
                    if(totalExpenseIncomeData[0] < budgetValue ) {
                        estimatedBalanceValue = totalExpenseIncomeData[3] - budgetValue;
                    }
                    else {
                        estimatedBalanceValue = totalExpenseIncomeData[3] - totalExpenseIncomeData[0];
                    }
                    String valueText = String.format(Locale.getDefault(), "%1$,.2f", estimatedBalanceValue) + " " + MainActivity.defaultCurrency;
                    estimatedBalance.setText(valueText);
                } else {
                    float estimatedBalanceValue;
                    if(totalExpenseIncomeData[4] < budgetValue ) {
                        estimatedBalanceValue = totalExpenseIncomeData[7] - budgetValue;
                    }
                    else {
                        estimatedBalanceValue = totalExpenseIncomeData[7] - totalExpenseIncomeData[4];
                    }
                    String valueText = String.format(Locale.getDefault(), "%1$,.2f", estimatedBalanceValue) + " " + MainActivity.defaultCurrency;
                    estimatedBalance.setText(valueText);

                }

            }
        }


    }
}
