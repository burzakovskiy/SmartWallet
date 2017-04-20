package com.rbsoftware.pfm.personalfinancemanager.accounts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds methods for loading account cards in background
 *
 * @author Roman Burzakovskiy
 */
public class AccountsLoader extends AsyncTaskLoader<List<AccountCard>> {

    public static final String ACTION = "AccountsLoader.FORCELOAD";
    private static final String TAG = "AccountsLoader";

    public AccountsLoader(Context context) {
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
    public List<AccountCard> loadInBackground() {
        ArrayList<AccountCard> cards = new ArrayList<>();
        AccountDocument doc = MainActivity.financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());
        if (doc == null) {
            HashMap<String, List<String>> values = new HashMap<>();
            List<String> mainAccount = new ArrayList<>();
            mainAccount.add(getContext().getString(R.string.main_wallet));
            values.put(FinanceDocument.MAIN_ACCOUNT, mainAccount);
            doc = new AccountDocument(MainActivity.getUserId(), values);
            MainActivity.financeDocumentModel.createDocument(doc);

        }
        HashMap<String, List<String>> accounts = doc.getAccountsMap();
        for (Map.Entry<String, List<String>> entry : accounts.entrySet()) {
            List<FinanceDocument> financeDocumentList =
                    MainActivity
                            .financeDocumentModel
                            .queryFinanceDocumentsByDate(DateUtils.FROM_START, MainActivity.getUserId(),
                                    FinanceDocument.DOC_TYPE,
                                    entry.getKey());

            float totalBalance = (!financeDocumentList.isEmpty()) ? getTotalBalance(financeDocumentList) : 0;
            AccountCard card = new AccountCard(getContext(), entry.getKey(), entry.getValue().get(0), totalBalance);
            cards.add(card);

        }
        return cards;
    }

    @Override
    public void deliverResult(List<AccountCard> data) {
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
     * Calculates total balance value
     *
     * @param list of Finance documents
     * @return total balance value
     */
    private float getTotalBalance(List<FinanceDocument> list) {

        float totalIncome = 0;
        float totalExpense = 0;
        for (FinanceDocument doc : list) {
            totalIncome += doc.getTotalIncome();
            totalExpense += doc.getTotalExpense();
        }
        return totalIncome - totalExpense;
    }

}
