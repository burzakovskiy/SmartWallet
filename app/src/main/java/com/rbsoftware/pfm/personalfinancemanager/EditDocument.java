package com.rbsoftware.pfm.personalfinancemanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;

public class EditDocument extends AppCompatActivity {
    private final String TAG = "EditDocument";
    private String docId;
    private ReportCard reportCard;
    private ConnectionDetector mConnectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.report_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        }

        if (savedInstanceState == null) {
            docId = getIntent().getExtras().getString("docId");

        }else{
            docId = savedInstanceState.getString("docId");
        }
        reportCard = new ReportCard(this);
        reportCard.setup(this, savedInstanceState, docId);
        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(this);
        }
        MainActivity.mTracker.setScreenName(TAG);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if network is available and send analytics tracker

        if (mConnectionDetector.isConnectingToInternet()) {

            MainActivity.mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("Open").build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportCard.isCommentDialogWindowOpen) {
            reportCard.commentDialog.dismiss();
            reportCard.isCommentDialogWindowOpen = true;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }
        if (id == R.id.report_toolbar_done) {
            if (reportCard.validateFields() && reportCard.validateSpinner()) {
                Intent intent = new Intent();
                intent.putExtra("oldDocId", docId);
                intent.putStringArrayListExtra("editDefaultCategoriesResult", reportCard.getDefaultCategoriesResult());
                intent.putExtra("editCustomCategoriesResult", reportCard.getCustomCategoriesResult());
                intent.putExtra("documentDate", reportCard.getDate());
                intent.putExtra("comments", reportCard.getComments());
                setResult(RESULT_OK, intent);
                finish();
            }

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(reportCard.getElementsToSave());
        outState.putString("docId", docId);

        super.onSaveInstanceState(outState);

    }


}
