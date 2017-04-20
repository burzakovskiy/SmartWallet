package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds methods for loading banking cards in background
 *
 * @author Roman Burzakovskiy
 */
public class BankingCardLoader extends AsyncTaskLoader<List<BankingCard>> {
    public static final String ACTION = "BankingCardLoader.FORCELOAD";

    public BankingCardLoader(Context context) {
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
    public List<BankingCard> loadInBackground() {
        ArrayList<BankingCard> cards = new ArrayList<>();
        List<BankingCardDocument> docList = MainActivity.financeDocumentModel.queryBankingCardDocuments(MainActivity.getUserId(), BankingCardDocument.DOC_TYPE);
        for (BankingCardDocument doc : docList) {
            cards.add(new BankingCard(getContext(), doc));
        }
        return cards;
    }

    @Override
    public void deliverResult(List<BankingCard> data) {
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
}

