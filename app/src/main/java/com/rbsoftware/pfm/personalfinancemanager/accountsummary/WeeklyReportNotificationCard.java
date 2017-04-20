package com.rbsoftware.pfm.personalfinancemanager.accountsummary;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;
import com.rbsoftware.pfm.personalfinancemanager.weeklyreport.WeeklyReportActivity;

import java.util.Calendar;
import java.util.Locale;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Holds methods for building notification weekly report card
 * Created by Roman Burzakovskiy on 7/14/2016.
 */
public class WeeklyReportNotificationCard extends Card {
    private static final String TAG = "ReportNotification";

    public WeeklyReportNotificationCard(Context context) {
        super(context, R.layout.weekly_report_notification_card_main_inner_layout);
        WeeklyReportNotificationHeader header = new WeeklyReportNotificationHeader(context);
        this.addCardHeader(header);
        this.setBackgroundColorResourceId(R.color.notification_card);

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        long time = calendar.getTimeInMillis() / 1000;
        String endReport = DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, String.valueOf(time));
        calendar.add(Calendar.DAY_OF_WEEK, -6);
        time = calendar.getTimeInMillis() / 1000;
        String startReport = DateUtils.getNormalDate(DateUtils.DATE_FORMAT_SHORT, String.valueOf(time));
        final String reportText = String.format(Locale.getDefault(), getContext().getString(R.string.weekly_report_from_till), startReport, endReport);
        TextView reportTextView = (TextView) view.findViewById(R.id.weekly_report_notification_textview);
        reportTextView.setText(reportText);
        Button showReportButton = (Button) view.findViewById(R.id.weekly_report_notification_show_button);

        showReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WeeklyReportActivity.class);
                getContext().startActivity(intent);
            }
        });
    }

    private class WeeklyReportNotificationHeader extends CardHeader {
        public WeeklyReportNotificationHeader(Context context) {
            super(context, R.layout.weekly_report_notification_card_header_inner_layout);
        }
    }
}
