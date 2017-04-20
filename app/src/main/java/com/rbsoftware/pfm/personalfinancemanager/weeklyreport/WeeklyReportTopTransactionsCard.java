package com.rbsoftware.pfm.personalfinancemanager.weeklyreport;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.List;
import java.util.Locale;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for building top transactions card
 * Created by Roman Burzakovskiy on 7/16/2016.
 */
public class WeeklyReportTopTransactionsCard extends Card {
    /**
     * list of top income transactions
     * inside array 0- date in unix time stamp format, 1 - category key, 2 - value
     */
    private List<String[]> topIncomeTransactionsList;
    /**
     * list of top expense transactions
     * inside array 0- date in unix time stamp format, 1 - category key, 2 - value
     */
    private List<String[]> topExpenseTransactionsList;

    public WeeklyReportTopTransactionsCard(Context context, List<String[]> topIncomeTransactionsList, List<String[]> topExpenseTransactionsList) {
        super(context, R.layout.weekly_report_top_tansactions_card_main_inner_layout);
        this.topIncomeTransactionsList = topIncomeTransactionsList;
        this.topExpenseTransactionsList = topExpenseTransactionsList;
        WeeklyReportTopTransactionsCardHeader header = new WeeklyReportTopTransactionsCardHeader(context);
        addCardHeader(header);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        LinearLayout incomeListWrapper = (LinearLayout) view.findViewById(R.id.weekly_report_top_transactions_card_income_wrapper);
        if (!topIncomeTransactionsList.isEmpty()) {
            for (String[] asArray : topIncomeTransactionsList)
                incomeListWrapper.addView(generateTopTransactionsRow(asArray));
        } else {
            incomeListWrapper.addView(generateEmptyTextView());
        }
        LinearLayout expenseListWrapper = (LinearLayout) view.findViewById(R.id.weekly_report_top_transactions_card_expense_wrapper);
        if (!topExpenseTransactionsList.isEmpty()) {
            for (String[] asArray : topExpenseTransactionsList)
                expenseListWrapper.addView(generateTopTransactionsRow(asArray));
        } else {
            expenseListWrapper.addView(generateEmptyTextView());
        }
    }

    /**
     * Generates text view if there is no data
     *
     * @return empty text view
     */
    private TextView generateEmptyTextView() {

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView emptyTextView = new TextView(getContext());
        emptyTextView.setLayoutParams(layoutParams);
        emptyTextView.setText(R.string.no_data_to_show);
        emptyTextView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
        emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        return emptyTextView;
    }

    /**
     * Generates top transaction row
     *
     * @param data top transaction array 0-date, 1- category key, 2- value
     * @return top transaction row
     */
    private RelativeLayout generateTopTransactionsRow(String[] data) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout rowLayout = (RelativeLayout) layoutInflater.inflate(R.layout.weekly_report_top_transactions_card_list_item, null);

        TextView dateTextView = (TextView) rowLayout.findViewById(R.id.weekly_report_top_transactions_card_list_date_text_view);
        dateTextView.setText(DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, data[0]));

        TextView categoryTextView = (TextView) rowLayout.findViewById(R.id.weekly_report_top_transactions_card_list_category_text_view);
        categoryTextView.setText(Utils.keyToString(getContext(), data[1]));

        TextView valueTextView = (TextView) rowLayout.findViewById(R.id.weekly_report_top_transactions_card_list_value_text_view);
        valueTextView.setText(String.format(Locale.getDefault(), "%1$,.2f", Float.valueOf(data[2])));


        return rowLayout;
    }

    private class WeeklyReportTopTransactionsCardHeader extends CardHeader {

        public WeeklyReportTopTransactionsCardHeader(Context context) {
            super(context, R.layout.weekly_report_top_tansactions_card_header_inner_layout);
        }
    }
}
