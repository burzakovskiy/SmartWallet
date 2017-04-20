package com.rbsoftware.pfm.personalfinancemanager;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

/**
 * Performs Shared Preferences backup
 * Created by Roman Burzakovskiy on 7/28/2016.
 */
public class LegacyBackupAgent extends BackupAgentHelper {
    public static final String TAG = "LegacyBackupAgent";
    private static final String SETTINGS_FILE = "com.rbsoftware.pfm.personalfinancemanager_preferences";

    private static final String USER_DATA_BACKUP_KEY = "userData";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesBackupHelper userData = new SharedPreferencesBackupHelper(this, Utils.PREF_FILE, SETTINGS_FILE);
        addHelper(USER_DATA_BACKUP_KEY, userData);
    }
}
