package com.rbsoftware.pfm.personalfinancemanager.banking;


import android.app.DatePickerDialog;
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
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudant.sync.datastore.ConflictException;
import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Roman Burzakovskiy
 */
@SuppressWarnings("ConstantConditions")
public class BankIntegration extends Fragment {

    private final static String TAG = "BankIntegration";
    private final int BANKING_CARD_LOADER_ID = 3;

    /**
     * Banking card recycler view
     */
    private CardRecyclerView mRecyclerView;
    /**
     * Banking cards array adapter
     */
    private BankingCardRecyclerViewAdapter mCardArrayAdapter;
    /**
     * Text view for empty adapter
     */
    private TextView mEmptyView;
    /**
     * New bank integration FAB
     */
    private FloatingActionButton btnNewBankIntegration;
    /**
     * Internet connection detector
     */
    private ConnectionDetector mConnectionDetector;
    /**
     * banking data update listener
     */
    private OnUpdateBankingData mUpdateBankingData;
    /**
     * Create prefinance document dialog
     */
    private AlertDialog dialog;
    /**
     * transaction calendar date
     */
    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedDay = 0;
    /**
     * View to display transaction date
     */
    private TextView mDateTextView;
    /**
     * Comment dialog window
     */
    public AlertDialog commentDialog;
    /**
     * Comment to document
     */
    private String mComment = "";
    /**
     * Check if comment is set
     */
    private ImageView isCommentSetImageView;

    /**
     * true if dialog is opened
     */
    public boolean isCommentDialogWindowOpen;
    /**
     * Pre finance document counter
     */
    private int preFinanceDocumentPosition = 0;
    /**
     * Description to category map
     * key- description
     * value- category position
     */
    private HashMap<String, Integer> mDescriptionToCategoryMap;

    /**
     * Map of custom categories
     */
    private HashMap<String, List<String>> mCustomCategoriesMap;
    private int customIncomeCategoriesNumber = 0;// number of custom income categories
    private int customExpenseCategoriesNumber = 0;// number of custom expense categories

    /**
     * Banking data update interface
     */
    public interface OnUpdateBankingData {
        /**
         * update listener
         */
        void onUpdateData();
    }

    public BankIntegration() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //attach listener to fragment
        mUpdateBankingData = (OnUpdateBankingData) context;
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
        return inflater.inflate(R.layout.fragment_bank_integration, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[4]);

