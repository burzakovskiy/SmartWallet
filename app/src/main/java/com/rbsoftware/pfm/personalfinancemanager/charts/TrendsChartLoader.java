package com.rbsoftware.pfm.personalfinancemanager.charts;

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
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds method for loading trends data in background
 *
 * @author Roman Burzakovskiy
 */
public class TrendsChartLoader extends AsyncTaskLoader<List<String[]>> {

    private static final String TAG = "TrendsChartLoader";
    public static final String ACTION = "TrendsChartLoader.FORCELOAD";


    public TrendsChartLoader(Context context) {
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
    public List<String[]> loadInBackground() {

        List<FinanceDocument> financeDocumentList = MainActivity.financeDocumentModel.queryFinanceDocumentsByDate(
                Utils.readFromSharedPreferences(getContext(), "periodTrend", "thisWeek"),
                MainActivity.getUserId(),
                FinanceDocument.DOC_TYPE,
                FinanceDocumentModel.ORDER_ASC,
                MainActivity.getActiveAccountId());
        String lineId = Utils.readFromSharedPreferences(getContext(),
                "checkedLine", "-2");
        return getDataFromDocument(lineId, financeDocumentList);
    }

    @Override
    public void deliverResult(List<String[]> data) {
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
     * Fetches values from document fro line chart
     *
     * @param lineId  chart type
     * @param docList list of finance documents
     * @return data values and dates list of arrays
     */
    private List<String[]> getDataFromDocument(String lineId, List<FinanceDocument> docList) {
        float value;
        int i;
        List<String[]> data = new ArrayList<>();
        if (Utils.isNumber(lineId)) {
            switch (Integer.valueOf(lineId)) {
                case -2:
                    i = 0;
                    for (FinanceDocument doc : docList) {
                        value = doc.getTotalIncome() - doc.getTotalExpense();
                        if (i != 0) {
                            if (DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate()).equals(data.get(i - 1)[1])) {
                                data.get(i - 1)[0] = String.valueOf(Float.valueOf(data.get(i - 1)[0]) + value);
                            } else {

                                data.add(new String[]{
                                        Float.toString(Float.valueOf(data.get(i - 1)[0]) + value),
                                        DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                                });
                                i++;
                            }
                        } else {
                            data.add(new String[]{
                                    Float.toString(value),
                                    DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                            });
                            i++;
                        }

                    }
                    return data;

                case -1:
                    i = 0;
                    for (FinanceDocument doc : docList) {
                        value = doc.getTotalIncome();
                        if (value != 0) {
                            if (i != 0) {
                                if (DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate()).equals(data.get(i - 1)[1])) {
                                    data.get(i - 1)[0] = String.valueOf(Float.valueOf(data.get(i - 1)[0]) + value);
                                } else {
                                    data.add(new String[]{
                                            Float.toString(value),
                                            DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                                    });
                                    i++;
                                }
                            } else {
                                data.add(new String[]{
                                        Float.toString(value),
                                        DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                                });
                                i++;
                            }
                        }
                    }
                    return data;

                case 0:
                    i = 0;
                    for (FinanceDocument doc : docList) {
                        value = doc.getTotalExpense();
                        if (value != 0) {
                            if (i != 0) {
                                if (DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate()).equals(data.get(i - 1)[1])) {
                                    data.get(i - 1)[0] = String.valueOf(Float.valueOf(data.get(i - 1)[0]) + value);
                                } else {
                                    data.add(new String[]{
                                            Float.toString(value),
                                            DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                                    });
                                    i++;
                                }
                            } else {
                                data.add(new String[]{
                                        Float.toString(value),
                                        DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                                });
                                i++;
                            }
                        }
                    }
                    return data;
            }

        }
        i = 0;
        for (FinanceDocument doc : docList) {
            value =0;
            if (doc.getConvertedValuesMap().get(FinanceDocument.CUSTOM_INCOME).containsKey(lineId)) {
                value = doc.getConvertedValuesMap().get(FinanceDocument.CUSTOM_INCOME).get(lineId);
            } else if (doc.getConvertedValuesMap().get(FinanceDocument.CUSTOM_EXPENSE).containsKey(lineId)) {
                value = doc.getConvertedValuesMap().get(FinanceDocument.CUSTOM_EXPENSE).get(lineId);
            }
            if (value != 0) {
                if (i != 0) {
                    if (DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate()).equals(data.get(i - 1)[1])) {
                        data.get(i - 1)[0] = String.valueOf(Float.valueOf(data.get(i - 1)[0]) + value);
                    } else {
                        data.add(new String[]{
                                Float.toString(value),
                                DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                        });
                        i++;
                    }
                } else {
                    data.add(new String[]{
                            Float.toString(value),
                            DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, doc.getDate())
                    });
                    i++;
                }
            }
        }

        return data;
    }


}
