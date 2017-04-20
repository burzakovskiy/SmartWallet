package com.rbsoftware.pfm.personalfinancemanager.goals;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocumentModel;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Holds methods for loading goals data in background
 *
 * @author Roman Burzakovskiy
 */
public class GoalLoader extends AsyncTaskLoader<List<GoalCard>> {

    public static final String ACTION = "GoalLoader.FORCELOAD";
    private static final String TAG = "GoalLoader";

    public GoalLoader(Context context) {
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
    public List<GoalCard> loadInBackground() {
        ArrayList<GoalCard> cards = new ArrayList<>();
        //querying list goal documents
        List<GoalDocument> goalDocumentList = MainActivity
                .financeDocumentModel
                .queryGoalDocumentsByDate(DateUtils.FROM_START,
                        MainActivity.getUserId(),
                        GoalDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());
        int numberOfGoals = goalDocumentList.size();

        //querying list of finance douments
        List<FinanceDocument> financeDocumentList = MainActivity
                .financeDocumentModel
                .queryFinanceDocumentsByDate(DateUtils.FROM_START,
                        MainActivity.getUserId(),
                        FinanceDocument.DOC_TYPE,
                        FinanceDocumentModel.ORDER_DESC,
                        MainActivity.getActiveAccountId());

        if (!financeDocumentList.isEmpty()) {
            float totalBalance = getTotalBalance(financeDocumentList);
            if (totalBalance > 0) {
                int numberOfMonths = getNumberOfMonths(financeDocumentList);
                float averageBalance = totalBalance / numberOfMonths;
                if (averageBalance == 0) averageBalance = 1;

                //sort documents in ascending order by value
                Collections.sort(goalDocumentList, new Comparator<GoalDocument>() {
                    @Override
                    public int compare(GoalDocument goalDocument1, GoalDocument goalDocument2) {
                        return Math.round(goalDocument1.getConvertedValue() - goalDocument2.getConvertedValue());
                    }
                });
                //building month left list
                List<Integer> monthLeftList = getMonthsLeftList(goalDocumentList, numberOfGoals, totalBalance, averageBalance);
                for (int i = 0; i < numberOfGoals; i++) {
                    //building cards list
                    cards.add(generateMaterialCard(goalDocumentList.get(i), monthLeftList.get(i)));
                }
            }
        }
        return cards;
    }

    @Override
    public void deliverResult(List<GoalCard> data) {
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

    /**
     * Calculates number of months user uses app
     *
     * @param list of Finance documents
     * @return number of months
     */
    private int getNumberOfMonths(List<FinanceDocument> list) {

        long firstDate = Long.valueOf(list.get(list.size() - 1).getDate());
        long lastDate = Long.valueOf(list.get(0).getDate());

        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        c.setTimeInMillis(firstDate * 1000);
        int firstYear = c.get(Calendar.YEAR);
        int firstMonth = c.get(Calendar.MONTH);
        c.setTimeInMillis(lastDate * 1000);
        int lastYear = c.get(Calendar.YEAR);
        int lastMonth = c.get(Calendar.MONTH);
        return (lastYear - firstYear) * 12 + (lastMonth - firstMonth) + 1;
    }

    /**
     * Generates Goal card layout
     *
     * @param goalDocument goal document
     * @param monthsLeft   number of month left for goal completion
     * @return Goal card
     */
    private GoalCard generateMaterialCard(final GoalDocument goalDocument, int monthsLeft) {
        String textOverImage = goalDocument.getName() + " " + String.format(Locale.getDefault(), "%1$,.2f", goalDocument.getValue()) + " " + goalDocument.getCurrency();
        String title = getContext().getString(R.string.goal_card_title) + " " + timeLeftToText(monthsLeft);

        GoalCard card = (GoalCard) GoalCard.with(getContext())
                .setTextOverImage(textOverImage)
                .setTitle(title)
                .setSubTitle(getContext().getString(R.string.goal_disclaimer))
                .useDrawableExternal(new GoalCard.DrawableExternal() {
                    @Override
                    public void setupInnerViewElements(ViewGroup parent, View viewImage) {
                        if (goalDocument.getImageName() != null) {
                            Bitmap bitmap = MainActivity.financeDocumentModel.getGoalImage(goalDocument.getDocumentRevision().getId(), goalDocument.getImageName());
                            ((ImageView) viewImage).setImageBitmap(bitmap);
                        } else {
                            ((ImageView) viewImage).setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.spaceship));
                        }
                    }
                }).build(new GoalCard(getContext()));
        card.setDocument(goalDocument);
        return card;
    }

    /**
     * Calculates number of months required to achieve goal
     *
     * @param goalDocumentList goal documents list
     * @param numberOfGoals    number of goal documents
     * @param totalBalance     total balance
     * @param averageBalance   average balance
     * @return number of months list
     */
    private List<Integer> getMonthsLeftList(List<GoalDocument> goalDocumentList, int numberOfGoals, float totalBalance, float averageBalance) {
        List<Integer> monthsLeftList = new ArrayList<>();
        for (int i = 1; i <= numberOfGoals; i++) {
            //get goal value
            float goalValue = goalDocumentList.get(i - 1).getConvertedValue();
            // previous goal value, if there no previous goal use total balance
            float prevGoalValue = (i == 1) ? totalBalance : goalDocumentList.get(i - 2).getConvertedValue();
            /**
             * month left calculation formula
             * M[n] = (G[n] - G[n-1]) *(N-n+1)/A +M[n-1]
             */
            int monthsLeft = Math.round(((goalValue - prevGoalValue) * (numberOfGoals - i + 1)) / averageBalance);
            if (monthsLeft < 0) monthsLeft = 0;
            if (i > 1) monthsLeft += monthsLeftList.get(i - 2);
            monthsLeftList.add(monthsLeft);
        }

        return monthsLeftList;
    }

    /**
     * Convert number of months left into years and month string
     *
     * @param monthsLeft months left
     * @return converted into user friendly string
     */
    private String timeLeftToText(int monthsLeft) {
        int years = monthsLeft / 12;
        int months = monthsLeft % 12;

        if ((years != 0) && (months == 0)) return getYearsString(years);
        return getYearsString(years) + " " + getMonthsString(months);
    }

    /**
     * Converts integer value of years into user friendly string
     *
     * @param years left
     * @return converted value
     */
    private String getYearsString(int years) {
        if (years == 0) return "";
        if (years == 1)
            return years + " " + getContext().getResources().getStringArray(R.array.year_array)[0];
        if ((years > 1) && (years < 5))
            return years + " " + getContext().getResources().getStringArray(R.array.year_array)[1];
        return years + " " + getContext().getResources().getStringArray(R.array.year_array)[2];
    }

    /**
     * Converts integer value of months into user friendly string
     *
     * @param months left
     * @return converted value
     */
    private String getMonthsString(int months) {
        if (months == 0) return getContext().getResources().getStringArray(R.array.month_array)[0];
        if (months == 1)
            return months + " " + getContext().getResources().getStringArray(R.array.month_array)[1];
        if ((months > 1) && (months < 5))
            return months + " " + getContext().getResources().getStringArray(R.array.month_array)[2];
        return months + " " + getContext().getResources().getStringArray(R.array.month_array)[3];
    }
}
