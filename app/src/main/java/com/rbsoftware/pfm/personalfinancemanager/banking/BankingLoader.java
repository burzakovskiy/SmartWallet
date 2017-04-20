package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds methods for loading banking data in background
 *
 * @author Roman Burzakovskiy
 */
public class BankingLoader extends AsyncTaskLoader<List<PreFinanceDocument>> {
    private static final String TAG = "BankingLoader";
    public static final String ACTION = "BankingLoader.FORCELOAD";
    /**
     * Weak reference to MainActivity
     */
    private WeakReference<Activity> weeakActivity;

    public BankingLoader(Context context, Activity activity) {
        super(context);
        weeakActivity = new WeakReference<>(activity);
    }


    @Override
    protected void onStartLoading() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter(ACTION);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        forceLoad();
    }

    @Override
    public List<PreFinanceDocument> loadInBackground() {
        List<PreFinanceDocument> preFinanceDocumentList = new ArrayList<>();

        List<BankingCardDocument> bankingCardDocumentList = MainActivity.financeDocumentModel.queryBankingCardDocuments(MainActivity.getUserId(), BankingCardDocument.DOC_TYPE);
        if (bankingCardDocumentList != null && !bankingCardDocumentList.isEmpty()) {
            for (BankingCardDocument bankingCardDocument : bankingCardDocumentList) {
                int bank = Integer.valueOf(bankingCardDocument.getBank());
                String preFinanceDocumentDate = Utils.readFromSharedPreferences(getContext(), "preFinanceDocumentDate", "");
                String date;
                if (preFinanceDocumentDate.isEmpty()) {
                    date = bankingCardDocument.getDate();
                } else {
                    date = preFinanceDocumentDate;
                }
                String cardNumber = bankingCardDocument.getCardNumber();
                String accountNumber = bankingCardDocument.getAccountNumber();
                //Raiffeisen Bank Aval
                if (bank == 0) {
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {

                        RaiffeisenBankAvalSMSParser parser = new RaiffeisenBankAvalSMSParser(getContext(), -1, date);
                        parser.setCardNumber(cardNumber.substring(4));
                        parser.setAccountNumber(accountNumber);
                        preFinanceDocumentList = parser.getParsedSMSList();
                    } else {

                        ActivityCompat.requestPermissions(weeakActivity.get(),
                                new String[]{Manifest.permission.READ_SMS},
                                MainActivity.PERMISSIONS_REQUEST_READ_SMS);

                    }
                    //Ukrsotsbank
                } else if (bank == 1) {

                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {

                        UkrsotsbankSMSParser parser = new UkrsotsbankSMSParser(getContext(), -1, date);
                        parser.setCardNumber(cardNumber.substring(4));
                        parser.setAccountNumber(accountNumber);
                        preFinanceDocumentList = parser.getParsedSMSList();
                    } else {

                        ActivityCompat.requestPermissions(weeakActivity.get(),
                                new String[]{Manifest.permission.READ_SMS},
                                MainActivity.PERMISSIONS_REQUEST_READ_SMS);

                    }
                    //State Saving Bank
                } else if (bank == 2) {
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {

                        OschadbankSMSParser parser = new OschadbankSMSParser(getContext(), -1, date);
                        parser.setCardNumber(cardNumber.substring(4));
                        parser.setAccountNumber(accountNumber);
                        preFinanceDocumentList = parser.getParsedSMSList();
                    } else {

                        ActivityCompat.requestPermissions(weeakActivity.get(),
                                new String[]{Manifest.permission.READ_SMS},
                                MainActivity.PERMISSIONS_REQUEST_READ_SMS);

                    }
                    //Ukrsibbank
                }else if (bank == 3) {
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {

                        UkrsibbankSMSParser parser = new UkrsibbankSMSParser(getContext(), -1, date);
                        parser.setCardNumber(cardNumber.substring(4));
                        parser.setAccountNumber(accountNumber);
                        preFinanceDocumentList = parser.getParsedSMSList();
                    } else {

                        ActivityCompat.requestPermissions(weeakActivity.get(),
                                new String[]{Manifest.permission.READ_SMS},
                                MainActivity.PERMISSIONS_REQUEST_READ_SMS);

                    }
                }


            }
        }
        return preFinanceDocumentList;
    }

    @Override
    public void deliverResult(List<PreFinanceDocument> data) {
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
