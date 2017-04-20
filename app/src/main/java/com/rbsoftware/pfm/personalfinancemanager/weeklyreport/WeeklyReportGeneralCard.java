package com.rbsoftware.pfm.personalfinancemanager.weeklyreport;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.Locale;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for building general weekly report card
 * Created by Roman Burzakovskiy on 7/15/2016.
 */
public class WeeklyReportGeneralCard extends Card {
    private static final String TAG = "ReportGeneralCard";

    private float thisWeekIncome;
    private float lastWeekIncome;
    private float thisWeekExpense;
    private float lastWeekExpense;
    private float thisWeekBalance;
    private float lastWeekBalance;

    public WeeklyReportGeneralCard(Context context, float thisWeekIncome, float lastWeekIncome, float thisWeekExpense, float lastWeekExpense) {
        super(context, R.layout.weekly_report_general_card_main_inner_layout);
        this.thisWeekIncome = thisWeekIncome;
        this.lastWeekIncome = lastWeekIncome;
        this.thisWeekExpense = thisWeekExpense;
        this.lastWeekExpense = lastWeekExpense;
        this.thisWeekBalance = thisWeekIncome - thisWeekExpense;
        this.lastWeekBalance = lastWeekIncome - lastWeekExpense;

        WeeklyReportGeneralCardHeader header = new WeeklyReportGeneralCardHeader(context);
        addCardHeader(header);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        //setting summary text
        TextView summaryTextView = (TextView) view.findViewById(R.id.weekly_report_general_card_summary_textview);
        summaryTextView.setText(getSummaryText());

        //building report table
        LinearLayout listWrapper = (LinearLayout) view.findViewById(R.id.weekly_report_general_card_list_wrapper);
        listWrapper.addView(createBalanceRow());
        listWrapper.addView(createIncomeRow());
        listWrapper.addView(createExpenseRow());
    }

    /**
     * creates balance table row
     *
     * @return balance table row
     */
    private RelativeLayout createBalanceRow() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout balanceRowLayout = (RelativeLayout) layoutInflater.inflate(R.layout.weekly_report_general_card_list_item, null);

        TextView titleTextView = (TextView) balanceRowLayout.findViewById(R.id.weekly_report_general_card_list_title_text_view);
        titleTextView.setText(getContext().getString(R.string.balance));

        TextView thisWeekTextView = (TextView) balanceRowLayout.findViewById(R.id.weekly_report_general_card_list_this_week_text_view);
        thisWeekTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", thisWeekBalance));
        thisWeekTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.balance));

        TextView lastWeekTextView = (TextView) balanceRowLayout.findViewById(R.id.weekly_report_general_card_list_last_week_text_view);
        lastWeekTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", lastWeekBalance));
        TextView deltaTextView = (TextView) balanceRowLayout.findViewById(R.id.weekly_report_general_card_list_delta_text_view);
        if (thisWeekBalance == lastWeekBalance || thisWeekBalance == 0 || lastWeekBalance == 0) {
            deltaTextView.setText("");
        } else {
            if (thisWeekBalance > lastWeekBalance) {
                int balanceDelta = Math.abs(Math.round((thisWeekBalance / lastWeekBalance - 1) * 100));
                String delta = "+" + balanceDelta + "%";
                deltaTextView.setText(delta);
                deltaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.income));
            } else {
                int balanceDelta = Math.abs(Math.round((lastWeekBalance / thisWeekBalance - 1) * 100));
                String delta = "-" + balanceDelta + "%";
                deltaTextView.setText(delta);
                deltaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.expense));
            }
        }


        return balanceRowLayout;
    }

    /**
     * creates income table row
     *
     * @return income table row
     */
    private RelativeLayout createIncomeRow() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout incomeRowLayout = (RelativeLayout) layoutInflater.inflate(R.layout.weekly_report_general_card_list_item, null);

        TextView titleTextView = (TextView) incomeRowLayout.findViewById(R.id.weekly_report_general_card_list_title_text_view);
        titleTextView.setText(getContext().getString(R.string.income));

        TextView thisWeekTextView = (TextView) incomeRowLayout.findViewById(R.id.weekly_report_general_card_list_this_week_text_view);
        thisWeekTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", thisWeekIncome));
        thisWeekTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.income));

        TextView lastWeekTextView = (TextView) incomeRowLayout.findViewById(R.id.weekly_report_general_card_list_last_week_text_view);
        lastWeekTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", lastWeekIncome));
        TextView deltaTextView = (TextView) incomeRowLayout.findViewById(R.id.weekly_report_general_card_list_delta_text_view);
        if (thisWeekIncome == lastWeekIncome || thisWeekIncome == 0 || lastWeekIncome == 0) {
            deltaTextView.setText("");
        } else {
            if (thisWeekIncome > lastWeekIncome) {
                int incomeDelta = Math.abs(Math.round((thisWeekIncome / lastWeekIncome - 1) * 100));
                String delta = "+" + incomeDelta + "%";
                deltaTextView.setText(delta);
                deltaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.income));
            } else {
                int incomeDelta = Math.abs(Math.round((lastWeekIncome / thisWeekIncome - 1) * 100));
                String delta = "-" + incomeDelta + "%";
                deltaTextView.setText(delta);
                deltaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.expense));
            }
        }

        return incomeRowLayout;
    }

    /**
     * creates expense table row
     *
     * @return expense table row
     */
    private RelativeLayout createExpenseRow() {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        RelativeLayout expenseRowLayout = (RelativeLayout) layoutInflater.inflate(R.layout.weekly_report_general_card_list_item, null);

        TextView titleTextView = (TextView) expenseRowLayout.findViewById(R.id.weekly_report_general_card_list_title_text_view);
        titleTextView.setText(getContext().getString(R.string.expense));

        TextView thisWeekTextView = (TextView) expenseRowLayout.findViewById(R.id.weekly_report_general_card_list_this_week_text_view);
        thisWeekTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", thisWeekExpense));
        thisWeekTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.expense));

        TextView lastWeekTextView = (TextView) expenseRowLayout.findViewById(R.id.weekly_report_general_card_list_last_week_text_view);
        lastWeekTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", lastWeekExpense));
        TextView deltaTextView = (TextView) expenseRowLayout.findViewById(R.id.weekly_report_general_card_list_delta_text_view);
        if (thisWeekExpense == lastWeekExpense || thisWeekExpense == 0 || lastWeekExpense == 0) {
            deltaTextView.setText("");
        } else {
            if (thisWeekExpense > lastWeekExpense) {
                int expenseDelta = Math.abs(Math.round((thisWeekExpense / lastWeekExpense - 1) * 100));
                String delta = "+" + expenseDelta + "%";
                deltaTextView.setText(delta);
                deltaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.expense));
            } else {
                int expenseDelta = Math.abs(Math.round((lastWeekExpense / thisWeekExpense - 1) * 100));
                String delta = "-" + expenseDelta + "%";
                deltaTextView.setText(delta);
                deltaTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.income));
            }
        }

        return expenseRowLayout;
    }

    private String getSummaryText() {
        if (thisWeekBalance > 0 && thisWeekBalance > lastWeekBalance) {
            return getContext().getString(R.string.weekly_report_summary_amazing);
        }
        if (thisWeekExpense > lastWeekExpense) {
            return getContext().getString(R.string.weekly_report_summary_bad);

        }

        return getContext().getString(R.string.weekly_report_summary_general);
    }

    private class WeeklyReportGeneralCardHeader extends CardHeader {
        public WeeklyReportGeneralCardHeader(Context context) {
            super(context, R.layout.weekly_report_general_card_header_inner_layout);
        }
    }
}
