package com.rbsoftware.pfm.personalfinancemanager.accountsummary.details;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocumentModel;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads details data in background
 * Created by Roman Burzakovskiy on 7/13/2016.
 */
public class DetailsLoader extends AsyncTaskLoader<LinkedHashMap<String, List<String[]>>> {
    private static final String TAG = "DetailsLoader";
    /**
     * income or expense data
     */
    private int dataType;

    public DetailsLoader(Context context, int dataType) {
        super(context);
        this.dataType = dataType;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public LinkedHashMap<String, List<String[]>> loadInBackground() {
        List<FinanceDocument> financeDocumentList = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(
                        Utils.readFromSharedPreferences(getContext(), "periodAccSummary", "thisWeek"),
                        MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());
        return generateResultMap(financeDocumentList);
    }

    /**
     * Maps transactions to dates
     *
     * @param financeDocumentList list of finance documents
     * @return transactions to dates map
     * key -date
     * value - list of arrays 0- category, 1-value
     */
    private LinkedHashMap<String, List<String[]>> generateResultMap(List<FinanceDocument> financeDocumentList) {

        LinkedHashMap<String, List<String[]>> resultMap = new LinkedHashMap<>();
        for (FinanceDocument doc : financeDocumentList) {

            String date = DateUtils.getNormalDate(DateUtils.DATE_FORMAT_LONG, doc.getDate());
            HashMap<String, Float> valuesMap = doc.getConvertedValuesMap().get(dataType);

            String sign = "+";
            if (dataType == FinanceDocument.CUSTOM_EXPENSE) {
                sign = "-";
            }
            for (Map.Entry<String, Float> entry : valuesMap.entrySet()) {
                String valueString = sign + String.format(Locale.getDefault(), "%1$,.2f", entry.getValue()) + " " + MainActivity.defaultCurrency;
                if (resultMap.containsKey(date)) {
                    resultMap.get(date).add(new String[]{
                            Utils.keyToString(getContext(), entry.getKey()),
                            valueString});


                } else {
                    List<String[]> resultList = new ArrayList<>();
                    resultList.add(new String[]{
                            Utils.keyToString(getContext(), entry.getKey()),
                            valueString});
                    resultMap.put(date, resultList);

                }

            }
        }

        return resultMap;
    }
}
