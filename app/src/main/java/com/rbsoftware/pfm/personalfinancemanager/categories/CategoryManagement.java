package com.rbsoftware.pfm.personalfinancemanager.categories;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudant.sync.datastore.ConflictException;
import com.google.android.gms.analytics.HitBuilders;
import com.rbsoftware.pfm.personalfinancemanager.ConnectionDetector;
import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.billing.IabHelper;
import com.rbsoftware.pfm.personalfinancemanager.billing.IabResult;
import com.rbsoftware.pfm.personalfinancemanager.billing.Inventory;
import com.rbsoftware.pfm.personalfinancemanager.billing.Purchase;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Roman Burzakovskiy
 */
public class CategoryManagement extends Fragment implements OnCategoryRemoveListener {
    private final static String TAG = "CategoryManagement";
    private static final int RC_REQUEST_BUY_EXTRA_CATEGORY = 9000;

    /**
     * weak reference to context
     */
    private WeakReference<Context> weakContext;
    /**
     * Create category dialog
     */
    private AlertDialog mDialog;
    /**
     * true if dialog is opened
     */
    private boolean isDialogWindowOpen;
    /**
     * Create custom category FAB
     */
    private FloatingActionButton btnCreateCategory;
    /**
     * Income categories card view
     */
    private CardViewNative mIncomeCardView;
    /**
     * Expense categories card view
     */
    private CardViewNative mExpenseCardView;
    /**
     * Income categories card internet connection detector
     */
    private ConnectionDetector mConnectionDetector;
    /**
     * State of instance
     */
    private Bundle savedInstanceState;

    /**
     * true if category limit has been reached
     */
    private static boolean isCategoryLimitReached = false;

