package com.rbsoftware.pfm.personalfinancemanager;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Sender reminder message to user
 *
 * @author Roman Burzakovskiy
 */
public class NotificationReceiver extends BroadcastReceiver {
    private final static String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context mContext, Intent intent) {
        WakefulIntentService.acquireStaticLock(mContext); //acquire a partial WakeLock

        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = df.format(c.getTime());
        String updatedDate = Utils.readFromSharedPreferences(mContext, "createdDate", "");
        String installDate = Utils.readFromSharedPreferences(mContext, "appInstallDate", "");
        if (!updatedDate.equals(currentDate) && !installDate.equals(currentDate)) {
            NotificationManager mNM = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder dailyReminder = (NotificationCompat.Builder) new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_bulb_white_24dp)
                    .setContentTitle(mContext.getString(R.string.daily_reminder_title))
                    .setContentText(mContext.getString(R.string.daily_reminder_text))
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true);

            int lastDayOfWeek = Calendar.SATURDAY;
            if(c.getFirstDayOfWeek()  == Calendar.MONDAY) lastDayOfWeek = Calendar.SUNDAY;
            if (c.get(Calendar.DAY_OF_WEEK) == lastDayOfWeek) {
                dailyReminder.setContentText(mContext.getString(R.string.weekly_reminder_text));
            }
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(mContext, LoginActivity.class);

            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            dailyReminder.setContentIntent(resultPendingIntent);
            mNM.notify(1, dailyReminder.build());
        }

    }

}
