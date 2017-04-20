package com.rbsoftware.pfm.personalfinancemanager.weeklyreport;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocumentModel;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Holds methods for loading weekly report data in background
 * Created by Roman Burzakovskiy on 7/15/2016.
 */
public class WeeklyReportLoader extends AsyncTaskLoader<HashMap<String, Card>> {
    private static final String TAG = "WeeklyReportLoader";

    public WeeklyReportLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public HashMap<String, Card> loadInBackground() {
        HashMap<String, Card> resultMap = new HashMap<>();
        List<FinanceDocument> docListThisWeek = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(DateUtils.THIS_WEEK,
                        MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());

        List<FinanceDocument> docListLastWeek = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(DateUtils.LAST_WEEK,
                        MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());


        WeeklyReportGeneralCard weeklyReportGeneralCard = new WeeklyReportGeneralCard(
                getContext(),
                getIncome(docListThisWeek),
                getIncome(docListLastWeek),
                getExpense(docListThisWeek),
                getExpense(docListLastWeek));
        resultMap.put("WeeklyReportGeneralCard", weeklyReportGeneralCard);

        //preparing data for line chart card
        List<String[]> incomeChartThisWeek = getLineChartData(docListThisWeek, FinanceDocument.CUSTOM_INCOME);

        List<String[]> expenseChartThisWeek = getLineChartData(docListThisWeek, FinanceDocument.CUSTOM_EXPENSE);

        WeeklyReportLineChartCard weeklyReportLineChartCard = new WeeklyReportLineChartCard(
                getContext(),
                incomeChartThisWeek,
                expenseChartThisWeek);
        resultMap.put("WeeklyReportLineChartCard", weeklyReportLineChartCard);


        //preparing data for top transaction card
        List<String[]> topIncomeTransactions = getTopTransactions(docListThisWeek, FinanceDocument.CUSTOM_INCOME, 3);
        List<String[]> topExpenseTransactions = getTopTransactions(docListThisWeek, FinanceDocument.CUSTOM_EXPENSE, 3);

        WeeklyReportTopTransactionsCard weeklyReportTopTransactionsCard = new WeeklyReportTopTransactionsCard(getContext(), topIncomeTransactions, topExpenseTransactions);
        resultMap.put("WeeklyReportTopTransactionsCard", weeklyReportTopTransactionsCard);
        return resultMap;
    }

    /**
     * Gets total income from document list
     *
     * @param docList list of finance documents
     * @return total income
     */
    private float getIncome(List<FinanceDocument> docList) {
        float income = 0;
        for (FinanceDocument item : docList) {
            income += item.getTotalIncome();
        }
        return income;
    }

    /**
     * Gets total expense from document list
     *
     * @param docList list of finance documents
     * @return total expense
     */
    private float getExpense(List<FinanceDocument> docList) {
        float expense = 0;
        for (FinanceDocument item : docList) {
            expense += item.getTotalExpense();
        }
        return expense;
    }

    /**
     * Gets data for line chart
     *
     * @param docList  list of finance documents
     * @param dataType 0- income, 1-expense
     * @return List of line chart data, 0- value, 1-date
     */
    private List<String[]> getLineChartData(List<FinanceDocument> docList, int dataType) {
        float value;
        int i;
        List<String[]> data = new ArrayList<>();
        i = 0;
        for (FinanceDocument doc : docList) {
            if (dataType == FinanceDocument.CUSTOM_INCOME) {
                value = doc.getTotalIncome();
            } else {
                value = doc.getTotalExpense();
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

        Collections.reverse(data);
        return data;
    }

    /**
     * Gets list of top transactions
     *
     * @param docList  list of finance documents
     * @param dataType 0-income/1-expense
     * @param limit    number of entries to return
     * @return list of top transactions
     * 0- date
     * 1- category
     * 2 - value
     */
    private List<String[]> getTopTransactions(List<FinanceDocument> docList, int dataType, int limit) {
        List<String[]> topTransactionsList = new ArrayList<>();

        //putting all data into list
        for (FinanceDocument document : docList) {
            HashMap<String, Float> unsortedMap = document.getConvertedValuesMap().get(dataType);
            for (Map.Entry<String, Float> entry : unsortedMap.entrySet()) {

                topTransactionsList.add(new String[]{document.getDate(), entry.getKey(), String.valueOf(entry.getValue())});

            }
        }

        //sorting list
        Collections.sort(topTransactionsList, new Comparator<String[]>() {
            @Override
            public int compare(String[] t1, String[] t2) {
                return Float.valueOf(t2[2]).compareTo(Float.valueOf(t1[2]));
            }
        });

        //removing redundant elements
        int listSize = topTransactionsList.size();
        if (listSize > limit) {
            topTransactionsList.subList(limit, listSize).clear();
        }

        return topTransactionsList;
    }


}