    public CategoryManagement() {
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
        return inflater.inflate(R.layout.fragment_category_management, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        weakContext = new WeakReference<>(getContext());

        this.savedInstanceState = savedInstanceState;
        getActivity().setTitle(getResources().getStringArray(R.array.drawer_menu)[7]);
        isDialogWindowOpen = savedInstanceState != null && savedInstanceState.getBoolean("isDialogWindowOpen");
        if (isDialogWindowOpen) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDialogWindow(savedInstanceState);
                }
            }, 100);

        }

        if (mIncomeCardView == null) {
            mIncomeCardView = (CardViewNative) getActivity().findViewById(R.id.category_income_card);
        }
        if (mExpenseCardView == null) {
            mExpenseCardView = (CardViewNative) getActivity().findViewById(R.id.category_expense_card);
        }


        btnCreateCategory = (FloatingActionButton) getActivity().findViewById(R.id.btn_new_category);

        btnCreateCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isCategoryLimitReached) {
                    showDialogWindow(savedInstanceState);
                } else if (MainActivity.billingHelper.isSetupDone()) {
                    try {

                        MainActivity.billingHelper.launchPurchaseFlow(getActivity(), Inventory.SKU_EXTRA_CATEGORY, RC_REQUEST_BUY_EXTRA_CATEGORY,
                                mPurchaseFinishedListener, MainActivity.getUserId());
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        Log.e(TAG, "Error launching purchase flow. Another async operation in progress.");
                    }
                } else {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        generateCardData();

        if (mConnectionDetector == null) {
            mConnectionDetector = new ConnectionDetector(getContext());
        }
        MainActivity.mTracker.setScreenName(TAG);


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
        btnCreateCategory.hide();
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
            outState.putString("editTextCategoryName", ((EditText) mDialog.findViewById(R.id.et_category_name)).getText().toString());
            //noinspection ConstantConditions
            outState.putInt("categoryTypeSpinner", ((Spinner) mDialog.findViewById(R.id.category_type_spinner)).getSelectedItemPosition());
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Generates categories cards
     */
    private void generateCardData() {
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> categoriesMap = categoryDocument.getCategoriesMap();

        //check if max number of categories reached
        int mapSize = categoriesMap.size();

        String numberOfExtraCategoriesString = Utils.readFromSharedPreferences(weakContext.get(), "numberOfExtraCategories", "0");
        //decrypting value
        if (!numberOfExtraCategoriesString.equals("0") || !Utils.isNumber(numberOfExtraCategoriesString))
            numberOfExtraCategoriesString = Utils.decrypt(numberOfExtraCategoriesString);
        //number of purchased categories
        int numberOfExtraCategories = Integer.valueOf(numberOfExtraCategoriesString);
        isCategoryLimitReached = mapSize >= FinanceDocument.MAX_NUMBER_OF_CUSTOM_CATEGORIES + numberOfExtraCategories;

        HashMap<String, List<String>> incomeMap = new HashMap<>();
        HashMap<String, List<String>> expenseMap = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : categoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                incomeMap.put(entry.getKey(), entry.getValue());
            } else {
                expenseMap.put(entry.getKey(), entry.getValue());
            }
        }
        CategoryIncomeCard incomeCard = new CategoryIncomeCard(weakContext.get(), incomeMap, this);
        if (mIncomeCardView.getCard() == null) {
            mIncomeCardView.setCard(incomeCard);
        } else {
            mIncomeCardView.replaceCard(incomeCard);
        }

        CategoryExpenseCard expenseCard = new CategoryExpenseCard(weakContext.get(), expenseMap, this);
        if (mExpenseCardView.getCard() == null) {
            mExpenseCardView.setCard(expenseCard);
        } else {
            mExpenseCardView.replaceCard(expenseCard);
        }
    }

    /**
     * Generates create category popup window
     */
    @SuppressWarnings("ConstantConditions")
    private void showDialogWindow(Bundle savedInstanceState) {

        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialog = new AlertDialog.Builder(getContext())
                .setView(layoutInflater.inflate(R.layout.category_create_card_layout, null))
                .setTitle(getString(R.string.create_category))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), null)
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();
        final EditText editTextCategoryName = (EditText) mDialog.findViewById(R.id.et_category_name);
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //noinspection ConstantConditions
                editTextCategoryName.setText("");
                mDialog.dismiss();
            }
        });
        ArrayAdapter<CharSequence> budgetPeriodSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.category_type_spinner, android.R.layout.simple_spinner_item);
        budgetPeriodSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner categoryTypeSpinner = (Spinner) mDialog.findViewById(R.id.category_type_spinner);
        categoryTypeSpinner.setAdapter(budgetPeriodSpinnerAdapter);

        if (savedInstanceState != null) {
            //noinspection ConstantConditions
            editTextCategoryName.setText(savedInstanceState.getString("editTextCategoryName"));
            categoryTypeSpinner.setSelection(savedInstanceState.getInt("categoryTypeSpinner"));

        }
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));

        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressWarnings("ConstantConditions") String categoryName = editTextCategoryName.getText().toString();
                if (validateCategoryName(categoryName)) {
                    editTextCategoryName.setText("");
                    mDialog.dismiss();

                    String categoryID = Utils.generateRandomString(10);
                    updateCategoryDocument(categoryID, categoryName, Integer.toString(categoryTypeSpinner.getSelectedItemPosition()));
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
     * Updates category document
     *
     * @param id   of category
     * @param name of category
     * @param type "0"- income
     *             "1"- expense
     */
    private void updateCategoryDocument(String id, String name, String type) {
        CategoryDocument oldDoc = MainActivity.financeDocumentModel.getCategoryDocument(CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> tempMap = oldDoc.getCategoriesMap();
        List<String> newCategory = new ArrayList<>();
        newCategory.add(0, name);
        newCategory.add(1, type);
        tempMap.put(id, newCategory);
        try {
            MainActivity.financeDocumentModel.updateCategoryDocument(oldDoc, new CategoryDocument(MainActivity.getUserId(), tempMap));
            generateCardData();
        } catch (ConflictException e) {
            e.printStackTrace();
        }


    }

    /**
     * Validates category name
     *
     * @param categoryName new category name
     * @return true if name is valid
     */
    private boolean validateCategoryName(String categoryName) {
        //check if new category is empty
        if (categoryName.isEmpty()) {
            Toast.makeText(getContext(), getContext().getString(R.string.set_value), Toast.LENGTH_LONG).show();
            return false;
        }

        //check if name is number
        if (Utils.isNumber(categoryName)) {
            Toast.makeText(getContext(), getContext().getString(R.string.wrong_name), Toast.LENGTH_LONG).show();
            return false;
        }

        //check if new category is in default categories
        String[] defaultCategories = getResources().getStringArray(R.array.report_activity_category_spinner);

        for (String defaultCategory : defaultCategories) {
            if (defaultCategory.toLowerCase().equals(categoryName.trim().toLowerCase())) {

                Toast.makeText(getContext(), getString(R.string.category_exists), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        //checking if new category is in custom categories list

        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> customCategoriesMap = categoryDocument.getCategoriesMap();
        if (!customCategoriesMap.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : customCategoriesMap.entrySet()) {
                if (entry.getValue().get(0).trim().toLowerCase().equals(categoryName.trim().toLowerCase())) {
                    Toast.makeText(getContext(), getString(R.string.category_exists), Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onRemove(final String categoryId) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.delete) + " " + Utils.keyToString(getContext(), categoryId))
                .setMessage(getContext().getString(R.string.delete_dialog_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        removeCategory(categoryId);


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
    }

    private void removeCategory(String categoryId) {
        CategoryDocument oldDoc = MainActivity.financeDocumentModel.getCategoryDocument(CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> tempMap = oldDoc.getCategoriesMap();
        tempMap.remove(categoryId);

        try {
            MainActivity.financeDocumentModel.updateCategoryDocument(oldDoc, new CategoryDocument(MainActivity.getUserId(), tempMap));

            generateCardData();
        } catch (ConflictException e) {
            e.printStackTrace();
        }
    }


    /**
     * Purchase finished listener
     */
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (MainActivity.billingHelper == null) return;

            if (result.isFailure()) {
                Log.e(TAG, "Error purchasing: " + result);
                return;
            }
            if (!Utils.verifyDeveloperPayload(purchase)) {
                Log.e(TAG, "Error purchasing. Authenticity verification failed.");
                return;
            }

            if (purchase.getSku().equals(Inventory.SKU_EXTRA_CATEGORY)) {
                try {
                    btnCreateCategory.hide();
                    MainActivity.billingHelper.consumeAsync(purchase, mConsumeFinishedListener);

                } catch (IabHelper.IabAsyncInProgressException e) {
                    Log.e(TAG, "Error consuming gas. Another async operation in progress.");
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

                    String numberOfExtraCategoriesString = Utils.readFromSharedPreferences(weakContext.get(), "numberOfExtraCategories", "0");
                    //decrypting string
                    if (!numberOfExtraCategoriesString.equals("0") || !Utils.isNumber(numberOfExtraCategoriesString))
                        numberOfExtraCategoriesString = Utils.decrypt(numberOfExtraCategoriesString);
                    int numberOfExtraCategories = Integer.valueOf(numberOfExtraCategoriesString);
                    numberOfExtraCategories += 1;

                    //encrypting string
                    numberOfExtraCategoriesString = Utils.encrypt(String.valueOf(numberOfExtraCategories));
                    Utils.saveToSharedPreferences(weakContext.get(), "numberOfExtraCategories", numberOfExtraCategoriesString);

                    //updateing data on consuming finished
                    generateCardData();
                    btnCreateCategory.show();
                }
            } else {
                Log.e(TAG, "Error while consuming: " + result);
            }
        }
    };

}
