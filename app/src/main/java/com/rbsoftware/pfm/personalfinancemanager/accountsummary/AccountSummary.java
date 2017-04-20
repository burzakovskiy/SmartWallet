package com.rbsoftware.pfm.personalfinancemanager.accountsummary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.ExportData;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.ReportActivity;
import com.rbsoftware.pfm.personalfinancemanager.accountsummary.details.DetailsFragment;
import com.rbsoftware.pfm.personalfinancemanager.goals.GoalCard;
import com.rbsoftware.pfm.personalfinancemanager.goals.GoalLoader;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * A simple {@link Fragment} subclass.
 * Account summary fragment holds account data
 **/
public class AccountSummary extends Fragment {

    private final String TAG = "AccountSummary";
    private final int ACCOUNT_SUMMARY_CARDS = 2;
    private final int BUDGET_ALERT_CARD = 5;
    private final int ACCOUNT_SUMMARY_GOAL_CARD = 6;

    private String selectedItem;
    private TextView mTextViewPeriod;
    private CardViewNative mBalanceCardView;
    private CardViewNative mIncomeCardView;
    private CardViewNative mExpenseCardView;
    private ConnectionDetector mConnectionDetector;
    private FloatingActionButton createNewFinanceDocumentFab;
    private HashMap<String, Float> incomeMap;
    private HashMap<String, Float> expenseMap;

