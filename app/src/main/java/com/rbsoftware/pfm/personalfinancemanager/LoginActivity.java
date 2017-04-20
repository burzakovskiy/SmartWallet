package com.rbsoftware.pfm.personalfinancemanager;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;


public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;
    public static GoogleApiClient mGoogleApiClient;

    // Connection detector class
    private ConnectionDetector mConnectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this /* OnConnectionFailedListener */)

                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        if (signInButton != null) {
            signInButton.setOnClickListener(this);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            signInButton.setScopes(gso.getScopeArray());
            signInButton.setVisibility(View.GONE);
        }
        // [END customize_button]
        mConnectionDetector = new ConnectionDetector(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.


            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleResult(googleSignInResult);
                }
            });
        }

    }


    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleResult(result);
            } else {
                signInButton.setVisibility(View.VISIBLE);
            }


            if (!mGoogleApiClient.isConnecting()) {

                mGoogleApiClient.connect();
            }
        }
    }
    // [END onActivityResult]


    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    // [END signIn]


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        signInButton.setVisibility(View.VISIBLE);
        mGoogleApiClient.reconnect();


    }


    @Override
    public void onClick(View v) {

        if (mConnectionDetector.isConnectingToInternet()) {
            signIn();
        } else {
            showNoNetworkDialog();
        }

    }

    /**
     * Shows alert dialog if no network connection
     */
    private void showNoNetworkDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(getString(R.string.no_network_title));
        alertDialog.setMessage(getString(R.string.no_network_message));
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    /**
     * Fetches profile data and starts MainActivity
     */
    private void handleResult(GoogleSignInResult result) {

        if (result != null && result.isSuccess()) {
            signInButton.setVisibility(View.GONE);
            if (Boolean.valueOf(Utils.readFromSharedPreferences(this, "firstStart", "true"))) {
                showSelectCurrencyDialog(result.getSignInAccount());
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String password = sharedPreferences.getString("password", null);
                if (password != null && !password.isEmpty()) {
                    showPasswordDialog(result.getSignInAccount(), password);
                } else {
                    finishSignIn(result.getSignInAccount());
                }

            }
        } else if (result == null || !result.isSuccess()) {
            //check data from shared preferences
            String userId = Utils.readFromSharedPreferences(this, "userId", null);
            if (userId != null && !userId.isEmpty()) {
                signInButton.setVisibility(View.GONE);
                if (Boolean.valueOf(Utils.readFromSharedPreferences(this, "firstStart", "true"))) {
                    showSelectCurrencyDialog(null);
                } else {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    String password = sharedPreferences.getString("password", null);
                    if (password != null && !password.isEmpty()) {
                        showPasswordDialog(null, password);
                    } else {
                        finishSignIn(null);
                    }

                }
            } else {
                signInButton.setVisibility(View.VISIBLE);
            }

        }


    }

    /**
     * Starts MainActivity and passes user's data there
     */
    private void finishSignIn(GoogleSignInAccount acct) {
        Intent intent = new Intent(this, MainActivity.class);
        if (acct != null) {
            intent.putExtra("name", acct.getDisplayName());
            Utils.saveToSharedPreferences(this, "userName", acct.getDisplayName());

            intent.putExtra("id", acct.getId());
            Utils.saveToSharedPreferences(this, "userId", acct.getId());

            intent.putExtra("email", acct.getEmail());
            Utils.saveToSharedPreferences(this, "userEmail", acct.getEmail());

            if (acct.getPhotoUrl() != null) {
                intent.putExtra("photoURL", acct.getPhotoUrl().toString());
                //saving user photo to shared prefs
                Utils.saveToSharedPreferences(this, "userPhoto", acct.getPhotoUrl().toString());
            }
        } else {

            //reading data from shared preferences and passing it into intent
            String userId = Utils.readFromSharedPreferences(this, "userId", null);
            intent.putExtra("id", userId);

            String userName = Utils.readFromSharedPreferences(this, "userName", null);
            intent.putExtra("name", userName);

            String userEmail = Utils.readFromSharedPreferences(this, "userEmail", null);
            intent.putExtra("email", userEmail);

            String userPhoto = Utils.readFromSharedPreferences(this, "userPhoto", null);
            if (userPhoto != null && !userPhoto.isEmpty()) {
                intent.putExtra("photoURL", userPhoto);
            }

        }

        startActivity(intent);
        finish();
    }

    /**
     * Shows dialog for default currency selection
     */
    private void showSelectCurrencyDialog(final GoogleSignInAccount acct) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.default_currency))
                .setCancelable(false)
                .setSingleChoiceItems(
                        new ArrayAdapter<>(this,
                                R.layout.select_default_currency_list_item,
                                getResources().getStringArray(R.array.report_activity_currency_spinner)),
                        0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("defaultCurrency", getResources().getStringArray(R.array.report_activity_currency_spinner)[which]);
                                editor.apply();
                                dialog.dismiss();
                                finishSignIn(acct);

                            }
                        }
                )
                .show();
    }

    private void showPasswordDialog(final GoogleSignInAccount acct, final String password) {
        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lparams.setMargins(Utils.dpToPx(this, 8), 0, Utils.dpToPx(this, 8), 0);
        final EditText editText = new EditText(this);
        int maxLength = 10;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(fArray);
        editText.setHint(getString(R.string.enter_password));
        editText.setLayoutParams(lparams);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.requestFocus();


        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.password))
                .setCancelable(false)
                .setView(editText)
                .setPositiveButton(getString(R.string.ok), null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editText.getText().toString().equals(password)) {
                    dialog.dismiss();
                    finishSignIn(acct);
                } else {
                    editText.requestFocus();
                    editText.setError(getString(R.string.wrong_password));
                }
            }
        });
    }


}