        //reading custom categories data
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        mCustomCategoriesMap = categoryDocument.getCategoriesMap();
        for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                customIncomeCategoriesNumber++;
            } else {
                customExpenseCategoriesNumber++;
            }
        }


        mRecyclerView = (CardRecyclerView) getActivity().findViewById(R.id.banking_card_recycler_view);
        mEmptyView = (TextView) getActivity().findViewById(R.id.empty_banking_card);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getLoaderManager().initLoader(BANKING_CARD_LOADER_ID, null, loaderCallbacks);

        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(getContext());
        }
        MainActivity.mTracker.setScreenName(TAG);
        btnNewBankIntegration = (FloatingActionButton) getActivity().findViewById(R.id.btn_new_bank_integration);

        btnNewBankIntegration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NewBankIntegrationStepper(), "NewBankIntegrationStepper").commit();
            }
        });
        if (getArguments() != null) {
            DescriptionToCategoryDocument descriptionToCategoryDocument =
                    MainActivity.financeDocumentModel
                            .getDescriptionToCategoryDocument(DescriptionToCategoryDocument.DESCRIPTION_TO_CATEGORY_ID + MainActivity.getUserId());
            if (descriptionToCategoryDocument == null) {
                mDescriptionToCategoryMap = new HashMap<>();
            } else {
                mDescriptionToCategoryMap = descriptionToCategoryDocument.getDescriptionToCategoryMap();
            }
            final ArrayList<String> preFinanceDocumentsList = getArguments().getStringArrayList("preFinanceDocumentStringList");
            if (preFinanceDocumentsList != null && !preFinanceDocumentsList.isEmpty()) {
                Collections.reverse(preFinanceDocumentsList);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCreateDocumentDialog(savedInstanceState, preFinanceDocumentsList);
                    }
                }, 500);

            }
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
    public void onStop() {
        super.onStop();
        if (getArguments() != null) {
            DescriptionToCategoryDocument descriptionToCategoryDocument =
                    MainActivity.financeDocumentModel
                            .getDescriptionToCategoryDocument(DescriptionToCategoryDocument.DESCRIPTION_TO_CATEGORY_ID + MainActivity.getUserId());
            if (descriptionToCategoryDocument == null) {
                MainActivity.financeDocumentModel.createDocument(new DescriptionToCategoryDocument(MainActivity.getUserId(), mDescriptionToCategoryMap));
            } else {
                HashMap<String, Integer> tempMap = descriptionToCategoryDocument.getDescriptionToCategoryMap();
                tempMap.putAll(mDescriptionToCategoryMap);
                try {
                    MainActivity.financeDocumentModel.updateDescriptionToCategoryDocument(
                            descriptionToCategoryDocument,
                            new DescriptionToCategoryDocument(MainActivity.getUserId(), tempMap));
                } catch (ConflictException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (isCommentDialogWindowOpen) {
            commentDialog.dismiss();
            isCommentDialogWindowOpen = true;
        }
        if (dialog != null) {
            dialog.dismiss();
        }

        super.onDestroy();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (dialog != null) {
            outState.putString("mDateTextView", mDateTextView.getText().toString());
            outState.putInt("selectedYear", selectedYear);
            outState.putInt("selectedMonth", selectedMonth);
            outState.putInt("selectedDay", selectedDay);
            outState.putInt("preFinanceDocumentPosition", preFinanceDocumentPosition);
            //noinspection ResourceType
            Spinner categorySpinner = (Spinner) dialog.findViewById(1001);
            assert categorySpinner != null;
            outState.putInt("categorySpinner", categorySpinner.getSelectedItemPosition());
            //noinspection ResourceType
            Spinner currencySpinner = (Spinner) dialog.findViewById(2001);
            assert currencySpinner != null;
            outState.putInt("currencySpinner", currencySpinner.getSelectedItemPosition());
            //noinspection ResourceType
            EditText editTextValue = (EditText) dialog.findViewById(3001);
            assert editTextValue != null;
            outState.putString("editTextValue", editTextValue.getText().toString());
        }
        outState.putString("mComment", mComment);
        if (commentDialog != null) {
            outState.putString("unsavedComment", ((EditText) commentDialog.findViewById(R.id.report_card_comment_edittext)).getText().toString());
        }
        outState.putBoolean("isCommentDialogWindowOpen", isCommentDialogWindowOpen);
    }

    /**
     * Fills recycler view with banking cards
     *
     * @param cards list of cards
     */
    private void generateBankingCards(List<BankingCard> cards) {
        for (BankingCard card : cards) {
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
                                            try {
                                                MainActivity.financeDocumentModel.deleteDocument(((BankingCard) card).getDocument());
                                                mCardArrayAdapter.remove((BankingCard) card);
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


                            break;

                    }
                }
            });
        }
        mCardArrayAdapter = new BankingCardRecyclerViewAdapter(getActivity(), cards);
        if (mCardArrayAdapter.getItemCount() > 0) {
            btnNewBankIntegration.hide();
        } else {
            btnNewBankIntegration.show();
        }
        mCardArrayAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
                if (mCardArrayAdapter.getItemCount() > 0) {
                    btnNewBankIntegration.hide();
                } else {
                    btnNewBankIntegration.show();
                }
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


    @SuppressWarnings("ConstantConditions")
    private void showCreateDocumentDialog(Bundle savedInstanceState, final List<String> preFinanceDocumentList) {
        final Calendar c = Calendar.getInstance(TimeZone.getDefault());
        final DateFormat sdf = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        final int preFinanceDocumentListSize = preFinanceDocumentList.size();

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialog = new AlertDialog.Builder(getContext())
                .setView(layoutInflater.inflate(R.layout.report_card_layout, null))
                .setTitle(getString(R.string.pending_operation))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.save), null)
                .setNegativeButton(getString(R.string.skip), null)
                .show();
        dialog.findViewById(R.id.report_wrapper).setBackgroundResource(0);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mDateTextView = (TextView) dialog.findViewById(R.id.report_card_date_text_view);


        ImageButton datePickerButton = (ImageButton) dialog.findViewById(R.id.report_card_date_picker_button);
        if (datePickerButton != null) {
            datePickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                }
            });
        }

        //init green checkbox
        isCommentSetImageView = (ImageView) dialog.findViewById(R.id.report_card_comment_set_imageview);

        //setting comment button
        ImageButton commentButton = (ImageButton) dialog.findViewById(R.id.report_card_comment_button);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog(null);
            }
        });
        if (savedInstanceState != null) {
            preFinanceDocumentPosition = savedInstanceState.getInt("preFinanceDocumentPosition");
        }
        final RelativeLayout relativeLayout = (RelativeLayout) dialog.findViewById(R.id.report_item_layout);
        assert relativeLayout != null;

        String[] preFinanceArray = preFinanceDocumentList.get(preFinanceDocumentPosition).split("--");
        relativeLayout.addView(createNewCategorySpinner(preFinanceArray[3]));
        relativeLayout.addView(createNewEditText(preFinanceArray[1]));
        relativeLayout.addView(createNewCurrencySpinner(preFinanceArray[2]));
        relativeLayout.addView(createNewDescriptionTextView(preFinanceArray[3]));

        c.setTimeInMillis(Long.valueOf(preFinanceArray[0]) * 1000);
        mDateTextView.setText(sdf.format(c.getTimeInMillis())); // set date of document creation

        if (savedInstanceState != null) {

            selectedYear = savedInstanceState.getInt("selectedYear");
            selectedMonth = savedInstanceState.getInt("selectedMonth");
            selectedDay = savedInstanceState.getInt("selectedDay");
            mDateTextView.setText(savedInstanceState.getString("mDateTextView")); //get date from savedinstancestate

            //reading comment from savedinstance state
            mComment = savedInstanceState.getString("mComment");
            if (!mComment.isEmpty()) {
                isCommentSetImageView.setVisibility(View.VISIBLE);
            } else {
                isCommentSetImageView.setVisibility(View.GONE);
            }
            isCommentDialogWindowOpen = savedInstanceState != null && savedInstanceState.getBoolean("isCommentDialogWindowOpen");
            if (isCommentDialogWindowOpen) {
                final String unSavedComment = savedInstanceState.getString("unsavedComment");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCommentDialog(unSavedComment);
                    }
                }, 100);

            }
            //noinspection ResourceType
            Spinner categorySpinner = (Spinner) dialog.findViewById(1001);
            if (categorySpinner != null) {
                categorySpinner.setSelection(savedInstanceState.getInt("categorySpinner"));
            }
            //noinspection ResourceType
            EditText editTextValue = (EditText) dialog.findViewById(3001);
            if (editTextValue != null) {
                editTextValue.setText(savedInstanceState.getString("editTextValue"));
            }
            //noinspection ResourceType
            Spinner currencySpinner = (Spinner) dialog.findViewById(2001);
            if (currencySpinner != null) {
                currencySpinner.setSelection(savedInstanceState.getInt("currencySpinner"));
            }
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    createNewFinanceDocument(getDefaultCategoriesResult(), getCustomCategoriesResult(), getDate(), getComments());
                    Utils.saveToSharedPreferences(getContext(), "preFinanceDocumentDate", getDate());

                    //noinspection ResourceType
                    String description = ((TextView) dialog.findViewById(4001)).getText().toString();
                    //noinspection ResourceType
                    int category = ((Spinner) dialog.findViewById(1001)).getSelectedItemPosition();
                    mDescriptionToCategoryMap.put(description, category);

                    //reset comment
                    mComment="";
                    isCommentSetImageView.setVisibility(View.GONE);

                    //if there are other transactions  load them otherwise close dialog
                    if (preFinanceDocumentPosition < preFinanceDocumentListSize - 1) {

                        preFinanceDocumentPosition++;
                        relativeLayout.removeAllViews();
                        String[] preFinanceArray = preFinanceDocumentList.get(preFinanceDocumentPosition).split("--");
                        relativeLayout.addView(createNewCategorySpinner(preFinanceArray[3]));
                        relativeLayout.addView(createNewEditText(preFinanceArray[1]));
                        relativeLayout.addView(createNewCurrencySpinner(preFinanceArray[2]));
                        relativeLayout.addView(createNewDescriptionTextView(preFinanceArray[3]));

                        c.setTimeInMillis(Long.valueOf(preFinanceArray[0]) * 1000);
                        mDateTextView.setText(sdf.format(c.getTimeInMillis())); // set date of document creation*/


                    } else {
                        dialog.dismiss();
                        Date currDate = new Date();
                        Utils.saveToSharedPreferences(getContext(), "preFinanceDocumentDate", Long.toString(currDate.getTime() / 1000));
                        mUpdateBankingData.onUpdateData();
                    }
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //reset comment
                mComment="";
                isCommentSetImageView.setVisibility(View.GONE);

                //if there are other transactions  load them otherwise close dialog
                if (preFinanceDocumentPosition < preFinanceDocumentListSize - 1) {
                    preFinanceDocumentPosition++;
                    relativeLayout.removeAllViews();

                    String[] preFinanceArray = preFinanceDocumentList.get(preFinanceDocumentPosition).split("--");
                    relativeLayout.addView(createNewCategorySpinner(preFinanceArray[3]));
                    relativeLayout.addView(createNewEditText(preFinanceArray[1]));
                    relativeLayout.addView(createNewCurrencySpinner(preFinanceArray[2]));
                    relativeLayout.addView(createNewDescriptionTextView(preFinanceArray[3]));

                    c.setTimeInMillis(Long.valueOf(preFinanceArray[0]) * 1000);
                    mDateTextView.setText(sdf.format(c.getTimeInMillis())); // set date of document creation*/

                } else {
                    dialog.dismiss();
                    Date currDate = new Date();
                    Utils.saveToSharedPreferences(getContext(), "preFinanceDocumentDate", Long.toString(currDate.getTime() / 1000));
                    mUpdateBankingData.onUpdateData();

                }
            }
        });
    }

    /**
     * Validates editText fields
     *
     * @return false if field is empty or contains only zeros
     */
    private boolean validateFields() {
        //noinspection ResourceType
        EditText editTextValue = (EditText) dialog.findViewById(3001);
        assert editTextValue != null;
        String value = editTextValue.getText().toString();
        if (value.isEmpty() || !Utils.isNumber(value) || value.matches("0.")) {
            editTextValue.requestFocus();
            Toast.makeText(getContext(), getContext().getString(R.string.set_value), Toast.LENGTH_LONG).show();
            return false;
        }

        if (Float.valueOf(value) == 0) {
            editTextValue.requestFocus();
            Toast.makeText(getContext(), getContext().getString(R.string.set_non_zero_value), Toast.LENGTH_LONG).show();

            return false;
        }
        return true;
    }

    /**
     * Creates comment dialog
     *
     * @param unSavedComment - comment text that was not saved yet
     */
    private void showCommentDialog(String unSavedComment) {
        commentDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.comment)
                .setIcon(R.drawable.ic_comment_grey_24dp)
                .setView(R.layout.report_card_commnet_dialog_layout)
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), null)
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();


        //init comment input field
        final EditText editTextComment = (EditText) commentDialog.findViewById(R.id.report_card_comment_edittext);
        if (unSavedComment != null) {
            editTextComment.setText(unSavedComment);
        } else {
            editTextComment.setText(mComment);
        }

        commentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        commentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //noinspection ConstantConditions
                if (!mComment.isEmpty()) {
                    //noinspection ConstantConditions
                    isCommentSetImageView.setVisibility(View.VISIBLE);
                } else {
                    //noinspection ConstantConditions
                    isCommentSetImageView.setVisibility(View.GONE);
                }
                //noinspection ConstantConditions
                commentDialog.dismiss();
                Log.d(TAG, mComment);
            }
        });
        commentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        commentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mComment = editTextComment.getText().toString();
                //noinspection ConstantConditions
                if (!mComment.isEmpty()) {
                    //noinspection ConstantConditions
                    isCommentSetImageView.setVisibility(View.VISIBLE);
                } else {
                    //noinspection ConstantConditions
                    isCommentSetImageView.setVisibility(View.GONE);
                }

                commentDialog.dismiss();
            }
        });
        commentDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isCommentDialogWindowOpen = false;
            }
        });
        isCommentDialogWindowOpen = true;
    }

    /**
     * Show date picker dialog window
     *
     * @param currYear  current year
     * @param currMonth current month
     * @param currDay   current day
     */
    private void showDatePickerDialog(int currYear, int currMonth, int currDay) {
        if (selectedYear == 0) {
            selectedYear = currYear;
            selectedMonth = currMonth;
            selectedDay = currDay;
        }
        //change theme for buggy samsung device
        Context context = getContext();
        if (Utils.isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(getContext(), android.R.style.Theme_Holo_Light_Dialog);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                selectedYear = year;
                selectedMonth = monthOfYear;
                selectedDay = dayOfMonth;
                Calendar c = Calendar.getInstance(TimeZone.getDefault());
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                DateFormat sdf = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                mDateTextView.setText(sdf.format(c.getTimeInMillis()));
            }
        }, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Generates operation category spinner
     *
     * @return currency spinner
     */

    private Spinner createNewCurrencySpinner(String value) {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(getContext(), 40));
        final Spinner spinner = new Spinner(getContext());
        lparams.addRule(RelativeLayout.RIGHT_OF, 3001);


        spinner.setLayoutParams(lparams);
        ArrayAdapter<CharSequence> currencySpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.report_activity_currency_spinner, android.R.layout.simple_spinner_item);
        currencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(currencySpinnerAdapter);
        spinner.setSelection(Utils.getCurrencyPosition(value));
        //noinspection ResourceType
        spinner.setId(2001);
        spinner.setSaveEnabled(true);
        return spinner;
    }

    /**
     * Generates currency type spinner
     *
     * @param key hashmap key
     * @return category spinner
     */


    private Spinner createNewCategorySpinner(String key) {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(getContext(), 40));
        final Spinner spinner = new Spinner(getContext());
        spinner.setLayoutParams(lparams);

        //building category list
        List<String> defaultCategoriesList = Arrays.asList(getContext().getResources().getStringArray(R.array.report_activity_category_spinner));
        List<String> incomeCategoriesList = new ArrayList<>(defaultCategoriesList.subList(
                0,
                FinanceDocument.NUMBER_OF_INCOME_CATEGORIES));
        List<String> expenseCategoriesList = new ArrayList<>(defaultCategoriesList.subList(
                FinanceDocument.NUMBER_OF_INCOME_CATEGORIES,
                FinanceDocument.NUMBER_OF_CATEGORIES));

        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> categoriesMap = categoryDocument.getCategoriesMap();
        for (Map.Entry<String, List<String>> entry : categoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                incomeCategoriesList.add(entry.getValue().get(0));
            } else {
                expenseCategoriesList.add(entry.getValue().get(0));
            }
        }
        ArrayAdapter<CharSequence> categorySpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.addAll(incomeCategoriesList);
        categorySpinnerAdapter.addAll(expenseCategoriesList);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(categorySpinnerAdapter);
        //noinspection ResourceType
        spinner.setId(1001);
        spinner.setSaveEnabled(true);
        //setting selection from description to category map
        if (key != null && !key.isEmpty() && mDescriptionToCategoryMap.containsKey(key)) {
            if (mDescriptionToCategoryMap.get(key) >= categorySpinnerAdapter.getCount()) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(mDescriptionToCategoryMap.get(key));
            }
        }

        return spinner;
    }

    /**
     * Generates operation input value edit text
     *
     * @return edit text field
     */


    private EditText createNewEditText(String value) {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(getContext(), 40));
        final EditText editText = new EditText(getContext());
        lparams.addRule(RelativeLayout.RIGHT_OF, 1001);


        int maxLength = 8;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(fArray);
        editText.setLayoutParams(lparams);
        editText.setHint(getContext().getResources().getString(R.string.value));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //noinspection ResourceType
        editText.setId(3001);
        editText.setText(value);
        editText.setSaveEnabled(true);
        return editText;
    }


    /**
     * Generates operation description text view
     *
     * @return text view field
     */
    private TextView createNewDescriptionTextView(String value) {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.setMargins(Utils.dpToPx(getContext(), 4), Utils.dpToPx(getContext(), 8), Utils.dpToPx(getContext(), 4), Utils.dpToPx(getContext(), 8));
        lparams.addRule(RelativeLayout.BELOW, 1001);
        lparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        final TextView textView = new TextView(getContext());
        textView.setLayoutParams(lparams);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        //noinspection ResourceType
        textView.setId(4001);
        textView.setText(value);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        return textView;

    }


    /**
     * Compiles inputs from fields
     *
     * @return list of strings category-value-currency
     */

    private SparseArray<Object> getDefaultCategoriesResult() {
        SparseArray<Object> params = new SparseArray<>();
        ArrayList<String> list = new ArrayList<>();

        //noinspection ResourceType

        Spinner categorySpinner = (Spinner) dialog.findViewById(1001);
        //noinspection ResourceType

        Spinner currencySpinner = (Spinner) dialog.findViewById(2001);
        //noinspection ResourceType

        EditText editTextValue = (EditText) dialog.findViewById(3001);


        if (categorySpinner != null && editTextValue != null && currencySpinner != null) {

            int selectedCategory = categorySpinner.getSelectedItemPosition();

            if (selectedCategory < FinanceDocument.NUMBER_OF_INCOME_CATEGORIES) //default income categories

            {
                list.add(selectedCategory + "-"
                        + categorySpinner.getSelectedItem().toString() + "-"
                        + editTextValue.getText().toString().replaceFirst("^0+(?!$)", "") + "-"
                        + currencySpinner.getSelectedItem().toString());

                //or default expense categories
            } else if (selectedCategory >= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + customIncomeCategoriesNumber &&
                    selectedCategory < FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + customIncomeCategoriesNumber + FinanceDocument.NUMBER_OF_EXPENSE_CATEGORIES) {
                selectedCategory = categorySpinner.getSelectedItemPosition() - customIncomeCategoriesNumber;
                list.add(selectedCategory + "-"
                        + categorySpinner.getSelectedItem().toString() + "-"
                        + editTextValue.getText().toString().replaceFirst("^0+(?!$)", "") + "-"
                        + currencySpinner.getSelectedItem().toString());
            }
        }

        params.append(FinanceDocument.PARAM_USERID, MainActivity.getUserId());
        params.append(FinanceDocument.PARAM_SALARY, Utils.getItem(list, 0));
        params.append(FinanceDocument.PARAM_RENTAL_INCOME, Utils.getItem(list, 1));
        params.append(FinanceDocument.PARAM_INTEREST, Utils.getItem(list, 2));
        params.append(FinanceDocument.PARAM_GIFTS, Utils.getItem(list, 3));
        params.append(FinanceDocument.PARAM_OTHER_INCOME, Utils.getItem(list, 4));
        params.append(FinanceDocument.PARAM_TAXES, Utils.getItem(list, 5));
        params.append(FinanceDocument.PARAM_MORTGAGE, Utils.getItem(list, 6));
        params.append(FinanceDocument.PARAM_CREDIT_CARD, Utils.getItem(list, 7));
        params.append(FinanceDocument.PARAM_UTILITIES, Utils.getItem(list, 8));
        params.append(FinanceDocument.PARAM_FOOD, Utils.getItem(list, 9));
        params.append(FinanceDocument.PARAM_CAR_PAYMENT, Utils.getItem(list, 10));
        params.append(FinanceDocument.PARAM_PERSONAL, Utils.getItem(list, 11));
        params.append(FinanceDocument.PARAM_ACTIVITIES, Utils.getItem(list, 12));
        params.append(FinanceDocument.PARAM_OTHER_EXPENSE, Utils.getItem(list, 13));


        return params;
    }

    /**
     * gets custom categories report
     *
     * @return map of custom categories
     * key - category id
     * value - 0 - value
     * 1 - currency
     * 2 - recursion
     */
    public HashMap<String, List<String>> getCustomCategoriesResult() {
        HashMap<String, List<String>> customParamsMap = new HashMap<>();
        //noinspection ResourceType
        Spinner categorySpinner = (Spinner) dialog.findViewById(1001);
        //noinspection ResourceType
        Spinner currencySpinner = (Spinner) dialog.findViewById(2001);
        //noinspection ResourceType
        EditText editTextValue = (EditText) dialog.findViewById(3001);
        int selectedCategory = categorySpinner.getSelectedItemPosition();
        if ((selectedCategory >= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES &&
                selectedCategory < FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + customIncomeCategoriesNumber) ||
                selectedCategory >= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES + customIncomeCategoriesNumber + FinanceDocument.NUMBER_OF_EXPENSE_CATEGORIES) {

            /**
             * 0-value
             * 1 - currency
             * 2 -recursion
             */
            List<String> customResultList = new ArrayList<>();
            customResultList.add(0, editTextValue.getText().toString().replaceFirst("^0+(?!$)", ""));
            customResultList.add(1, currencySpinner.getSelectedItem().toString());
            customResultList.add(2, "Never");

            String selectedCustomCategory = categorySpinner.getSelectedItem().toString();
            for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
                if (entry.getValue().get(0).equals(selectedCustomCategory)) {
                    customParamsMap.put(entry.getKey(), customResultList);
                }
            }
        }


        return customParamsMap;
    }

    /**
     * Gets selected in calendar date
     *
     * @return date in unix timestamp format
     */
    private String getDate() {
        Date date = new Date();
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        try {
            date = formatter.parse(mDateTextView.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);

        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        return Long.toString(c.getTimeInMillis() / 1000);
    }

    /**
     * Gets document comments
     *
     * @return doc comments
     */
    public String getComments() {
        return mComment;
    }

    /**
     * Creation new document from data
     *
     * @param defaultParams list of finance document fields
     */
    private void createNewFinanceDocument(SparseArray<Object> defaultParams, HashMap<String, List<String>> customParams, String date, String comments) {
        FinanceDocument financeDocument = new FinanceDocument(defaultParams, customParams, date, comments);
        MainActivity.financeDocumentModel.createDocument(financeDocument);
        //Save tag when new doc created
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String currentDate = df.format(c.getTime());
        Utils.saveToSharedPreferences(getContext(), "createdDate", currentDate);

    }

    /**
     * Sends broadcast intent to update history
     */
    public void updateBankingCard() {
        Intent intent = new Intent(BankingCardLoader.ACTION);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private final LoaderManager.LoaderCallbacks<List<BankingCard>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<BankingCard>>() {
        @Override
        public Loader<List<BankingCard>> onCreateLoader(int id, Bundle args) {
            return new BankingCardLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<BankingCard>> loader, List<BankingCard> data) {
            generateBankingCards(data);
        }

        @Override
        public void onLoaderReset(Loader<List<BankingCard>> loader) {
        }
    };

}
