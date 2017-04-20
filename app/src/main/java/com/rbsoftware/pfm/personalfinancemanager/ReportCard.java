package com.rbsoftware.pfm.personalfinancemanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


/**
 * Holds methods for edit and report layout and operations
 *
 * @author Roman Burzakovskiy
 */
@SuppressWarnings("ConstantConditions")
public class ReportCard {
    private final static String TAG = "ReportCard";
    /**
     * Application context weak reference
     */
    private WeakReference<Context> weakContext;
    /**
     * Instance state
     */
    private Bundle savedInstanceState;
    /**
     * add new row FAB
     */
    private FloatingActionButton btnAddNewRow;

    /**
     * layout with input rows
     */
    private RelativeLayout mLayout;
    /**
     * selected date
     */
    private TextView mTextViewDate;
    /**
     * Weak reference to activity
     */
    private WeakReference<Activity> weakActivity;
    private int categorySpinnerId = 1001; //IDs of categorySpinner
    private int currencySpinnerId = 2001; //IDs of currencySpinner
    private int editTextValueId = 3001;   //Ids of editText
    private int deleteButtonId = 4001;   //Ids of deleteButton
    private int buttonCounter; //counter to make button "Add Line" invisible

    /**
     * Map of custom categories
     * key - category id
     * value - 0-category name, 1- income/expense
     */
    private HashMap<String, List<String>> mCustomCategoriesMap;
    private int customCategoriesNumber = 0;// number of custom categories
    private int customIncomeCategoriesNumber = 0;// number of custom income categories
    private int customExpenseCategoriesNumber = 0;// number of custom expense categories


    /**
     * Selected date parameters
     */
    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedDay = 0;

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
     * Finance document to create/edit
     */
    private FinanceDocument mFinanceDocument;


    public ReportCard(Context context) {
        this.weakContext = new WeakReference<>(context);
    }


