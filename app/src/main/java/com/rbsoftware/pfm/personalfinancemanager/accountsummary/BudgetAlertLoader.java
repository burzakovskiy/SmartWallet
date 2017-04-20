package com.rbsoftware.pfm.personalfinancemanager.accountsummary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocumentModel;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.budget.BudgetDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds method for loading alert data in background
 *
 * @author Roman Burzakovskiy
 */
public class BudgetAlertLoader extends AsyncTaskLoader<BudgetAlertCard> {
    private static final String TAG = "BudgetAlertLoader";
    public static final String ACTION = "BudgetAlertLoader.FORCELOAD";

    public BudgetAlertLoader(Context context) {
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
    public BudgetAlertCard loadInBackground() {
        List<BudgetDocument> docListBudget = MainActivity
                .financeDocumentModel
                .queryBudgetDocumentsByDate(DateUtils.THIS_YEAR,
                        MainActivity.getUserId(),
                        BudgetDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());
        List<FinanceDocument> docListFinance = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(DateUtils.THREE_MONTHS, MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());
        int[] expenses = getExpenses(docListFinance);
        return new BudgetAlertCard(getContext(), getAlertDocsList(docListBudget, expenses), expenses);
    }

    @Override
    public void deliverResult(BudgetAlertCard data) {
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
     * Filters document list
     *
     * @param docList  of Budget documents
     * @param expenses filter criteria
     * @return documents that require attention
     */
    private List<BudgetDocument> getAlertDocsList(List<BudgetDocument> docList, int[] expenses) {
        List<BudgetDocument> data = new ArrayList<>();
        for (BudgetDocument doc : docList) {
            if (doc.getPeriod().equals(BudgetDocument.PERIOD_WEEKLY) && ((float) expenses[0] >= doc.getConvertedValue() * 0.75f)) {
                data.add(doc);
            }
            if (doc.getPeriod().equals(BudgetDocument.PERIOD_MONTHLY) && ((float) expenses[1] >= doc.getConvertedValue() * 0.75f)) {
                data.add(doc);
            }
        }
        return data;
    }

    /**
     * Extracts expenses data from document list
     *
     * @param docList of finance documents
     * @return array with this week and this month total expenses
     */
    private int[] getExpenses(List<FinanceDocument> docList) {
        int[] data = {0, 0};

        for (FinanceDocument doc : docList) {
            long date = Long.valueOf(doc.getDate());
            if (date >= DateUtils.getFirstDateOfCurrentWeek()) {
                data[0] += doc.getTotalExpense();
            }

            if (date >= DateUtils.getFirstDateOfCurrentMonth()) {
                data[1] += doc.getTotalExpense();
            }


        }
        return data;

    }

}
