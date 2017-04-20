package com.rbsoftware.pfm.personalfinancemanager.history;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cloudant.sync.datastore.ConflictException;
import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.EditDocument;
import com.rbsoftware.pfm.personalfinancemanager.ExportData;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.accounts.AccountDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


/**
 * A simple {@link Fragment} subclass.
 * Holds user's history
 *
 * @author Roman Burzakovskiy
 */
public class History extends Fragment implements CardHeader.OnClickCardHeaderPopupMenuListener, SearchView.OnQueryTextListener {
    private final String TAG = "History";
    private final int HISTORY_LOADER_ID = 2;

    private CardRecyclerView mRecyclerView;
    private HistoryCardRecyclerViewAdapter mCardArrayAdapter;
    private HistoryCard card;
    private List<HistoryCard> mCards;
    private TextView mEmptyView;
    private ConnectionDetector mConnectionDetector;
    private String mQueryText;
    private SearchView mSearchView;


    public History() {
        // Required empty public constructor
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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[5]);

        mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.history_card_recycler_view);
        mEmptyView = (TextView) getActivity().findViewById(R.id.emptyHistory);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Context mContext = getContext();
        getLoaderManager().initLoader(HISTORY_LOADER_ID, null, loaderCallbacks);
        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(mContext);
        }
        MainActivity.mTracker.setScreenName(TAG);

        if (savedInstanceState != null) {
            mQueryText = savedInstanceState.getString("searchQuery");
        }
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!mSearchView.getQuery().toString().trim().isEmpty()) {
            outState.putString("searchQuery", mSearchView.getQuery().toString());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setOnQueryTextListener(this);
        if (mQueryText != null) {
            menu.findItem(R.id.search).expandActionView();
            mSearchView.setQuery(mQueryText, false);
        }

        int status = getContext().getSharedPreferences("material_showcaseview_prefs", Context.MODE_PRIVATE)
                .getInt("status_" + TAG, 0);
        if (status != -1) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startShowcase();
                }
            }, 1000);
        }

        super.onCreateOptionsMenu(menu, inflater);


    }

    private void generateHistory(List<HistoryCard> cards) {
        mCards = new ArrayList<>();
        mCards.addAll(cards);
        for (HistoryCard historyCard : cards) {
            historyCard.getCardHeader().setPopupMenu(R.menu.history_card_menu, this);
        }

        mCardArrayAdapter = new HistoryCardRecyclerViewAdapter(getActivity(), cards);
        //Staggered grid view
        mCardArrayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });

        //Set the empty view
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mCardArrayAdapter);
            checkAdapterIsEmpty();


        }

    }

    /**
     * Checks whether recycler view is empty
     * And switches to empty view
     */
    private void checkAdapterIsEmpty() {
        if (mCardArrayAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                SparseArray<Object> defaultParams = new SparseArray<>();
                ArrayList<String> editResult = data.getStringArrayListExtra("editDefaultCategoriesResult");
                String oldDocId = data.getStringExtra("oldDocId");
                defaultParams.append(FinanceDocument.PARAM_USERID, MainActivity.getUserId());
                defaultParams.append(FinanceDocument.PARAM_SALARY, Utils.getItem(editResult, 0));
                defaultParams.append(FinanceDocument.PARAM_RENTAL_INCOME, Utils.getItem(editResult, 1));
                defaultParams.append(FinanceDocument.PARAM_INTEREST, Utils.getItem(editResult, 2));
                defaultParams.append(FinanceDocument.PARAM_GIFTS, Utils.getItem(editResult, 3));
                defaultParams.append(FinanceDocument.PARAM_OTHER_INCOME, Utils.getItem(editResult, 4));
                defaultParams.append(FinanceDocument.PARAM_TAXES, Utils.getItem(editResult, 5));
                defaultParams.append(FinanceDocument.PARAM_MORTGAGE, Utils.getItem(editResult, 6));
                defaultParams.append(FinanceDocument.PARAM_CREDIT_CARD, Utils.getItem(editResult, 7));
                defaultParams.append(FinanceDocument.PARAM_UTILITIES, Utils.getItem(editResult, 8));
                defaultParams.append(FinanceDocument.PARAM_FOOD, Utils.getItem(editResult, 9));
                defaultParams.append(FinanceDocument.PARAM_CAR_PAYMENT, Utils.getItem(editResult, 10));
                defaultParams.append(FinanceDocument.PARAM_PERSONAL, Utils.getItem(editResult, 11));
                defaultParams.append(FinanceDocument.PARAM_ACTIVITIES, Utils.getItem(editResult, 12));
                defaultParams.append(FinanceDocument.PARAM_OTHER_EXPENSE, Utils.getItem(editResult, 13));

                //getting cusomt params from intent
                HashMap<String, List<String>> customParams = (HashMap<String, List<String>>) data.getSerializableExtra("editCustomCategoriesResult");
                try {
                    MainActivity.financeDocumentModel.updateFinanceDocument(
                            MainActivity.financeDocumentModel.getFinanceDocument(oldDocId),
                            new FinanceDocument(defaultParams, customParams, data.getStringExtra("documentDate"), data.getStringExtra("comments")));
                } catch (ConflictException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends broadcast intent to update history
     */
    public void updateHistory() {
        Intent intent = new Intent(HistoryLoader.ACTION);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }


    private final LoaderManager.LoaderCallbacks<List<HistoryCard>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<HistoryCard>>() {
        @Override
        public Loader<List<HistoryCard>> onCreateLoader(int id, Bundle args) {
            return new HistoryLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<HistoryCard>> loader, List<HistoryCard> data) {
            generateHistory(data);
        }

        @Override
        public void onLoaderReset(Loader<List<HistoryCard>> loader) {
        }
    };

    @Override
    public void onMenuItemClick(final BaseCard card, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.history_edit:
                Intent edit = new Intent(getActivity(), EditDocument.class);
                edit.putExtra("docId", ((HistoryCard) card).getDocument().getDocumentRevision().getId());
                startActivityForResult(edit, 2);

                return;

            case R.id.history_delete:

                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle(getContext().getString(R.string.delete_dialog_title))
                        .setMessage(getContext().getString(R.string.delete_dialog_message))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    MainActivity.financeDocumentModel.deleteDocument(((HistoryCard) card).getDocument());
                                    mCardArrayAdapter.remove((HistoryCard) card);
                                } catch (ConflictException e) {
                                    e.printStackTrace();
                                }
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


                return;
            case R.id.history_transfer:
                showTransferDialog((HistoryCard) card);
                return;

            case R.id.history_share:

                try {
                    ExportData.exportHistoryAsCsv(getContext(), ((HistoryCard) card).getDocument());
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mCards != null && mCardArrayAdapter != null) {
            List<HistoryCard> filteredModelList = filter(mCards, newText.trim());
            mCardArrayAdapter.setCards(filteredModelList);
            mCardArrayAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Search filter
     *
     * @param cards list of cards
     * @param query parameter
     * @return filtered cards list
     */
    private List<HistoryCard> filter(List<HistoryCard> cards, String query) {

        final List<HistoryCard> filteredCardList = new ArrayList<>();
        for (HistoryCard card : cards) {
            FinanceDocument doc = card.getDocument();
            HashMap<String, List<String>> map = doc.getValuesMap();
            if (!Utils.isNumber(query)) {
                String date = DateUtils.getNormalDate(DateUtils.DATE_FORMAT_FULL, doc.getDate()).toLowerCase();
                if (date.contains(query.toLowerCase())) {
                    filteredCardList.add(card);
                } else if (doc.getComments().toLowerCase().contains(query.toLowerCase())) {
                    filteredCardList.add(card);
                } else {
                    for (String key : map.keySet()) {
                        String text = Utils.keyToString(getContext(), key).toLowerCase();
                        if (text.contains(query.toLowerCase())) {
                            filteredCardList.add(card);
                            break;
                        }
                    }
                }
            } else {
                if (query.equals(String.valueOf(doc.getTotalIncome())) || query.equals(String.valueOf(doc.getTotalExpense()))) {
                    filteredCardList.add(card);

                } else {
                    for (List<String> value : map.values()) {
                        String text = value.get(0);
                        if (query.equals(text)) {
                            filteredCardList.add(card);
                            break;
                        }
                    }
                }
            }

        }
        return filteredCardList;
    }

    /**
     * Shows operation transfer dialog
     *
     * @param card history card
     */
    private void showTransferDialog(final HistoryCard card) {
        AccountDocument doc = MainActivity.financeDocumentModel.getAccountDocument(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());
        final HashMap<String, List<String>> accountsMap = doc.getAccountsMap();
        final ArrayList<String> accountList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : accountsMap.entrySet()) {
            if (!entry.getKey().equals(MainActivity.getActiveAccountId())) {
                accountList.add(entry.getValue().get(0));
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.select_account))
                .setSingleChoiceItems(
                        new ArrayAdapter<>(getContext(),
                                R.layout.select_default_currency_list_item,
                                accountList),
                        0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newAccount = FinanceDocument.MAIN_ACCOUNT;
                                for (Map.Entry<String, List<String>> entry : accountsMap.entrySet()) {
                                    if (entry.getValue().get(0).equals(accountList.get(which))) {
                                        newAccount = entry.getKey();
                                    }
                                }
                                FinanceDocument newDoc = card.getDocument();
                                newDoc.setAccount(newAccount);

                                try {
                                    MainActivity.financeDocumentModel.updateFinanceDocument(card.getDocument(), newDoc);
                                    mCardArrayAdapter.remove(card);
                                } catch (ConflictException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();

                            }
                        })

                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        if (accountList.isEmpty()) {
            dialog.setMessage(getString(R.string.no_accounts));
        }
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    /**
     * Runs showcase presentation on fragment start
     **/
    private void startShowcase() {
        if (getActivity().findViewById(R.id.search) != null) {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view
            config.setDismissTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), TAG);
            sequence.setConfig(config);
            sequence.addSequenceItem(getActivity().findViewById(R.id.search), getString(R.string.search_history), getString(R.string.ok));
            sequence.start();
        }
    }
}
