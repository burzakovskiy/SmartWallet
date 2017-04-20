package com.rbsoftware.pfm.personalfinancemanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Background services that fires notification at scheduled time
 *
 * @author Roman Burzakovskiy
 */
public class NotificationService extends WakefulIntentService {
    private final static String TAG = "NotificationService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NotificationService() {

        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {


        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                1000*60*60*24,
                pendingIntent);
        super.onHandleIntent(intent);


    }


}