    public AccountSummary() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_summary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[0]);

        if (mBalanceCardView == null) {
            mBalanceCardView = (CardViewNative) getActivity().findViewById(R.id.account_summary_balance_card);
        }
        if (mIncomeCardView == null) {
            mIncomeCardView = (CardViewNative) getActivity().findViewById(R.id.account_summary_income_card);
        }
        if (mExpenseCardView == null) {
            mExpenseCardView = (CardViewNative) getActivity().findViewById(R.id.account_summary_expense_card);
        }

        if (mTextViewPeriod == null) {
            mTextViewPeriod = (TextView) getActivity().findViewById(R.id.tv_period);
        }



        //FAB declaration and listener
        createNewFinanceDocumentFab = (FloatingActionButton) getActivity().findViewById(R.id.account_summary_create_finance_document_fab);
        createNewFinanceDocumentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent report = new Intent(getActivity(), ReportActivity.class);
                startActivityForResult(report, 1);
            }
        });
        createNewFinanceDocumentFab.show();
        getLoaderManager().initLoader(ACCOUNT_SUMMARY_CARDS, null, loaderCallbacksAccountSummaryCards);
        getLoaderManager().initLoader(BUDGET_ALERT_CARD, null, loaderCallbacksBudgetAlertCard);
        getLoaderManager().initLoader(ACCOUNT_SUMMARY_GOAL_CARD, null, loaderCallbacksGoalCard);

        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(getContext());
        }
        MainActivity.mTracker.setScreenName(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();

        //initiates weekly report notification
        initWeeklyReportNotification();

        mTextViewPeriod.setText(Utils.readFromSharedPreferences(getActivity(), "periodTextAccSummary", getResources().getString(R.string.this_week)));
        //check if network is available and send analytics tracker

        if (mConnectionDetector.isConnectingToInternet()) {

            MainActivity.mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("Open").build());
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        createNewFinanceDocumentFab.hide();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                //getting default params from intent
                SparseArray<Object> defaultParams = new SparseArray<>(); //List FinanceDocument constructor parameters
                ArrayList<String> reportResult = data.getStringArrayListExtra("reportDefaultCategoriesResult");

                defaultParams.append(FinanceDocument.PARAM_USERID, MainActivity.getUserId());
                defaultParams.append(FinanceDocument.PARAM_SALARY, Utils.getItem(reportResult, 0));
                defaultParams.append(FinanceDocument.PARAM_RENTAL_INCOME, Utils.getItem(reportResult, 1));
                defaultParams.append(FinanceDocument.PARAM_INTEREST, Utils.getItem(reportResult, 2));
                defaultParams.append(FinanceDocument.PARAM_GIFTS, Utils.getItem(reportResult, 3));
                defaultParams.append(FinanceDocument.PARAM_OTHER_INCOME, Utils.getItem(reportResult, 4));
                defaultParams.append(FinanceDocument.PARAM_TAXES, Utils.getItem(reportResult, 5));
                defaultParams.append(FinanceDocument.PARAM_MORTGAGE, Utils.getItem(reportResult, 6));
                defaultParams.append(FinanceDocument.PARAM_CREDIT_CARD, Utils.getItem(reportResult, 7));
                defaultParams.append(FinanceDocument.PARAM_UTILITIES, Utils.getItem(reportResult, 8));
                defaultParams.append(FinanceDocument.PARAM_FOOD, Utils.getItem(reportResult, 9));
                defaultParams.append(FinanceDocument.PARAM_CAR_PAYMENT, Utils.getItem(reportResult, 10));
                defaultParams.append(FinanceDocument.PARAM_PERSONAL, Utils.getItem(reportResult, 11));
                defaultParams.append(FinanceDocument.PARAM_ACTIVITIES, Utils.getItem(reportResult, 12));
                defaultParams.append(FinanceDocument.PARAM_OTHER_EXPENSE, Utils.getItem(reportResult, 13));

                //getting custom params from intent
                HashMap<String, List<String>> customParams = (HashMap<String, List<String>>) data.getSerializableExtra("reportCustomCategoriesResult");

                createNewFinanceDocument(defaultParams, customParams, data.getStringExtra("documentDate"), data.getStringExtra("comments"));

            }

        }
    }

    /**
     * initiates weekly report notification card
     */
    private void initWeeklyReportNotification() {
        //check if app was installed today
        String installDate = Utils.readFromSharedPreferences(getContext(), "appInstallDate", "");
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = df.format(c.getTime());
        if (!installDate.equals(currentDate)) {
            //check if today is the last day of the week
            int lastDayOfWeek = (c.getFirstDayOfWeek() == Calendar.MONDAY) ? Calendar.SUNDAY : Calendar.SATURDAY;
            if (c.get(Calendar.DAY_OF_WEEK) == lastDayOfWeek) {
                CardViewNative weeklyReportCardView = (CardViewNative) getActivity().findViewById(R.id.account_summary_weekly_report_card);
                weeklyReportCardView.setVisibility(View.VISIBLE);
                WeeklyReportNotificationCard card = new WeeklyReportNotificationCard(getContext());
                weeklyReportCardView.setCard(card);
            }
        }
    }

    /**
     * Creation new document from data
     *
     * @param defaultParams list of finance document fields
     */
    private void createNewFinanceDocument(SparseArray<Object> defaultParams, HashMap<String, List<String>> customParams, String date, String comments) {
        FinanceDocument financeDocument = new FinanceDocument(defaultParams, customParams, date, comments);
        MainActivity.financeDocumentModel.createDocument(financeDocument);
        //Save tag when new doc created
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = df.format(c.getTime());
        Utils.saveToSharedPreferences(getContext(), "createdDate", currentDate);

    }


    //Create options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.account_summary_menu, menu);
        int status = getContext().getSharedPreferences("material_showcaseview_prefs", Context.MODE_PRIVATE)
                .getInt("status_" + TAG, 0);
        if (status != -1) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startShowcase();
                }
            }, 1000);
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_filter:
                showPopup();
                return true;

            case R.id.document_share:
                try {
                    ExportData.exportSummaryAsCsv(getContext(), prepareCsvData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                return super.onOptionsItemSelected(item);
        }


    }
    //Helper methods

    /**
     * Shows account_summary_menu popup menu
     **/
    private void showPopup() {
        View menuItemView = getActivity().findViewById(R.id.action_filter);
        PopupMenu popup = new PopupMenu(getActivity(), menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.period, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();


                switch (id) {
                    case R.id.thisWeek:
                        selectedItem = "thisWeek";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_week));

                        break;
                    case R.id.thisMonth:
                        selectedItem = "thisMonth";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_month));

                        break;
                    case R.id.lastWeek:
                        selectedItem = "lastWeek";
                        mTextViewPeriod.setText(getResources().getString(R.string.last_week));

                        break;
                    case R.id.lastMonth:
                        selectedItem = "lastMonth";
                        mTextViewPeriod.setText(getResources().getString(R.string.last_month));

                        break;
                    case R.id.thisYear:
                        selectedItem = "thisYear";
                        mTextViewPeriod.setText(getResources().getString(R.string.this_year));

                        break;
                }
                Utils.saveToSharedPreferences(getActivity(), "periodAccSummary", selectedItem);
                Utils.saveToSharedPreferences(getActivity(), "periodTextAccSummary", mTextViewPeriod.getText().toString());
                updateCards();
                return false;
            }
        });
        popup.show();

    }

    /**
     * Generates balance, income and expense cards
     *
     * @param data from accountsummary loader
     */
    private void generateCardsData(HashMap<Integer, HashMap<String, Float>> data) {
        incomeMap = data.get(FinanceDocument.CUSTOM_INCOME);
        expenseMap = data.get(FinanceDocument.CUSTOM_EXPENSE);
        float totalIncome = 0;
        if (incomeMap != null) {
            for (float incomeValue : incomeMap.values()) {
                totalIncome += incomeValue;
            }
        }

        float totalExpense = 0;
        if (expenseMap != null) {
            for (float expenseValue : expenseMap.values()) {
                totalExpense += expenseValue;
            }
        }


        String balanceString = String.format(Locale.getDefault(), "%1$,.2f", totalIncome - totalExpense) + " " + MainActivity.defaultCurrency;

        BalanceCard mBalanceCard = new BalanceCard(getContext(), balanceString);
        if (mBalanceCardView.getCard() == null) {
            mBalanceCardView.setCard(mBalanceCard);
        } else {
            mBalanceCardView.replaceCard(mBalanceCard);
        }

        IncomeCard mIncomeCard = new IncomeCard(getContext(), totalIncome, incomeMap);
        mIncomeCard.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                startDetailsFragment(FinanceDocument.CUSTOM_INCOME);
            }
        });
        if (mIncomeCardView.getCard() == null) {
            mIncomeCardView.setCard(mIncomeCard);
        } else {
            mIncomeCardView.replaceCard(mIncomeCard);
        }

        ExpenseCard mExpenseCard = new ExpenseCard(getContext(), totalExpense, expenseMap);
        mExpenseCard.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                startDetailsFragment(FinanceDocument.CUSTOM_EXPENSE);
            }
        });
        if (mExpenseCardView.getCard() == null) {
            mExpenseCardView.setCard(mExpenseCard);
        } else {
            mExpenseCardView.replaceCard(mExpenseCard);
        }

    }

    private void startDetailsFragment(int dataType) {
        Bundle arguments = new Bundle();
        arguments.putInt("dataType", dataType);
        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setArguments(arguments);
        detailsFragment.show(getActivity().getSupportFragmentManager(), "DetailsFragment");

    }

    /**
     * Generates budget alert card
     *
     * @param card of budget alert
     */
    private void generateAlertCardData(BudgetAlertCard card) {
        CardViewNative budgetAlertCardView = (CardViewNative) getActivity().findViewById(R.id.account_summary_budget_alert_card);
        if (!card.isBudgetAlertCardEmpty()) {


            budgetAlertCardView.setVisibility(View.VISIBLE);
            if (budgetAlertCardView.getCard() == null) {
                budgetAlertCardView.setCard(card);
            } else {
                budgetAlertCardView.replaceCard(card);
            }
        } else {
            budgetAlertCardView.setVisibility(View.GONE);
        }
    }

    /**
     * Generates random goal card
     *
     * @param cards list from goalloader
     */
    private void generateGoalCardData(List<GoalCard> cards) {
        CardViewNative goalCardView = (CardViewNative) getActivity().findViewById(R.id.account_summary_goal_card);
        if (!cards.isEmpty()) {
            int position = new Random().nextInt(cards.size());
            GoalCard card = cards.get(position);

            goalCardView.setVisibility(View.VISIBLE);
            if (goalCardView.getCard() == null) {
                goalCardView.setCard(card);
            } else {
                goalCardView.replaceCard(card);
            }
        } else {
            goalCardView.setVisibility(View.GONE);
        }
    }

    /**
     * Compiles all views data into export ready list
     *
     * @return data
     **/
    private List<String[]> prepareCsvData() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{getString(R.string.period), mTextViewPeriod.getText().toString()});
        data.add(new String[]{"", ""});
        data.add(new String[]{getString(R.string.balance), ((BalanceCard) mBalanceCardView.getCard()).getBalanceValue()});
        data.add(new String[]{"", ""});

        data.add(new String[]{getString(R.string.income), ((IncomeCard) mIncomeCardView.getCard()).getTotalIncomeValue()});
        data.add(new String[]{"", ""});
        if (incomeMap != null) {
            for (Map.Entry<String, Float> entry : incomeMap.entrySet()) {
                data.add(new String[]{Utils.keyToString(getContext(), entry.getKey()), String.format(Locale.getDefault(), "%1$,.2f", entry.getValue()) + " " + MainActivity.defaultCurrency});
            }
        }

        data.add(new String[]{"", ""});
        data.add(new String[]{getString(R.string.expense), ((ExpenseCard) mExpenseCardView.getCard()).getTotalExpenseValue()});
        data.add(new String[]{"", ""});
        if (expenseMap != null) {
            for (Map.Entry<String, Float> entry : expenseMap.entrySet()) {
                data.add(new String[]{Utils.keyToString(getContext(), entry.getKey()), String.format(Locale.getDefault(), "%1$,.2f", entry.getValue()) + " " + MainActivity.defaultCurrency});
            }
        }


        return data;
    }


    /**
     * Sends broadcast intent to update cards
     */
    public void updateCards() {
        Intent intentAccountSummaryLoader = new Intent(AccountSummaryLoader.ACTION);
        Intent intentBudgetAlertLoader = new Intent(BudgetAlertLoader.ACTION);
        Intent intentGoalLoader = new Intent(GoalLoader.ACTION);

        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intentAccountSummaryLoader);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intentBudgetAlertLoader);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intentGoalLoader);
    }


    private final LoaderManager.LoaderCallbacks<HashMap<Integer, HashMap<String, Float>>> loaderCallbacksAccountSummaryCards = new LoaderManager.LoaderCallbacks<HashMap<Integer, HashMap<String, Float>>>() {
        @Override
        public Loader<HashMap<Integer, HashMap<String, Float>>> onCreateLoader(int id, Bundle args) {
            return new AccountSummaryLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<HashMap<Integer, HashMap<String, Float>>> loader, HashMap<Integer, HashMap<String, Float>> data) {
            generateCardsData(data);
        }

        @Override
        public void onLoaderReset(Loader<HashMap<Integer, HashMap<String, Float>>> loader) {
        }
    };

    private final LoaderManager.LoaderCallbacks<BudgetAlertCard> loaderCallbacksBudgetAlertCard = new LoaderManager.LoaderCallbacks<BudgetAlertCard>() {
        @Override
        public Loader<BudgetAlertCard> onCreateLoader(int id, Bundle args) {
            return new BudgetAlertLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<BudgetAlertCard> loader, BudgetAlertCard data) {
            generateAlertCardData(data);
        }

        @Override
        public void onLoaderReset(Loader<BudgetAlertCard> loader) {
        }
    };

    private final LoaderManager.LoaderCallbacks<List<GoalCard>> loaderCallbacksGoalCard = new LoaderManager.LoaderCallbacks<List<GoalCard>>() {
        @Override
        public Loader<List<GoalCard>> onCreateLoader(int id, Bundle args) {
            return new GoalLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<GoalCard>> loader, List<GoalCard> data) {
            generateGoalCardData(data);
        }

        @Override
        public void onLoaderReset(Loader<List<GoalCard>> loader) {
        }
    };

    /**
     * Runs showcase presentation on fragment start
     **/
    private void startShowcase() {
        if (getActivity().findViewById(R.id.action_filter) != null && getActivity().findViewById(R.id.document_share) != null) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view
            config.setDismissTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), TAG);
            sequence.setConfig(config);
            sequence.addSequenceItem(getActivity().findViewById(R.id.action_filter), getString(R.string.action_filter), getString(R.string.got_it));
            sequence.addSequenceItem(getActivity().findViewById(R.id.document_share), getString(R.string.document_share), getString(R.string.got_it));

            MaterialShowcaseView detailsView = new MaterialShowcaseView.Builder(getActivity())
                    .setTarget(mIncomeCardView)
                    .setDismissTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                    .setContentText(R.string.details_view)
                    .setDismissText(R.string.ok)
                    .withRectangleShape()
                    .build();
            sequence.addSequenceItem(detailsView);
            sequence.start();
        }
    }

}