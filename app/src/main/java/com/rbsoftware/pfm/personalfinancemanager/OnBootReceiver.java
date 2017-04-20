package com.rbsoftware.pfm.personalfinancemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadCastReceiver for android.intent.action.BOOT_COMPLETED
 * passes all responsibility to NotificationService.
 *
 * @author Roman Burzakovskiy
 */
public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        WakefulIntentService.acquireStaticLock(context); //acquire a partial WakeLock
        context.startService(new Intent(context, NotificationService.class)); //start NotificationService
    }
}