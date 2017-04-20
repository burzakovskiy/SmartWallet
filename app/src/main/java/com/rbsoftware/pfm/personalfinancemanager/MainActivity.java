package com.rbsoftware.pfm.personalfinancemanager;


import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;

import android.view.ViewGroup;
import android.widget.ImageView;


import com.cloudant.sync.datastore.ConflictException;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.rbsoftware.pfm.personalfinancemanager.accounts.AccountDocument;
import com.rbsoftware.pfm.personalfinancemanager.accounts.AccountsManagement;
import com.rbsoftware.pfm.personalfinancemanager.accountsummary.AccountSummary;
import com.rbsoftware.pfm.personalfinancemanager.banking.BankIntegration;
import com.rbsoftware.pfm.personalfinancemanager.banking.BankingLoader;
import com.rbsoftware.pfm.personalfinancemanager.banking.PreFinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.billing.IabBroadcastReceiver;
import com.rbsoftware.pfm.personalfinancemanager.billing.IabHelper;
import com.rbsoftware.pfm.personalfinancemanager.billing.IabResult;
import com.rbsoftware.pfm.personalfinancemanager.billing.Inventory;
import com.rbsoftware.pfm.personalfinancemanager.billing.Purchase;
import com.rbsoftware.pfm.personalfinancemanager.budget.Budget;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryManagement;
import com.rbsoftware.pfm.personalfinancemanager.charts.Charts;
import com.rbsoftware.pfm.personalfinancemanager.charts.IncomeExpenseChart;
import com.rbsoftware.pfm.personalfinancemanager.charts.TrendsChart;
import com.rbsoftware.pfm.personalfinancemanager.goals.Goals;
import com.rbsoftware.pfm.personalfinancemanager.history.History;
import com.rbsoftware.pfm.personalfinancemanager.settings.SettingsActivity;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements
        BankIntegration.OnUpdateBankingData,
        AccountsManagement.OnAccountsUpdate,
        IabHelper.OnIabSetupFinishedListener,
        IabBroadcastReceiver.IabBroadcastListener {
    public static final int PERMISSIONS_REQUEST_READ_SMS = 1;
    private final static String TAG = "MainActivity";
    private final int BANKING_LOADER_ID = 3;
    private final int REQUEST_INVITE = 11;

    /**
     * User preferred currency
     */
    public static String defaultCurrency;
    /**
     * Google Analytics tracjer
     */
    public static Tracker mTracker;

    /**
     * Android in app billing helper
     */
    public static IabHelper billingHelper;

    /**
     * purchase update broadcast receiver
     */
    private IabBroadcastReceiver iabBroadcastReceiver;
    /**
     * unique user identifier
     */
    private static String userID;
    /**
     * current user name
     */
    private String userName = "";
    /**
     * Account header in navigation drawer
     */
    private AccountHeader drawerAccountHeader;
    /**
     * Navigation drawer
     */
    private Drawer mMaterialDrawer;
    /**
     * Finance document model instance
     */
    public static FinanceDocumentModel financeDocumentModel;
    /**
     * Bundle of prefinance documents
     */
    private Bundle mPreFinanceDocumentsBundle;
    /**
     * id of currently active account
     */
    private static String mActiveAccountId;
    /**
     * Data replicator
     */
    private static DataReplication mDataReplication;
    /**
     * internet connection detector
     */
    private ConnectionDetector mConnectionDetector;

    /**
     * Stores state of screen orientation
     */
    private int orientationState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Obtain the shared Tracker instance.
        AnalyticsTracker application = (AnalyticsTracker) getApplication();
        mTracker = application.getDefaultTracker();
        // Enable Advertising Features.
        mTracker.enableAdvertisingIdCollection(true);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Get intent userdata from login activity
        Intent intent = getIntent();
        userID = intent.getExtras().getString("id");

        userName = intent.getStringExtra("name");

        //setting current orientation state
        orientationState = getResources().getConfiguration().orientation;


        //initiating billing
        if (billingHelper == null) {
            billingHelper = new IabHelper(getApplicationContext(), Utils.buildLicenseKey());
            billingHelper.startSetup(this);
        }


        // Protect creation of static variable.
        if (financeDocumentModel == null) {
            // Model needs to stay in existence for lifetime of app.
            financeDocumentModel = new FinanceDocumentModel(getApplicationContext());
            //setup index
            financeDocumentModel.setIndexManager();
        }

        mConnectionDetector = new ConnectionDetector(this);


        //Setting up data replication and pulling data from cloudant
        if (mConnectionDetector.isConnectingToInternet() && mConnectionDetector.isWifiConnected()) {
            if (mDataReplication == null) {
                mDataReplication = new DataReplication(getApplicationContext(), financeDocumentModel);

            }
        }


        //saving app install date
        String installDate = Utils.readFromSharedPreferences(this, "appInstallDate", "");
        if (installDate == null || !installDate.isEmpty()) {
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String currentDate = df.format(c.getTime());
            Utils.saveToSharedPreferences(this, "appInstallDate", currentDate);
        }
        //Updating currency rates
        reloadCurrency();

        if (savedInstanceState == null) openFragment(1);

        //Create category document if it does not exist
        CategoryDocument categoryDocument = financeDocumentModel.getCategoryDocument(CategoryDocument.CATEGORY_DOCUMENT_ID + getUserId());
        if (categoryDocument == null) {
            financeDocumentModel.createDocument(new CategoryDocument(getUserId(), new HashMap<String, List<String>>()));
        }

        //Setting default active account
        String activeAccountNameSharedPrefs = Utils.readFromSharedPreferences(this, "mainAccountName", getString(R.string.main_wallet));
        String mainAccountNameDocument = getString(R.string.main_wallet);
        AccountDocument accountDocument = financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + getUserId());

        if (accountDocument != null) {
            HashMap<String, List<String>> accountsMap = accountDocument.getAccountsMap();
            mainAccountNameDocument = accountsMap.get(FinanceDocument.MAIN_ACCOUNT).get(0);
        }
        if (!activeAccountNameSharedPrefs.equals(getString(R.string.main_wallet)) || !mainAccountNameDocument.equals(getString(R.string.main_wallet))) {
            changeMainAccountName(getString(R.string.main_wallet));
        }
        if (savedInstanceState == null) {
            mActiveAccountId = FinanceDocument.MAIN_ACCOUNT;
        } else {
            mActiveAccountId = savedInstanceState.getString("mActiveAccountId");
        }
        setupNavigationDrawer(savedInstanceState, toolbar, intent);

        boolean firstStart = Boolean.valueOf(Utils.readFromSharedPreferences(this, "firstStart", "true"));


        if (firstStart) {
            //Start service to check for alarms
            Log.d(TAG, "NotificationService is not running. Starting..");
            WakefulIntentService.acquireStaticLock(this);
            this.startService(new Intent(this, NotificationService.class));
            firstStart = false;
            Utils.saveToSharedPreferences(this, "firstStart", Boolean.toString(firstStart));
        }


        //load banking data
        mPreFinanceDocumentsBundle = new Bundle();
        getSupportLoaderManager().initLoader(BANKING_LOADER_ID, null, loaderCallbacks);

    }


    @Override
    protected void onResume() {

        //Reading default currency from settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        defaultCurrency = sharedPreferences.getString("defaultCurrency", "USD");
        super.onResume();
    }

    @Override
    protected void onStop() {
        String mainAccountName = getString(R.string.main_wallet);

        Utils.saveToSharedPreferences(this, "mainAccountName", mainAccountName);

        //start push replication
        if (mConnectionDetector.isConnectingToInternet() && mConnectionDetector.isWifiConnected()) {
            mDataReplication.startPushReplication();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregister receiver

        if (iabBroadcastReceiver != null) {
            unregisterReceiver(iabBroadcastReceiver);
        }

        //request backup
        requestBackup();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("mActiveAccountId", getActiveAccountId());
        outState.putString("activeAccountName", drawerAccountHeader.getActiveProfile().getEmail().getText());
        //add the values which need to be saved from the drawer to the bundle
        outState.putAll(mMaterialDrawer.saveInstanceState(outState));
        //add the values which need to be saved from the accountHeader to the bundle
        outState.putAll(drawerAccountHeader.saveInstanceState(outState));

        //saving current orientation state
        outState.putInt("screenOrientation", orientationState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_INVITE) {
                if (resultCode == RESULT_OK) {
                    // Get the invitation IDs of all sent messages
                    String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                    for (String id : ids) {
                        Log.d(TAG, "onActivityResult: sent invitation " + id);
                    }
                }
            }
        }
    }


    //HELPER METHODS

    /**
     * Creates navigation drawer
     *
     * @param savedInstanceState of activity
     * @param toolbar            of activity
     * @param intent             received after login
     */
    private void setupNavigationDrawer(final Bundle savedInstanceState, Toolbar toolbar, Intent intent) {
        AccountDocument accountDocument = financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + getUserId());
        if (accountDocument == null) {
            HashMap<String, List<String>> values = new HashMap<>();
            List<String> mainAccount = new ArrayList<>();
            mainAccount.add(getString(R.string.main_wallet));
            values.put(FinanceDocument.MAIN_ACCOUNT, mainAccount);
            accountDocument = new AccountDocument(MainActivity.getUserId(), values);
            MainActivity.financeDocumentModel.createDocument(accountDocument);

        }
        final HashMap<String, List<String>> accountsMap = accountDocument.getAccountsMap();
        ArrayList<ProfileDrawerItem> profiles = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : accountsMap.entrySet()) {
            ProfileDrawerItem profileDrawerItem;
            if (intent.getStringExtra("photoURL") != null) {
                profileDrawerItem = new ProfileDrawerItem()
                        .withName(intent.getStringExtra("name"))
                        .withEmail(entry.getValue().get(0))
                        .withIcon(intent.getStringExtra("photoURL"));
            } else {
                profileDrawerItem = new ProfileDrawerItem()
                        .withName(intent.getStringExtra("name"))
                        .withEmail(entry.getValue().get(0));
            }
            profiles.add(profileDrawerItem);
        }
        //Set up image loading through Picasso
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                if (uri != null) {
                    Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
                }
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }


        });
        // Create the AccountHeader
        drawerAccountHeader = new AccountHeaderBuilder()
                .withActivity(this)

                .withHeaderBackground(R.drawable.account_header_background)
                .addProfiles(
                        profiles.toArray(new ProfileDrawerItem[profiles.size()])
                )
                .withOnlyMainProfileImageVisible(true)
                .withProfileImagesClickable(false)
                .withSavedInstance(savedInstanceState)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        for (Map.Entry<String, List<String>> entry : getAccountsMap().entrySet()) {
                            if (entry.getValue().get(0).equals(profile.getEmail().toString())) {
                                mActiveAccountId = entry.getKey();
                                break;
                            }
                        }
                        //checking if orientation has been changed
                        //if not reload data
                        int prevState = orientationState;
                        if (savedInstanceState != null) {
                            prevState = savedInstanceState.getInt("screenOrientation");
                            savedInstanceState.putInt("screenOrientation", getResources().getConfiguration().orientation);
                        }
                        if (!Utils.isOrientationChanged(getApplicationContext(), prevState)) {
                            reloadFragmentData();
                        }
                        return false;
                    }
                })
                .build();

        //Setting default account
        for (IProfile profile : profiles) {

            String accountName = getString(R.string.main_wallet);
            if (savedInstanceState != null) {
                accountName = savedInstanceState.getString("activeAccountName");
            }

            if (profile.getEmail().getText().equals(accountName)) {
                drawerAccountHeader.setActiveProfile(profile, true);
            }
        }

        //Build navigation drawer
        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerAccountHeader)
                .withDelayDrawerClickEvent(0)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[0]).withIcon(GoogleMaterial.Icon.gmd_dashboard),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[1]).withIcon(GoogleMaterial.Icon.gmd_book),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[2]).withIcon(GoogleMaterial.Icon.gmd_monetization_on),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[3]).withIcon(GoogleMaterial.Icon.gmd_pie_chart),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[4]).withIcon(GoogleMaterial.Icon.gmd_credit_card),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[5]).withIcon(GoogleMaterial.Icon.gmd_history),

                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[6]).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_star),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[7]).withIcon(GoogleMaterial.Icon.gmd_add_circle),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[8]).withIcon(GoogleMaterial.Icon.gmd_account_balance_wallet),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[9]).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_settings),
                        new PrimaryDrawerItem().withName(getResources().getStringArray(R.array.drawer_menu)[10]).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
                )

                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (position == 11) {
                            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivityForResult(i, MainActivity.RESULT_OK);
                            return true;
                        } else if (position == 8) {
                            recommendToFriend();
                        } else if (position == 12) {
                            signout();
                            return true;

                        } else {
                            openFragment(position);
                        }
                        return false;
                    }
                })

                .withSavedInstance(savedInstanceState);

        //make multipane layout for tablets in landscape orientation
        if (Utils.isTablet(this) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            drawerBuilder.withTranslucentStatusBar(false).withTranslucentNavigationBar(false);

            mMaterialDrawer = drawerBuilder.buildView();
            ((ViewGroup) findViewById(R.id.nav_tablet)).addView(mMaterialDrawer.getSlider());
        } else {
            mMaterialDrawer = drawerBuilder.build();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                mMaterialDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
            }
        }

    }

    /**
     * Opens fragment
     *
     * @param position fragment position
     */
    private void openFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 1:
                if (fragmentManager.findFragmentByTag("AccountSummary") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("AccountSummary")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new AccountSummary(), "AccountSummary").commit();
                }

                break;
            case 2:
                if (fragmentManager.findFragmentByTag("Budget") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Budget")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new Budget(), "Budget").commit();
                }
                break;
            case 3:
                if (fragmentManager.findFragmentByTag("Goals") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Goals")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new Goals(), "Goals").commit();
                }
                break;
            case 4:
                if (fragmentManager.findFragmentByTag("Charts") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("Charts")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new Charts(), "Charts").commit();
                }
                break;
            case 5:
                if (fragmentManager.findFragmentByTag("BankIntegration") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("BankIntegration")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    BankIntegration bankIntegration = new BankIntegration();
                    if (mPreFinanceDocumentsBundle != null) {
                        bankIntegration.setArguments(mPreFinanceDocumentsBundle);
                    }
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, bankIntegration, "BankIntegration").commit();
                }
                break;
            case 6:
                if (fragmentManager.findFragmentByTag("History") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("History")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new History(), "History").commit();
                }
                break;


            case 9:
                if (fragmentManager.findFragmentByTag("CategoryManagement") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("CategoryManagement")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new CategoryManagement(), "CategoryManagement").commit();
                }
                break;

            case 10:
                if (fragmentManager.findFragmentByTag("AccountsManagement") != null) {
                    //if the fragment exists, show it.
                    fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("AccountsManagement")).commit();
                } else {
                    //if the fragment does not exist, add it to fragment manager.
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, new AccountsManagement(), "AccountsManagement").commit();
                }
                break;


        }


    }

    /**
     * Changes main account name in case of locale change
     *
     * @param name of main account
     */
    private void changeMainAccountName(String name) {
        AccountDocument accountDocument = financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + getUserId());
        HashMap<String, List<String>> accountsMap = accountDocument.getAccountsMap();
        accountsMap.get(FinanceDocument.MAIN_ACCOUNT).set(0, name);
        try {
            financeDocumentModel.updateAccountDocument(accountDocument, new AccountDocument(MainActivity.getUserId(), accountsMap));
        } catch (ConflictException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initiates loader to update fragment data
     */
    private void reloadFragmentData() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("AccountSummary") != null)
            ((AccountSummary) fragmentManager.findFragmentByTag("AccountSummary")).updateCards();
        else if (fragmentManager.findFragmentByTag("Budget") != null)
            ((Budget) fragmentManager.findFragmentByTag("Budget")).updateBudget();
        else if (fragmentManager.findFragmentByTag("Goals") != null)
            ((Goals) fragmentManager.findFragmentByTag("Goals")).updateGoals();
        else if (fragmentManager.findFragmentByTag("History") != null)
            ((History) fragmentManager.findFragmentByTag("History")).updateHistory();
        else if (fragmentManager.findFragmentByTag("BankIntegration") != null)
            ((BankIntegration) fragmentManager.findFragmentByTag("BankIntegration")).updateBankingCard();
        else if (fragmentManager.findFragmentByTag("Charts") != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Charts charts = ((Charts) fragmentManager.findFragmentByTag("Charts"));
                    Charts.CollectionPagerAdapter adapter = (charts.getAdapter());
                    Fragment fragment = adapter.getRegisteredFragment(charts.getPager().getCurrentItem());
                    if (fragment instanceof IncomeExpenseChart)
                        ((IncomeExpenseChart) fragment).updateChart();
                    if (fragment instanceof TrendsChart)
                        ((TrendsChart) fragment).updateChart();
                }
            }, 500);

        }


    }

    /**
     * Sign out from application
     */
    private void signout() {
        //unbind billing helper
        if (billingHelper != null) {
            billingHelper.disposeWhenFinished();

            billingHelper = null;
        }

        if (LoginActivity.mGoogleApiClient.isConnected()) {
            finishSignout();
        } else {
            LoginActivity.mGoogleApiClient.connect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishSignout();
                }
            }, 1000);


        }


    }

    /**
     * Completes signout
     */
    private void finishSignout() {
        Auth.GoogleSignInApi.signOut(LoginActivity.mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        LoginActivity.mGoogleApiClient.disconnect();

                        //removing user credentials from shared preferences;
                        Utils.saveToSharedPreferences(MainActivity.this, "userName", "");
                        Utils.saveToSharedPreferences(MainActivity.this, "userId", "");
                        Utils.saveToSharedPreferences(MainActivity.this, "userEmail", "");
                        Utils.saveToSharedPreferences(MainActivity.this, "userPhoto", "");

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }


    /**
     * Getter userId
     *
     * @return uesrId
     */
    public static String getUserId() {
        return userID;
    }

    /**
     * Getter  active account
     *
     * @return active account
     */
    public static String getActiveAccountId() {
        return mActiveAccountId;
    }


    /**
     * Sends message to invite another user
     */
    private void recommendToFriend() {


        if (mConnectionDetector.isConnectingToInternet()) {
            mTracker.setScreenName(TAG);
            mTracker.send(new HitBuilders.EventBuilder().setCategory("Action").setAction("Refer").build());
        }
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.download_smart_wallet))
                .setMessage(getString(R.string.refer_text))
                .setDeepLink(Uri.parse("https://v9dw5.app.goo.gl/b2OT"))
                .setCustomImage(Uri.parse("http://smartwallet.mobi/assets/phone.png"))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }


    /**
     * Fetches last currency rates from internet
     */
    private void reloadCurrency() {
        ConnectionDetector mConnectionDetector = new ConnectionDetector(getApplicationContext());
        if (mConnectionDetector.isConnectingToInternet()) {
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String currentDate = df.format(c.getTime());
            String updatedDate = Utils.readFromSharedPreferences(this, "updatedDate", "");
            if (!updatedDate.equals(currentDate) || (financeDocumentModel.getCurrencyDocument(Currency.CURRENCY_ID) == null)) {
                Log.d(TAG, "Updating currency rates");
                new CurrencyConversion(this).execute();


            } else {
                Log.d(TAG, "Currency rates were updated today");
            }
        } else {
            Log.e(TAG, "Can't update currency rates. No internet connection");
        }
    }


    /**
     * Sends broadcast intent to update banking data
     */
    private void updateBanking() {
        Intent intent = new Intent(BankingLoader.ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * Set badge to bank integration drawer menu item and sets arguments for BankIntegration fragment
     *
     * @param docList list of preFinanceDocuments
     */
    private void createBankIntegrationBadge(List<PreFinanceDocument> docList) {
        if (docList != null && !docList.isEmpty()) {
            ArrayList<String> preFinanceDocumentStringList = new ArrayList<>();
            PrimaryDrawerItem item = new PrimaryDrawerItem()
                    .withName(getResources().getStringArray(R.array.drawer_menu)[4])
                    .withIcon(GoogleMaterial.Icon.gmd_credit_card)
                    .withBadge(docList.size() + "")
                    .withBadgeStyle(new BadgeStyle()
                            .withTextColor(Color.WHITE)
                            .withColorRes(R.color.primary_dark));

            mMaterialDrawer.updateItemAtPosition(item, 5);
            for (PreFinanceDocument doc : docList) {
                preFinanceDocumentStringList.add(doc.getDate() + "--" + doc.getValue() + "--" + doc.getCurrency() + "--" + doc.getDescription());
            }
            mPreFinanceDocumentsBundle.putStringArrayList("preFinanceDocumentStringList", preFinanceDocumentStringList);
        } else {
            ArrayList<String> preFinanceDocumentStringList = new ArrayList<>();
            PrimaryDrawerItem item = new PrimaryDrawerItem()
                    .withName(getResources().getStringArray(R.array.drawer_menu)[4])
                    .withIcon(GoogleMaterial.Icon.gmd_credit_card);

            mMaterialDrawer.updateItemAtPosition(item, 5);
            mPreFinanceDocumentsBundle.putStringArrayList("preFinanceDocumentStringList", preFinanceDocumentStringList);
        }

    }

    private final LoaderManager.LoaderCallbacks<List<PreFinanceDocument>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<PreFinanceDocument>>() {
        @Override
        public Loader<List<PreFinanceDocument>> onCreateLoader(int id, Bundle args) {
            return new BankingLoader(getApplicationContext(), MainActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<PreFinanceDocument>> loader, List<PreFinanceDocument> data) {
            createBankIntegrationBadge(data);
        }

        @Override
        public void onLoaderReset(Loader<List<PreFinanceDocument>> loader) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    updateBanking();

                }
            }


        }
    }


    /**
     * BankIntegration interface callback
     */
    @Override
    public void onUpdateData() {
        updateBanking();
    }


    /**
     * Gets map of accounts
     *
     * @return accounts hash map
     */
    private HashMap<String, List<String>> getAccountsMap() {
        final AccountDocument accountDocument = financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + getUserId());
        return accountDocument.getAccountsMap();
    }

    /**
     * AccountsManagement callback
     *
     * @param accountName new account name
     */
    @Override
    public void onProfileAdded(String accountName) {
        ProfileDrawerItem profile = new ProfileDrawerItem().withName(userName).withEmail(accountName);
        drawerAccountHeader.addProfile(profile, drawerAccountHeader.getProfiles().size());
    }

    /**
     * AccountsManagement callback
     *
     * @param accountName account to be removed
     */
    @Override
    public void onProfileRemoved(final String accountName) {
        List<IProfile> profileList = drawerAccountHeader.getProfiles();
        IProfile currentAccount = drawerAccountHeader.getActiveProfile();


        if (currentAccount.getEmail().getText().equals(accountName)) {
            IProfile mainAccount;
            for (IProfile profile : profileList) {
                if (profile.getEmail().getText().equals(getString(R.string.main_wallet))) {
                    mainAccount = profile;
                    drawerAccountHeader.setActiveProfile(mainAccount, true);
                    drawerAccountHeader.removeProfile(currentAccount);
                    return;
                }
            }


        } else {
            for (IProfile profile : profileList) {
                if (profile.getEmail().getText().equals(accountName)) {
                    drawerAccountHeader.removeProfile(profile);
                    return;
                }
            }
        }


    }

    /**
     * billing setup finished listener
     *
     * @param result The result of the setup process.
     */
    @Override
    public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
            // Oh noes, there was a problem.
            Log.e(TAG, "Problem setting up in-app billing: " + result);
            return;
        }

        // Have we been disposed of in the meantime? If so, quit.
        if (billingHelper == null) return;

        //registering purchase updated receiver
        iabBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(iabBroadcastReceiver, broadcastFilter);

        try {
            billingHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.e(TAG, "Error querying inventory. Another async operation in progress.");
        }
    }

    @Override
    public void receivedBroadcast() {
        try {
            billingHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.e(TAG, "Error querying inventory. Another async operation in progress.");
        }
    }

    /**
     * Listener that's called when we finish querying the items and subscriptions we own
     */
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (billingHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Log.e(TAG, "Failed to query inventory: " + result);
                return;
            }

            Purchase extraCategoryPurchase = inventory.getPurchase(Inventory.SKU_EXTRA_CATEGORY);
            if (extraCategoryPurchase != null && Utils.verifyDeveloperPayload(extraCategoryPurchase)) {
                try {
                    billingHelper.consumeAsync(inventory.getPurchase(Inventory.SKU_EXTRA_CATEGORY), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error consuming product. Another async operation in progress.");
                }
            }

        }
    };


    /**
     * Called when consumption is complete
     */
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            // if we were disposed of in the meantime, quit.
            if (MainActivity.billingHelper == null) return;


            if (result.isSuccess()) {
                if (purchase.getSku().equals(Inventory.SKU_EXTRA_CATEGORY)) {


                    String numberOfExtraCategoriesString = Utils.readFromSharedPreferences(getApplicationContext(), "numberOfExtraCategories", "0");
                    //decrypting string
                    if (!numberOfExtraCategoriesString.equals("0") || !Utils.isNumber(numberOfExtraCategoriesString))
                        numberOfExtraCategoriesString = Utils.decrypt(numberOfExtraCategoriesString);
                    int numberOfExtraCategories = Integer.valueOf(numberOfExtraCategoriesString);
                    numberOfExtraCategories += 1;

                    //encrypting string
                    numberOfExtraCategoriesString = Utils.encrypt(String.valueOf(numberOfExtraCategories));
                    Utils.saveToSharedPreferences(getApplicationContext(), "numberOfExtraCategories", numberOfExtraCategoriesString);
                }
            } else {
                Log.e(TAG, "Error while consuming: " + result);
            }
        }
    };


    /**
     * Request data backup for devices with API <23
     */
    private void requestBackup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            BackupManager bm = new BackupManager(this);
            bm.dataChanged();
        }
    }


}
