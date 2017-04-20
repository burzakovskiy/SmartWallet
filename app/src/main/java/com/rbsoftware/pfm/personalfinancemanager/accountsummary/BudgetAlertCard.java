package com.rbsoftware.pfm.personalfinancemanager.accountsummary;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.budget.BudgetDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for creation budget alert card
 *
 * @author Roman Burzakovskiy
 */
public class BudgetAlertCard extends Card{

    private final List<BudgetDocument> docList;
    private final int[] expense;
    public BudgetAlertCard(Context context,  List<BudgetDocument> docList, int[] expense) {
        super(context, R.layout.account_summary_budget_alert_card_layout);

        this.docList = docList;
        this.expense = expense;

        this.addCardHeader(new CardHeader(getContext(), R.layout.account_summary_alert_card_header_layout));
    }

    /**
     * Checks if card is empty
     * @return true if empty
     */
    public boolean isBudgetAlertCardEmpty(){return docList.isEmpty();}
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.account_summary_budget_alert_wrapper);
        if (linearLayout != null) {
            linearLayout.removeAllViewsInLayout();
            for(BudgetDocument doc  :docList) {
                addProgressIndicators(linearLayout, doc);
            }
        }
    }

    /**
     * Adds budget execution indicators
     *
     * @param linearLayout of wrapper
     */
    private void addProgressIndicators(LinearLayout linearLayout, BudgetDocument doc) {
        if (doc.getPeriod().equals(BudgetDocument.PERIOD_WEEKLY)) {
                if (expense[0] != 0) {
                    linearLayout.addView(createNewProgressRow(doc, expense[0]));
                }
        } else {
                if (expense[1] != 0) {
                    linearLayout.addView(createNewProgressRow(doc, expense[1]));
                }
            }
        }

    /**
     * Generates budget execution row
     *
     * @param progress of budget
     * @return layout with progress views
     */
    private LinearLayout createNewProgressRow(BudgetDocument doc, int progress) {
        final LinearLayout linearLayout = new LinearLayout(mContext);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);

        final LinearLayout.LayoutParams layoutParamsPeriod = new LinearLayout.LayoutParams(0, Utils.dpToPx(mContext, 45), 30f);
        layoutParamsPeriod.setMargins(0, Utils.dpToPx(getContext(), 8), 0, 0);
        final TextView tvPeriod = new TextView(mContext);
        tvPeriod.setLayoutParams(layoutParamsPeriod);
        tvPeriod.setText(doc.getName());
        tvPeriod.setTextColor(Color.GRAY);
        linearLayout.addView(tvPeriod);

        final LinearLayout.LayoutParams layoutParamsProgress = new LinearLayout.LayoutParams(0, Utils.dpToPx(mContext, 45), 50f);
        if (progress <= doc.getConvertedValue()) {
            final NumberProgressBar progressBar = new NumberProgressBar(mContext);
            progressBar.setLayoutParams(layoutParamsProgress);
            progressBar.setPadding(Utils.dpToPx(getContext(), 8), 0, Utils.dpToPx(getContext(), 8), 0);
            progressBar.setMax(Math.round(doc.getConvertedValue()));
            progressBar.setProgress(progress);
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
            tvBudgetExceeded.setGravity(Gravity.LEFT);

            int exceed = Math.round((float) progress / doc.getConvertedValue() * 100f - 100.0f);
            String text = mContext.getString(R.string.budget_exceeded) + " " + exceed + "%";
            tvBudgetExceeded.setText(text);
            tvBudgetExceeded.setTextColor(ContextCompat.getColor(mContext, R.color.expense));
            linearLayout.addView(tvBudgetExceeded);

        }

        return linearLayout;
    }

    }