    public void setup(Activity activity, Bundle savedInstanceState, String docId) {
        weakActivity = new WeakReference<>(activity);

        this.savedInstanceState = savedInstanceState;
        if (docId == null) {
            mFinanceDocument = null;
        } else {
            mFinanceDocument = MainActivity.financeDocumentModel.getFinanceDocument(docId);
        }

        //reading custom categories data
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        mCustomCategoriesMap = categoryDocument.getCategoriesMap();
        customCategoriesNumber = mCustomCategoriesMap.size();
        for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                customIncomeCategoriesNumber++;
            } else {
                customExpenseCategoriesNumber++;
            }
        }


        createLayout();
    }

    /**
     * Gets getCategorySpinnerId value
     *
     * @return int getCategorySpinnerId
     */
    private int getCategorySpinnerId() {
        return categorySpinnerId;
    }

    /**
     * Gets buttonCounter value
     *
     * @return int buttonCounter
     */
    private int getButtonCounter() {
        return buttonCounter;
    }

    /**
     * Prepares layout for onSaveInstanceState
     *
     * @return Bundle of layout state
     */
    public Bundle getElementsToSave() {
        Bundle outState = new Bundle();
        int counter = this.getCategorySpinnerId() - 1000;
        outState.putInt("counter", counter);
        outState.putInt("buttonCounter", this.getButtonCounter());
        outState.putString("mTextViewDate", mTextViewDate.getText().toString());
        outState.putInt("selectedYear", selectedYear);
        outState.putInt("selectedMonth", selectedMonth);
        outState.putInt("selectedDay", selectedDay);
        outState.putString("mComment", mComment);
        if (commentDialog != null) {
            outState.putString("unsavedComment", ((EditText) commentDialog.findViewById(R.id.report_card_comment_edittext)).getText().toString());
        }
        outState.putBoolean("isCommentDialogWindowOpen", isCommentDialogWindowOpen);

        for (int i = 1; i < counter; i++) {
            Spinner categorySpinner = (Spinner) weakActivity.get().findViewById(1000 + i);
            outState.putInt("categorySpinner" + i, categorySpinner.getSelectedItemPosition());
            Spinner currencySpinner = (Spinner) weakActivity.get().findViewById(2000 + i);
            outState.putInt("currencySpinner" + i, currencySpinner.getSelectedItemPosition());
            EditText editTextValue = (EditText) weakActivity.get().findViewById(3000 + i);
            outState.putString("editTextValue" + i, editTextValue.getText().toString());
        }
        return outState;
    }


    /**
     * Validates editText fields
     *
     * @return false if field is empty or contains only zeros
     */
    public boolean validateFields() {
        int counter = this.getCategorySpinnerId() - 1000;
        for (int i = 1; i < counter; i++) {
            EditText editTextValue = (EditText) weakActivity.get().findViewById(3000 + i);
            String value = editTextValue.getText().toString();
            if (value.isEmpty() || !Utils.isNumber(value) || value.matches("0.")) {
                editTextValue.requestFocus();
                Toast.makeText(weakContext.get(), weakContext.get().getString(R.string.set_value), Toast.LENGTH_LONG).show();

                return false;
            }

            if (Float.valueOf(value) == 0) {
                editTextValue.requestFocus();
                Toast.makeText(weakContext.get(), weakContext.get().getString(R.string.set_non_zero_value), Toast.LENGTH_LONG).show();

                return false;
            }
        }
        return true;
    }


    /**
     * Validates spinners
     *
     * @return false if category is duplicated
     */
    public boolean validateSpinner() {
        int counter = this.getCategorySpinnerId() - 1000;
        for (int i = counter - 1; i > 1; i--) {
            for (int j = i - 1; j >= 1; j--) {
                if (i != j && ((Spinner) weakActivity.get().findViewById(1000 + i)).getSelectedItem().equals(((Spinner) weakActivity.get().findViewById(1000 + j)).getSelectedItem())) {

                    Toast.makeText(weakContext.get(), weakContext.get().getString(R.string.duplicate_entry) + " : " + ((Spinner) weakActivity.get().findViewById(1000 + i)).getSelectedItem().toString(), Toast.LENGTH_LONG).show();

                    return false;

                }

            }
        }

        return true;
    }


    /**
     * Compiles inputs from fields
     *
     * @return list of strings category-value-currency
     */

    public ArrayList<String> getDefaultCategoriesResult() {
        ArrayList<String> list = new ArrayList<>();
        int counter = this.getCategorySpinnerId() - 1000;
        for (int i = 1; i < counter; i++) {
            Spinner categorySpinner = (Spinner) weakActivity.get().findViewById(1000 + i);
            Spinner currencySpinner = (Spinner) weakActivity.get().findViewById(2000 + i);
            EditText editTextValue = (EditText) weakActivity.get().findViewById(3000 + i);
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

        return list;
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
        int counter = this.getCategorySpinnerId() - 1000;
        for (int i = 1; i < counter; i++) {
            Spinner categorySpinner = (Spinner) weakActivity.get().findViewById(1000 + i);
            Spinner currencySpinner = (Spinner) weakActivity.get().findViewById(2000 + i);
            EditText editTextValue = (EditText) weakActivity.get().findViewById(3000 + i);
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
        }

        return customParamsMap;
    }

    /**
     * Gets selected in calendar date
     *
     * @return date in unix timestamp format
     */
    public String getDate() {
        Date date = new Date();
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        try {
            date = formatter.parse(mTextViewDate.getText().toString());
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
        Context context = weakContext.get();
        if (Utils.isBrokenSamsungDevice()) {
            context = new ContextThemeWrapper(weakContext.get(), android.R.style.Theme_Holo_Light_Dialog);
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
                mTextViewDate.setText(sdf.format(c.getTimeInMillis()));
            }
        }, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


    /**
     * Creates comment dialog
     *
     * @param unSavedComment - comment text that was not saved yet
     */
    private void showCommentDialog(String unSavedComment) {
        commentDialog = new AlertDialog.Builder(weakContext.get())
                .setTitle(R.string.comment)
                .setIcon(R.drawable.ic_comment_grey_24dp)
                .setView(R.layout.report_card_commnet_dialog_layout)
                .setCancelable(false)
                .setPositiveButton(weakContext.get().getString(android.R.string.ok), null)
                .setNegativeButton(weakContext.get().getString(android.R.string.cancel), null)
                .show();


        //init comment input field
        final EditText editTextComment = (EditText) commentDialog.findViewById(R.id.report_card_comment_edittext);
        if (unSavedComment != null) {
            editTextComment.setText(unSavedComment);
        } else {
            editTextComment.setText(mComment);
        }

        commentDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(weakContext.get(), R.color.colorAccent));
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
            }
        });
        commentDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(weakContext.get(), R.color.colorAccent));
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
     * Generates inner card layout
     */
    @SuppressWarnings("unchecked")
    private void createLayout() {


        mLayout = (RelativeLayout) weakActivity.get().findViewById(R.id.report_item_layout);
        mTextViewDate = (TextView) weakActivity.get().findViewById(R.id.report_card_date_text_view);
        final Calendar c = Calendar.getInstance(TimeZone.getDefault());
        DateFormat sdf = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());


        //Setting up datepicker button

        ImageButton datePickerButton = (ImageButton) weakActivity.get().findViewById(R.id.report_card_date_picker_button);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            }
        });

        //init green checkbox
        isCommentSetImageView = (ImageView) weakActivity.get().findViewById(R.id.report_card_comment_set_imageview);

        //setting comment button
        ImageButton commentButton = (ImageButton) weakActivity.get().findViewById(R.id.report_card_comment_button);
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog(null);
            }
        });


        if (savedInstanceState == null) { //prepare initial layout for report
            if (mFinanceDocument == null) {
                mTextViewDate.setText(sdf.format(c.getTimeInMillis())); // set current date

                //set visibility gone to comment check box
                isCommentSetImageView.setVisibility(View.GONE);

                //create intup rows
                mLayout.addView(createNewCategorySpinner());
                mLayout.addView(createNewEditText());
                mLayout.addView(createNewCurrencySpinner());
                mLayout.addView(createNewDeleteButton());
                buttonCounter = 0;
            } else {
                c.setTimeInMillis(Long.valueOf(mFinanceDocument.getDate()) * 1000);
                mTextViewDate.setText(sdf.format(c.getTimeInMillis())); // set date of document creation

                //getting comment from finance document
                mComment = mFinanceDocument.getComments();
                if (!mComment.isEmpty()) {
                    isCommentSetImageView.setVisibility(View.VISIBLE);
                } else {
                    isCommentSetImageView.setVisibility(View.GONE);
                }

                //prepare initial layout for edit
                buttonCounter = -1;
                HashMap<String, List<String>> allValueMap = mFinanceDocument.getValuesMap();
                for (Map.Entry<String, List<String>> entry : allValueMap.entrySet()) {
                    List<String> valuesList = entry.getValue();
                    if (valuesList != null) {
                        mLayout.addView(createNewCategorySpinner());
                        mLayout.addView(createNewEditText());
                        mLayout.addView(createNewCurrencySpinner());
                        mLayout.addView(createNewDeleteButton());
                        int spinnerSelection;
                        if (Utils.isNumber(entry.getKey())) {
                            if (Integer.valueOf(entry.getKey()) <= FinanceDocument.NUMBER_OF_INCOME_CATEGORIES) {
                                spinnerSelection = Integer.valueOf(entry.getKey()) - 1;
                            } else {
                                spinnerSelection = Integer.valueOf(entry.getKey()) + customIncomeCategoriesNumber - 1;
                            }
                        } else {
                            String categoryName = mCustomCategoriesMap.get(entry.getKey()).get(0);
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) ((Spinner) weakActivity.get().findViewById(categorySpinnerId - 1)).getAdapter();
                            spinnerSelection = adapter.getPosition(categoryName);
                        }
                        ((Spinner) weakActivity.get().findViewById(categorySpinnerId - 1)).setSelection(spinnerSelection);
                        ((EditText) weakActivity.get().findViewById(editTextValueId - 1)).setText(valuesList.get(0));
                        ((Spinner) weakActivity.get().findViewById(currencySpinnerId - 1)).setSelection(Utils.getCurrencyPosition(valuesList.get(1)));
                        buttonCounter++;
                    }

                }

            }

        } else {
            selectedYear = savedInstanceState.getInt("selectedYear");
            selectedMonth = savedInstanceState.getInt("selectedMonth");
            selectedDay = savedInstanceState.getInt("selectedDay");
            mTextViewDate.setText(savedInstanceState.getString("mTextViewDate")); //get date from savedinstancestate

            //reading comment from savedinstance state
            mComment = savedInstanceState.getString("mComment");
            if (!mComment.isEmpty()) {
                isCommentSetImageView.setVisibility(View.VISIBLE);
            } else {
                isCommentSetImageView.setVisibility(View.GONE);
            }
            isCommentDialogWindowOpen = savedInstanceState != null && savedInstanceState.getBoolean("isCommentDialogWindowOpen");
            if (isCommentDialogWindowOpen) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCommentDialog(savedInstanceState.getString("unsavedComment"));
                    }
                }, 100);

            }
            //views states were saved in onSaveInstanceState
            int counter = savedInstanceState.getInt("counter");
            for (int i = 1; i < counter; i++) {
                mLayout.addView(createNewCategorySpinner());
                Spinner categorySpinner = (Spinner) weakActivity.get().findViewById(1000 + i);
                categorySpinner.setSelection(savedInstanceState.getInt("categorySpinner" + i));
                mLayout.addView(createNewEditText());
                EditText editTextValue = (EditText) weakActivity.get().findViewById(3000 + i);
                editTextValue.setText(savedInstanceState.getString("editTextValue" + i));
                mLayout.addView(createNewCurrencySpinner());
                Spinner currencySpinner = (Spinner) weakActivity.get().findViewById(2000 + i);
                currencySpinner.setSelection(savedInstanceState.getInt("currencySpinner" + i));
                mLayout.addView(createNewDeleteButton());
                buttonCounter = savedInstanceState.getInt("buttonCounter");

            }

        }
        btnAddNewRow = (FloatingActionButton) weakActivity.get().findViewById(R.id.btn_add_new);

        //hide btnAddNewRow button if all fields called
        if (buttonCounter >= FinanceDocument.NUMBER_OF_CATEGORIES + customCategoriesNumber - 1) {
            btnAddNewRow.hide();
        }
        btnAddNewRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayout.addView(createNewCategorySpinner());
                mLayout.addView(createNewEditText());
                mLayout.addView(createNewCurrencySpinner());
                mLayout.addView(createNewDeleteButton());
                buttonCounter++;

                if (buttonCounter >= FinanceDocument.NUMBER_OF_CATEGORIES + customCategoriesNumber - 1) {
                    btnAddNewRow.hide();
                }


            }
        });
    }


    /**
     * Generates operation category spinner
     *
     * @return currency spinner
     */

    private Spinner createNewCurrencySpinner() {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(weakContext.get(), 40));
        final Spinner spinner = new Spinner(weakContext.get());
        int position = 0;
        lparams.addRule(RelativeLayout.RIGHT_OF, editTextValueId - 1);

        if (categorySpinnerId > 1001) {

            lparams.addRule(RelativeLayout.BELOW, currencySpinnerId - 1);
        }

        spinner.setLayoutParams(lparams);
        ArrayAdapter<CharSequence> currencySpinnerAdapter = ArrayAdapter.createFromResource(weakContext.get(), R.array.report_activity_currency_spinner, android.R.layout.simple_spinner_item);
        currencySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        String[] currencyList = weakContext.get().getResources().getStringArray(R.array.report_activity_currency_spinner);
        for (int i = 0; i < currencyList.length; i++) {
            if (currencyList[i].equals(MainActivity.defaultCurrency)) {
                position = i;
            }
        }
        spinner.setAdapter(currencySpinnerAdapter);
        spinner.setSelection(position);
        //noinspection ResourceType
        spinner.setId(currencySpinnerId);
        spinner.setSaveEnabled(true);
        currencySpinnerId++;
        return spinner;
    }

    /**
     * Generates currency type spinner
     *
     * @return category spinner
     */


    private Spinner createNewCategorySpinner() {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(weakContext.get(), 40));
        final Spinner spinner = new Spinner(weakContext.get());

        if (categorySpinnerId > 1001) {

            lparams.addRule(RelativeLayout.BELOW, categorySpinnerId - 1);
        }

        spinner.setLayoutParams(lparams);

        //building category list
        List<String> defaultCategoriesList = Arrays.asList(weakContext.get().getResources().getStringArray(R.array.report_activity_category_spinner));
        List<String> incomeCategoriesList = new ArrayList<>(defaultCategoriesList.subList(
                0,
                FinanceDocument.NUMBER_OF_INCOME_CATEGORIES));
        List<String> expenseCategoriesList = new ArrayList<>(defaultCategoriesList.subList(
                FinanceDocument.NUMBER_OF_INCOME_CATEGORIES,
                FinanceDocument.NUMBER_OF_CATEGORIES));


        for (Map.Entry<String, List<String>> entry : mCustomCategoriesMap.entrySet()) {
            if (entry.getValue().get(1).equals("0")) {
                incomeCategoriesList.add(entry.getValue().get(0));
            } else {
                expenseCategoriesList.add(entry.getValue().get(0));
            }
        }
        ArrayAdapter<CharSequence> categorySpinnerAdapter = new ArrayAdapter<>(weakContext.get(), android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.addAll(incomeCategoriesList);
        categorySpinnerAdapter.addAll(expenseCategoriesList);


        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(categorySpinnerAdapter);
        //noinspection ResourceType
        spinner.setId(categorySpinnerId);
        spinner.setSaveEnabled(true);

        if (weakActivity.get().findViewById(categorySpinnerId - 1) != null) {
            int prevPosition = ((Spinner) weakActivity.get().findViewById(categorySpinnerId - 1)).getSelectedItemPosition();
            if (prevPosition == FinanceDocument.NUMBER_OF_CATEGORIES + customCategoriesNumber - 1) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(prevPosition + 1);
            }

        }
        categorySpinnerId++;
        return spinner;
    }

    /**
     * Generates operation input value edit text
     *
     * @return edit text field
     */


    private EditText createNewEditText() {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(weakContext.get(), 40));
        final EditText editText = new EditText(weakContext.get());
        lparams.addRule(RelativeLayout.RIGHT_OF, categorySpinnerId - 1);

        if (categorySpinnerId > 1001) {

            lparams.addRule(RelativeLayout.BELOW, editTextValueId - 1);
        }

        int maxLength = 8;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(fArray);
        editText.setLayoutParams(lparams);
        editText.setHint(weakContext.get().getResources().getString(R.string.value));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        //noinspection ResourceType
        editText.setId(editTextValueId);
        editText.setSaveEnabled(true);
        editText.requestFocus();
        editTextValueId++;
        return editText;
    }

    /**
     * Generates delete button
     *
     * @return delete button
     */
    private ImageButton createNewDeleteButton() {
        final RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, Utils.dpToPx(weakContext.get(), 40));
        final ImageButton deleteButton = new ImageButton(weakContext.get());
        lparams.addRule(RelativeLayout.RIGHT_OF, currencySpinnerId - 1);
        deleteButton.setVisibility(View.INVISIBLE);
        if (deleteButtonId > 4001) {
            lparams.addRule(RelativeLayout.BELOW, deleteButtonId - 1);
            deleteButton.setVisibility(View.VISIBLE);
            weakActivity.get().findViewById(deleteButtonId - 1).setVisibility(View.INVISIBLE);
        }

        deleteButton.setLayoutParams(lparams);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        deleteButton.setImageResource(R.drawable.ic_remove_grey_24dp);
        //noinspection ResourceType
        deleteButton.setId(deleteButtonId);
        deleteButtonId++;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteButtonId - 2 != 4001) {
                    weakActivity.get().findViewById(deleteButtonId - 2).setVisibility(View.VISIBLE);
                }
                mLayout.removeView(weakActivity.get().findViewById(categorySpinnerId - 1));
                mLayout.removeView(weakActivity.get().findViewById(currencySpinnerId - 1));
                mLayout.removeView(weakActivity.get().findViewById(editTextValueId - 1));
                mLayout.removeView(weakActivity.get().findViewById(deleteButtonId - 1));
                categorySpinnerId--;
                currencySpinnerId--;
                editTextValueId--;
                deleteButtonId--;
                buttonCounter--;
                if (buttonCounter < FinanceDocument.NUMBER_OF_CATEGORIES + customCategoriesNumber - 1)
                    btnAddNewRow.show();
            }
        });

        return deleteButton;
    }

}
