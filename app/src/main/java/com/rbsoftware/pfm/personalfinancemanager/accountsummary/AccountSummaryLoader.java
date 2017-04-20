package com.rbsoftware.pfm.personalfinancemanager.accountsummary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds method for loading account summary data in background
 *
 * @author Roman Burzakovskiy
 */
public class AccountSummaryLoader extends AsyncTaskLoader<HashMap<Integer, HashMap<String, Float>>> {
    public static final String ACTION = "AccountSummaryLoader.FORCELOAD";
    private static final String TAG = "AccountSummaryLoader";

    public AccountSummaryLoader(Context context) {
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
    public HashMap<Integer, HashMap<String, Float>> loadInBackground() {
        List<FinanceDocument> financeDocumentList = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(
                        Utils.readFromSharedPreferences(getContext(), "periodAccSummary", "thisWeek"),
                        MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        MainActivity.getActiveAccountId());

        return getValuesFromList(financeDocumentList);
    }


    @Override
    public void deliverResult(HashMap<Integer, HashMap<String, Float>> data) {
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
     * Fetches values from documents and sums them up
     *
     * @param list FinanceDocument list
     * @return hashmap map with 2 nested hashmaps 0- sums of income values, 1- sums of expense values
     */
    private HashMap<Integer, HashMap<String, Float>> getValuesFromList(List<FinanceDocument> list) {
        HashMap<Integer, HashMap<String, Float>> resultMap = new HashMap<>();
        HashMap<String, Float> incomeResultMap = new HashMap<>();
        HashMap<String, Float> expenseResultMap = new HashMap<>();


        for (FinanceDocument item : list) {
            HashMap<Integer, HashMap<String, Float>> convertedValuesMap = item.getConvertedValuesMap();
            HashMap<String, Float> incomeDocumentMap = convertedValuesMap.get(FinanceDocument.CUSTOM_INCOME);

            for (Map.Entry<String, Float> entry : incomeDocumentMap.entrySet()) {
                if (incomeResultMap.containsKey(entry.getKey())) {
                    incomeResultMap.put(entry.getKey(), incomeResultMap.get(entry.getKey()) + entry.getValue());
                } else {
                    incomeResultMap.put(entry.getKey(), entry.getValue());
                }
            }

            HashMap<String, Float> expenseDocumentMap = convertedValuesMap.get(FinanceDocument.CUSTOM_EXPENSE);

            for (Map.Entry<String, Float> entry : expenseDocumentMap.entrySet()) {
                if (expenseResultMap.containsKey(entry.getKey())) {
                    expenseResultMap.put(entry.getKey(), expenseResultMap.get(entry.getKey()) + entry.getValue());
                } else {
                    expenseResultMap.put(entry.getKey(), entry.getValue());
                }
            }

            resultMap.put(FinanceDocument.CUSTOM_INCOME, incomeResultMap);
            resultMap.put(FinanceDocument.CUSTOM_EXPENSE, expenseResultMap);

        }
        return resultMap;

    }
}
