package com.rbsoftware.pfm.personalfinancemanager.weeklyreport;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.HashMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardViewNative;

public class WeeklyReportActivity extends AppCompatActivity {
    private final int WEEKLY_REPORT_LOADER_ID = 1;
    private final String TAG = "WeeklyReportActivity";

    /**
     * Network connection detector
     */
    private ConnectionDetector mConnectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.weekly_report_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        }
        setTitle(getString(R.string.weekly_report));

        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(this);
        }
        MainActivity.mTracker.setScreenName(TAG);

        //loading report data in background
        getSupportLoaderManager().initLoader(WEEKLY_REPORT_LOADER_ID, null, loaderCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();

        //check if network is available and send analytics tracker

        if (mConnectionDetector.isConnectingToInternet()) {

            MainActivity.mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("Open").build());
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets cards to card views
     *
     * @param cardsMap hashmap of cards
     */
    private void generateCards(HashMap<String, Card> cardsMap) {
        //getting general card
        CardViewNative generalCard = (CardViewNative) findViewById(R.id.weekly_report_general_card);
        if (generalCard.getCard() == null) {
            generalCard.setCard(cardsMap.get("WeeklyReportGeneralCard"));
        } else {
            generalCard.replaceCard(cardsMap.get("WeeklyReportGeneralCard"));
        }

        //getting line chart card
        CardViewNative lineChartCard = (CardViewNative) findViewById(R.id.weekly_report_line_chart_card);
        if (lineChartCard.getCard() == null) {
            lineChartCard.setCard(cardsMap.get("WeeklyReportLineChartCard"));
        } else {
            lineChartCard.replaceCard(cardsMap.get("WeeklyReportLineChartCard"));
        }
        //getting top transactions card
        CardViewNative topTransactionsCard = (CardViewNative) findViewById(R.id.weekly_report_top_transactions_card);
        if (topTransactionsCard.getCard() == null) {
            topTransactionsCard.setCard(cardsMap.get("WeeklyReportTopTransactionsCard"));
        } else {
            topTransactionsCard.replaceCard(cardsMap.get("WeeklyReportTopTransactionsCard"));
        }


    }

    private final LoaderManager.LoaderCallbacks<HashMap<String, Card>> loaderCallbacks = new LoaderManager.LoaderCallbacks<HashMap<String, Card>>() {
        @Override
        public Loader<HashMap<String, Card>> onCreateLoader(int id, Bundle args) {
            return new WeeklyReportLoader(getApplicationContext());
        }

        @Override
        public void onLoadFinished(Loader<HashMap<String, Card>> loader, HashMap<String, Card> data) {
            generateCards(data);
        }

        @Override
        public void onLoaderReset(Loader<HashMap<String, Card>> loader) {
        }
    };

}
