package com.rbsoftware.pfm.personalfinancemanager.accounts;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudant.sync.datastore.ConflictException;
import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

/**
 * A simple {@link Fragment} subclass for creating and managing accounts
 *
 * @author Roman Burzakovskiy
 */
public class AccountsManagement extends Fragment {
    private final static String TAG = "AccountsManagement";
    private final int ACCOUNT_LOADER_ID = 1;

    private AlertDialog mDialog;
    private FloatingActionButton btnCreateAccount;
    private CardRecyclerView mRecyclerView;
    private ConnectionDetector mConnectionDetector;
    private AccountsCardRecyclerViewAdapter mCardArrayAdapter;
    private boolean isDialogWindowOpen;
    private OnAccountsUpdate mUpdate;

    /**
     * Interface for notification that account was created or removed
     */
    public interface OnAccountsUpdate {
        void onProfileAdded(String accountName);

        void onProfileRemoved(String accountName);
    }

    public AccountsManagement() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mUpdate = (OnAccountsUpdate) context;
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
        return inflater.inflate(R.layout.fragment_accounts_management, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[8]);
        isDialogWindowOpen = savedInstanceState != null && savedInstanceState.getBoolean("isDialogWindowOpen");
        mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.account_card_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getLoaderManager().initLoader(ACCOUNT_LOADER_ID, null, loaderCallbacks);

        if (isDialogWindowOpen) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDialogWindow(savedInstanceState);
                }
            }, 100);

        }
        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(getContext());
        }
        MainActivity.mTracker.setScreenName(TAG);

        btnCreateAccount = (FloatingActionButton) getActivity().findViewById(R.id.btn_new_account);
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogWindow(savedInstanceState);
            }
        });
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
    public void onDestroy() {
        super.onDestroy();
        btnCreateAccount.hide();
        if (isDialogWindowOpen) {
            mDialog.dismiss();
            isDialogWindowOpen = true;
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isDialogWindowOpen", isDialogWindowOpen);
        if (mDialog != null) {
            //noinspection ConstantConditions
            outState.putString("editTextAccountName", ((EditText) mDialog.findViewById(R.id.et_account_name)).getText().toString());
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Generates create account popup window
     */
    private void showDialogWindow(Bundle savedInstanceState) {

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialog = new AlertDialog.Builder(getContext())
                .setView(layoutInflater.inflate(R.layout.account_create_card_layout, null))
                .setTitle(getString(R.string.create_account))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), null)
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        final EditText editTextAccountName = (EditText) mDialog.findViewById(R.id.et_account_name);
        if (savedInstanceState != null) {
            //noinspection ConstantConditions
            editTextAccountName.setText(savedInstanceState.getString("editTextAccountName"));

        }
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressWarnings("ConstantConditions") String accountName = editTextAccountName.getText().toString();
                if (validateAccountName(accountName)) {
                    mDialog.dismiss();

                    String accountID = Utils.generateRandomString(10);
                    updateAccountDocument(accountID, accountName);
                }

            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogWindowOpen = false;
            }
        });
        isDialogWindowOpen = true;
    }

    /**
     * Generates accounts cards
     *
     * @param cards account cards
     */
    private void generateAccounts(List<AccountCard> cards) {
        for (AccountCard card : cards) {
            if (!card.getAccountId().equals(FinanceDocument.MAIN_ACCOUNT)) {
                card.getCardHeader().setPopupMenu(R.menu.banking_card_menu, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                    @Override
                    public void onMenuItemClick(final BaseCard card, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.banking_card_delete:
                                AlertDialog dialog = new AlertDialog.Builder(getContext())
                                        .setTitle(getContext().getString(R.string.delete_dialog_title))
                                        .setMessage(getContext().getString(R.string.delete_dialog_message))
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                removeAccount(((AccountCard) card).getAccountId());
                                                mCardArrayAdapter.remove((AccountCard) card);

                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));


                                break;

                        }
                    }
                });
            }
        }
        mCardArrayAdapter = new AccountsCardRecyclerViewAdapter(getActivity(), cards);
        if (mCardArrayAdapter.getItemCount() >= 3) {
            btnCreateAccount.hide();
        } else {
            btnCreateAccount.show();
        }
        mCardArrayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mCardArrayAdapter.getItemCount() >= 3) {
                    btnCreateAccount.hide();
                } else {
                    btnCreateAccount.show();
                }
            }
        });

        //Set the empty view
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mCardArrayAdapter);

        }
    }

    /**
     * Sends broadcast intent to update accounts
     */
    private void updateAccounts() {
        Intent intent = new Intent(AccountsLoader.ACTION);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private final LoaderManager.LoaderCallbacks<List<AccountCard>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<AccountCard>>() {
        @Override
        public Loader<List<AccountCard>> onCreateLoader(int id, Bundle args) {
            return new AccountsLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<AccountCard>> loader, List<AccountCard> data) {
            generateAccounts(data);
        }

        @Override
        public void onLoaderReset(Loader<List<AccountCard>> loader) {
        }
    };

    /**
     * Removes selected account
     *
     * @param id account id to remove
     */
    private void removeAccount(String id) {
        AccountDocument oldDoc = MainActivity.financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> tempMap = oldDoc.getAccountsMap();
        final String accountName = tempMap.get(id).get(0);
        tempMap.remove(id);

        try {
            MainActivity.financeDocumentModel.updateAccountDocument(oldDoc, new AccountDocument(MainActivity.getUserId(), tempMap));

            RemoveAccountDataTask task = new RemoveAccountDataTask(new RemoveAccountDataTask.AsyncResponse() {
                @Override
                public void processFinish() {
                    Toast.makeText(getContext(), getString(R.string.account_removed), Toast.LENGTH_LONG).show();
                    mUpdate.onProfileRemoved(accountName);

                }
            }, id);
            task.execute();

        } catch (ConflictException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates account document
     *
     * @param id   of account
     * @param name of account
     */
    private void updateAccountDocument(String id, String name) {
        AccountDocument oldDoc = MainActivity.financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> tempMap = oldDoc.getAccountsMap();
        List<String> newAccount = new ArrayList<>();
        newAccount.add(name);
        tempMap.put(id, newAccount);
        try {
            MainActivity.financeDocumentModel.updateAccountDocument(oldDoc, new AccountDocument(MainActivity.getUserId(), tempMap));
            updateAccounts();
            mUpdate.onProfileAdded(name);
        } catch (ConflictException e) {
            e.printStackTrace();
        }


    }

    /**
     * Validate accounts name
     * @param accountName account name
     * @return false if account is empty or duplicate
     */

    private boolean validateAccountName(String accountName) {
        if (accountName == null || accountName.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.set_value), Toast.LENGTH_LONG).show();
            return false;
        }
        AccountDocument doc = MainActivity.financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> accountsMap = doc.getAccountsMap();
        for (Map.Entry<String, List<String>> entry : accountsMap.entrySet()) {
            if (entry.getValue().get(0).toLowerCase().trim().equals(accountName.toLowerCase().trim())) {
                Toast.makeText(getContext(), getString(R.string.account_exists), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }




}
