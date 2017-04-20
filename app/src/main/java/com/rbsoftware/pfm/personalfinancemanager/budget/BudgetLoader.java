package com.rbsoftware.pfm.personalfinancemanager.budget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocumentModel;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds methods for loading budget data in background
 *
 * @author Roman Burzakovskiy
 */
public class BudgetLoader extends AsyncTaskLoader<List<BudgetCard>> {

    public static final String ACTION = "BudgetLoader.FORCELOAD";

    public BudgetLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter(ACTION);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        forceLoad();
    }

    @Override
    public List<BudgetCard> loadInBackground() {
        ArrayList<BudgetCard> cards = new ArrayList<>();
        List<BudgetDocument> docListBudget = MainActivity
                .financeDocumentModel
                .queryBudgetDocumentsByDate(DateUtils.THIS_YEAR,
                        MainActivity.getUserId(),
                        BudgetDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());
        List<FinanceDocument> docListFinance = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(DateUtils.THREE_MONTHS,
                        MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());
        float[] totalIncomeExpenseData = getDataFromList(docListFinance);
        for (int i = 0; i < docListBudget.size(); i++) {
            BudgetCard card = new BudgetCard(getContext(), docListBudget.get(i), totalIncomeExpenseData);
            cards.add(card);
        }
        return cards;
    }

    @Override
    public void deliverResult(List<BudgetCard> data) {
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            forceLoad();
        }
    };

    /**
     * Gets total expense data in three months period
     *
     * @param docListFinance list of finance documents in 3 months period
     * @return [0] - this week
     * [1] - last week
     * [2] - 2 weeks ago
     * [3] - total income this week
     * [4] - this month
     * [5] - last month
     * [6] - 2 month ago
     * [7] - total income this month
     */
    private float[] getDataFromList(List<FinanceDocument> docListFinance) {
        float[] data = {0, 0, 0, 0, 0, 0, 0, 0};
        for (FinanceDocument doc : docListFinance) {
            long date = Long.valueOf(doc.getDate());
            if (date >= DateUtils.getFirstDateOfCurrentWeek()) {
                data[0] += doc.getTotalExpense();
                data[3] += doc.getTotalIncome();
            }
            if ((date >= DateUtils.getFirstDateOfPreviousWeek()) && (date < DateUtils.getFirstDateOfCurrentWeek())) {
                data[1] += doc.getTotalExpense();
            }
            if ((date >= DateUtils.getFirstDateOfTwoWeeksAgo()) && (date < DateUtils.getFirstDateOfPreviousWeek())) {
                data[2] += doc.getTotalExpense();
            }
            if (date >= DateUtils.getFirstDateOfCurrentMonth()) {
                data[4] += doc.getTotalExpense();
                data[7] += doc.getTotalIncome();
            }
            if ((date >= DateUtils.getFirstDateOfPreviousMonth()) && (date < DateUtils.getFirstDateOfCurrentMonth())) {
                data[5] += doc.getTotalExpense();
            }
            if ((date >= DateUtils.getFirstDateOfTwoMonthsAgo()) && (date < DateUtils.getFirstDateOfPreviousMonth())) {
                data[6] += doc.getTotalExpense();
            }

        }
        return data;
    }


}
