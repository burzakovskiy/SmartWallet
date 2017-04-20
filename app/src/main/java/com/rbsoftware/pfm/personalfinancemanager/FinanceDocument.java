package com.rbsoftware.pfm.personalfinancemanager;


import android.util.SparseArray;

import com.cloudant.sync.datastore.DocumentRevision;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by burzakovskiy on 11/24/2015.
 * Holds structure of finance document
 */
@SuppressWarnings("unchecked")
// Suppressing unchecked cast: 'java.lang.Object' to 'java.util.ArrayList<java.lang.String>'
public class FinanceDocument {
    public static final String DOC_TYPE = "Finance document";
    private static final String TAG = "FinanceDocument";
    public static final String MAIN_ACCOUNT = "mainAccount";

    public static final int NUMBER_OF_CATEGORIES = 14;
    public static final int NUMBER_OF_INCOME_CATEGORIES = 5;
    public static final int NUMBER_OF_EXPENSE_CATEGORIES = 9;
    public static final int MAX_NUMBER_OF_CUSTOM_CATEGORIES = 5;

    public static final int CUSTOM_INCOME = 0;
    public static final int CUSTOM_EXPENSE = 1;

    public final static int PARAM_USERID = 0;
    public final static int PARAM_SALARY = 1;
    public final static int PARAM_RENTAL_INCOME = 2;
    public final static int PARAM_INTEREST = 3;
    public final static int PARAM_GIFTS = 4;
    public final static int PARAM_OTHER_INCOME = 5;
    public final static int PARAM_TAXES = 6;
    public final static int PARAM_MORTGAGE = 7;
    public final static int PARAM_CREDIT_CARD = 8;
    public final static int PARAM_UTILITIES = 9;
    public final static int PARAM_FOOD = 10;
    public final static int PARAM_CAR_PAYMENT = 11;
    public final static int PARAM_PERSONAL = 12;
    public final static int PARAM_ACTIVITIES = 13;
    public final static int PARAM_OTHER_EXPENSE = 14;


    private List<String> salary = new ArrayList<>();
    private List<String> rentalIncome = new ArrayList<>();
    private List<String> interest = new ArrayList<>();
    private List<String> gifts = new ArrayList<>();
    private List<String> otherIncome = new ArrayList<>();
    private List<String> taxes = new ArrayList<>();
    private List<String> mortgage = new ArrayList<>();
    private List<String> creditCard = new ArrayList<>();
    private List<String> utilities = new ArrayList<>();
    private List<String> food = new ArrayList<>();
    private List<String> carPayment = new ArrayList<>();
    private List<String> personal = new ArrayList<>();
    private List<String> activities = new ArrayList<>();
    private List<String> otherExpenses = new ArrayList<>();

    private HashMap<String, List<String>> customCategoriesMap = new HashMap<>();
    private DocumentRevision rev;
    private String date;
    private String account;
    private String type;
    private String userId;
    private String comments;

    private FinanceDocument() {
    }


    /**
     * Public constructor
     *
     * @param defaultParams array of default categories
     * @param customParams  map of custom categories
     *                      key - category id
     *                      value 0 - value
     *                      1 - currency
     *                      2 - recursion
     */
    public FinanceDocument(SparseArray<Object> defaultParams, HashMap<String, List<String>> customParams, String comments) {

        this.userId = (String) defaultParams.get(0);
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);

        initFinanceDocumentDefaultCategories(defaultParams);
        initFinanceDocumentCustomCategories(customParams);
        this.comments = comments;

    }

    /**
     * Publice constructor
     *
     * @param defaultParams array of default categories
     * @param customParams  customParams map of custom categories
     *                      key - category id
     *                      value 0 - value
     *                      1 - currency
     *                      2 - recursion
     * @param date          document date
     */
    public FinanceDocument(SparseArray<Object> defaultParams, HashMap<String, List<String>> customParams, String date, String comments) {

        this.userId = (String) defaultParams.get(0);
        this.date = date;

        initFinanceDocumentDefaultCategories(defaultParams);
        initFinanceDocumentCustomCategories(customParams);
        this.comments = comments;
    }

    /**
     * 1 - salary, 2  - rental income, 3 - interest, 4 - gifts, 5 - other income
     * 6 - taxes, 7 - mortgage, 8 - credit card,
     * 9 - utilities (Electric bill, Water bill, Gas bill, Phone bill, Internet service, Cable or satellite service),
     * 10 - food (Groceries, Dining out),
     * 11 - car payment (Fuel, Auto insurance, Tires and maintenance, Tag/registration),
     * 12 - personal (Clothing, Hair care, Medical expenses),
     * 13 - activities (Gym membership, Vacation, Charitable giving, Entertainment,Gifts),
     * 14 - other expenses
     */

    private void initFinanceDocumentDefaultCategories(SparseArray<Object> params) {
        this.setType(DOC_TYPE);
        this.setUserId(userId);
        this.setAccount(MainActivity.getActiveAccountId());
        this.setSalary((ArrayList<String>) params.get(1));
        this.setRentalIncome((ArrayList<String>) params.get(2));
        this.setInterest((ArrayList<String>) params.get(3));
        this.setGifts((ArrayList<String>) params.get(4));
        this.setOtherIncome((ArrayList<String>) params.get(5));
        this.setTaxes((ArrayList<String>) params.get(6));
        this.setMortgage((ArrayList<String>) params.get(7));
        this.setCreditCard((ArrayList<String>) params.get(8));
        this.setUtilities((ArrayList<String>) params.get(9));
        this.setFood((ArrayList<String>) params.get(10));
        this.setCarPayment((ArrayList<String>) params.get(11));
        this.setPersonal((ArrayList<String>) params.get(12));
        this.setActivities((ArrayList<String>) params.get(13));
        this.setOtherExpenses((ArrayList<String>) params.get(14));
    }


    /**
     * initializes custom categories
     *
     * @param customParams map of custom categories
     *                     key - category id
     *                     value 0 - value
     *                     1 - currency
     *                     2 - recursion
     */
    private void initFinanceDocumentCustomCategories(HashMap<String, List<String>> customParams) {
        this.customCategoriesMap = customParams;
    }


    //account
    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return this.account;
    }
    //type


    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    //data


    public String getuserId() {
        return userId;
    }

    private void setUserId(String data) {
        this.userId = data;
    }

    //salary
    public float getSalary() {
        if (salary == null) {
            return 0;
        } else if (salary.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(salary.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(salary.get(0)), salary.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setSalary(String salary, String currency, String recursion) {

        this.salary.add(0, salary);
        this.salary.add(1, currency);
        this.salary.add(2, recursion);
    }

    private void setSalary(ArrayList<String> salary) {

        this.salary = salary;
    }

    //rental income

    public float getRentalIncome() {
        if (rentalIncome == null) {
            return 0;
        } else if (rentalIncome.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(rentalIncome.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(rentalIncome.get(0)), rentalIncome.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setRentalIncome(String rentalIncome, String currency, String recursion) {
        this.rentalIncome.add(0, rentalIncome);
        this.rentalIncome.add(1, currency);
        this.rentalIncome.add(2, recursion);
    }

    private void setRentalIncome(ArrayList<String> rentalIncome) {
        this.rentalIncome = rentalIncome;
    }

    //interest

    public float getInterest() {
        if (interest == null) {
            return 0;
        } else if (interest.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(interest.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(interest.get(0)), interest.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setInterest(String interest, String currency, String recursion) {

        this.interest.add(0, interest);
        this.interest.add(1, currency);
        this.interest.add(2, recursion);
    }

    private void setInterest(ArrayList<String> interest) {

        this.interest = interest;
    }

    //gifts

    public float getGifts() {
        if (gifts == null) {
            return 0;
        } else if (gifts.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(gifts.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(gifts.get(0)), gifts.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setGifts(String gifts, String currency, String recursion) {

        this.gifts.add(0, gifts);
        this.gifts.add(1, currency);
        this.gifts.add(2, recursion);
    }

    private void setGifts(ArrayList<String> gifts) {

        this.gifts = gifts;
    }

    //other income

    public float getOtherIncome() {
        if (otherIncome == null) {
            return 0;
        } else if (otherIncome.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(otherIncome.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(otherIncome.get(0)), otherIncome.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setOtherIncome(String otherIncome, String currency, String recursion) {

        this.otherIncome.add(0, otherIncome);
        this.otherIncome.add(1, currency);
        this.otherIncome.add(2, recursion);
    }

    private void setOtherIncome(ArrayList<String> otherIncome) {

        this.otherIncome = otherIncome;
    }

    //7 - taxes

    public float getTaxes() {

        if (taxes == null) {
            return 0;
        } else if (taxes.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(taxes.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(taxes.get(0)), taxes.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setTaxes(String taxes, String currency, String recursion) {

        this.taxes.add(0, taxes);
        this.taxes.add(1, currency);
        this.taxes.add(2, recursion);
    }

    private void setTaxes(ArrayList<String> taxes) {

        this.taxes = taxes;
    }

    // 8 - mortgage

    public float getMortgage() {
        if (mortgage == null) {
            return 0;
        } else if (mortgage.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(mortgage.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(mortgage.get(0)), mortgage.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setMortgage(String mortgage, String currency, String recursion) {

        this.mortgage.add(0, mortgage);
        this.mortgage.add(1, currency);
        this.mortgage.add(2, recursion);
    }

    private void setMortgage(ArrayList<String> mortgage) {

        this.mortgage = mortgage;
    }


    // 9 - credit card
    public float getCreditCard() {
        if (creditCard == null) {
            return 0;
        } else if (creditCard.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(creditCard.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(creditCard.get(0)), creditCard.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setCreditCard(String creditCard, String currency, String recursion) {

        this.creditCard.add(0, creditCard);
        this.creditCard.add(1, currency);
        this.creditCard.add(2, recursion);
    }

    private void setCreditCard(ArrayList<String> creditCard) {

        this.creditCard = creditCard;
    }

    //10 - utilities

    public float getUtilities() {
        if (utilities == null) {
            return 0;
        } else if (utilities.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(utilities.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(utilities.get(0)), utilities.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setUtilities(String utilities, String currency, String recursion) {

        this.utilities.add(0, utilities);
        this.utilities.add(1, currency);
        this.utilities.add(2, recursion);
    }

    private void setUtilities(ArrayList<String> utilities) {

        this.utilities = utilities;
    }

    //11 - food

    public float getFood() {
        if (food == null) {
            return 0;
        } else if (food.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(food.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(food.get(0)), food.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setFood(String food, String currency, String recursion) {

        this.food.add(0, food);
        this.food.add(1, currency);
        this.food.add(2, recursion);
    }

    private void setFood(ArrayList<String> food) {

        this.food = food;
    }

    //12 - car payment

    public float getCarPayment() {
        if (carPayment == null) {
            return 0;
        } else if (carPayment.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(carPayment.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(carPayment.get(0)), carPayment.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setCarPayment(String carPayment, String currency, String recursion) {

        this.carPayment.add(0, carPayment);
        this.carPayment.add(1, currency);
        this.carPayment.add(2, recursion);
    }

    private void setCarPayment(ArrayList<String> carPayment) {

        this.carPayment = carPayment;
    }

    //13 - personal

    public float getPersonal() {
        if (personal == null) {
            return 0;
        } else if (personal.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(personal.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(personal.get(0)), personal.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setPersonal(String personal, String currency, String recursion) {

        this.personal.add(0, personal);
        this.personal.add(1, currency);
        this.personal.add(2, recursion);
    }

    private void setPersonal(ArrayList<String> personal) {

        this.personal = personal;
    }

    //14 - activities

    public float getActivities() {
        if (activities == null) {
            return 0;
        } else if (activities.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(activities.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(activities.get(0)), activities.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setActivities(String activities, String currency, String recursion) {

        this.activities.add(0, activities);
        this.activities.add(1, currency);
        this.activities.add(2, recursion);
    }

    private void setActivities(ArrayList<String> activities) {

        this.activities = activities;
    }
    //15 - other expenses

    public float getOtherExpenses() {
        if (otherExpenses == null) {
            return 0;
        } else if (otherExpenses.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(otherExpenses.get(0));
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(otherExpenses.get(0)), otherExpenses.get(1), MainActivity.defaultCurrency);
        }
    }

    public void setOtherExpenses(String otherExpenses, String currency, String recursion) {

        this.otherExpenses.add(0, otherExpenses);
        this.otherExpenses.add(1, currency);
        this.otherExpenses.add(2, recursion);
    }

    private void setOtherExpenses(ArrayList<String> otherExpenses) {

        this.otherExpenses = otherExpenses;
    }


    //date

    public String getDate() {
        return date;
    }

    private void setDate(String date) {
        this.date = date;
    }


    /**
     * Gets total income
     *
     * @return total income
     */

    public float getTotalIncome() {
        float totalIncome;
        totalIncome = getSalary() +
                getRentalIncome() +
                getInterest() +
                getGifts() +
                getOtherIncome();
        HashMap<String, Float> customIncomeCategories = getCustomCategoriesConvertedValuesMap().get(CUSTOM_INCOME);
        for (float value : customIncomeCategories.values()) {
            totalIncome += value;
        }

        return totalIncome;
    }


    /**
     * Gets total expense
     *
     * @return total expense
     */
    public float getTotalExpense() {
        float totalExpense;
        totalExpense = getTaxes() +
                getMortgage() +
                getCreditCard() +
                getUtilities() +
                getFood() +
                getCarPayment() +
                getPersonal() +
                getActivities() +
                getOtherExpenses();
        HashMap<String, Float> customExpenseCategories = getCustomCategoriesConvertedValuesMap().get(CUSTOM_EXPENSE);
        for (float value : customExpenseCategories.values()) {
            totalExpense += value;
        }
        return totalExpense;
    }


    /**
     * extracts data of FinanceDocument
     *
     * @return hashmap of data types and values
     */
    public HashMap<String, List<String>> getValuesMap() {

        HashMap<String, List<String>> mapSum = new HashMap<>();
        if (salary != null) {
            mapSum.put(String.valueOf(PARAM_SALARY), salary);
        }
        if (rentalIncome != null) {
            mapSum.put(String.valueOf(PARAM_RENTAL_INCOME), rentalIncome);
        }
        if (interest != null) {
            mapSum.put(String.valueOf(PARAM_INTEREST), interest);
        }
        if (gifts != null) {
            mapSum.put(String.valueOf(PARAM_GIFTS), gifts);
        }
        if (otherIncome != null) {
            mapSum.put(String.valueOf(PARAM_OTHER_INCOME), otherIncome);
        }
        if (taxes != null) {
            mapSum.put(String.valueOf(PARAM_TAXES), taxes);
        }
        if (mortgage != null) {
            mapSum.put(String.valueOf(PARAM_MORTGAGE), mortgage);
        }
        if (creditCard != null) {
            mapSum.put(String.valueOf(PARAM_CREDIT_CARD), creditCard);
        }
        if (utilities != null) {
            mapSum.put(String.valueOf(PARAM_UTILITIES), utilities);
        }
        if (food != null) {
            mapSum.put(String.valueOf(PARAM_FOOD), food);
        }
        if (carPayment != null) {
            mapSum.put(String.valueOf(PARAM_CAR_PAYMENT), carPayment);
        }
        if (personal != null) {
            mapSum.put(String.valueOf(PARAM_PERSONAL), personal);
        }
        if (activities != null) {
            mapSum.put(String.valueOf(PARAM_ACTIVITIES), activities);
        }
        if (otherExpenses != null) {
            mapSum.put(String.valueOf(PARAM_OTHER_EXPENSE), otherExpenses);
        }

        if (getCustomCategoriesValuesMap() != null) {
            if (getCustomCategoriesValuesMap().get(CUSTOM_INCOME) != null) {
                mapSum.putAll(getCustomCategoriesValuesMap().get(CUSTOM_INCOME));
            }
            if (getCustomCategoriesValuesMap().get(CUSTOM_EXPENSE) != null) {
                mapSum.putAll(getCustomCategoriesValuesMap().get(CUSTOM_EXPENSE));
            }
        }

        return mapSum;
    }

    /**
     * Generates map of custom categories values map
     * key - 0 - income
     * 1 - expense
     * value - key - category name
     * value - converted value
     *
     * @return custom categories  values map
     */
    public HashMap<Integer, HashMap<String, List<String>>> getCustomCategoriesValuesMap() {
        HashMap<Integer, HashMap<String, List<String>>> resultMap = new HashMap<>();
        /**
         * key - category id
         * value - 0 - value
         *         1 - currency
         *         2 - recursion
         */
        HashMap<String, List<String>> incomeMap = new HashMap<>();
        HashMap<String, List<String>> expenseMap = new HashMap<>();
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());

        /**
         * key - category id
         * value - 0 - category name
         *         1 - 0-income/1-expense
         */
        HashMap<String, List<String>> customMap = categoryDocument.getCategoriesMap();

        for (Map.Entry<String, List<String>> entry : customCategoriesMap.entrySet()) {
            //check if custom category id is in category document
            if (customMap.containsKey(entry.getKey())) {
                //check if custom category is income or expense
                if (customMap.get(entry.getKey()).get(1).equals("0")) {
                    incomeMap.put(entry.getKey(), entry.getValue());
                } else {
                    expenseMap.put(entry.getKey(), entry.getValue());

                }
            }
        }
        resultMap.put(CUSTOM_INCOME, incomeMap);
        resultMap.put(CUSTOM_EXPENSE, expenseMap);

        return resultMap;
    }

    public HashMap<Integer, HashMap<String, Float>> getConvertedValuesMap() {
        HashMap<Integer, HashMap<String, Float>> resultMap = new HashMap<>();
        /**
         * key - category name
         * value - 0 - value
         *         1 - currency
         *         2 - recursion
         */
        HashMap<String, Float> incomeMap = new HashMap<>();
        HashMap<String, Float> expenseMap = new HashMap<>();
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());

        /**
         * key - category id
         * value - 0 - category name
         *         1 - 0-income/1-expense
         */
        HashMap<String, List<String>> customMap = categoryDocument.getCategoriesMap();
        for (Map.Entry<String, List<String>> entry : customCategoriesMap.entrySet()) {
            //check if custom category id is in category document
            if (customMap.containsKey(entry.getKey())) {
                //check if custom category is income or expense
                if (customMap.get(entry.getKey()).get(1).equals("0")) {
                    incomeMap.put(entry.getKey(), convertToDefaultCurrency(entry.getValue().get(0), entry.getValue().get(1)));

                } else {
                    expenseMap.put(entry.getKey(), convertToDefaultCurrency(entry.getValue().get(0), entry.getValue().get(1)));

                }
            }

        }
        //putting default values to income map
        if (salary != null)
            incomeMap.put(String.valueOf(PARAM_SALARY), getSalary());
        if (rentalIncome != null)
            incomeMap.put(String.valueOf(PARAM_RENTAL_INCOME), getRentalIncome());
        if (interest != null)
            incomeMap.put(String.valueOf(PARAM_INTEREST), getInterest());
        if (gifts != null)
            incomeMap.put(String.valueOf(PARAM_GIFTS), getGifts());
        if (otherIncome != null)
            incomeMap.put(String.valueOf(PARAM_OTHER_INCOME), getOtherIncome());
        //putting default values to expense map

        if (taxes != null)
            expenseMap.put(String.valueOf(PARAM_TAXES), getTaxes());
        if (mortgage != null)
            expenseMap.put(String.valueOf(PARAM_MORTGAGE), getMortgage());
        if (creditCard != null)
            expenseMap.put(String.valueOf(PARAM_CREDIT_CARD), getCreditCard());
        if (utilities != null)
            expenseMap.put(String.valueOf(PARAM_UTILITIES), getUtilities());
        if (food != null)
            expenseMap.put(String.valueOf(PARAM_FOOD), getFood());
        if (carPayment != null)
            expenseMap.put(String.valueOf(PARAM_CAR_PAYMENT), getCarPayment());
        if (personal != null)
            expenseMap.put(String.valueOf(PARAM_PERSONAL), getPersonal());
        if (activities != null)
            expenseMap.put(String.valueOf(PARAM_ACTIVITIES), getActivities());
        if (otherExpenses != null)
            expenseMap.put(String.valueOf(PARAM_OTHER_EXPENSE), getOtherExpenses());

        resultMap.put(CUSTOM_INCOME, incomeMap);
        resultMap.put(CUSTOM_EXPENSE, expenseMap);

        return resultMap;
    }

    /**
     * Generates map of custom categories converted values map
     * key - 0 - income
     * 1 - expense
     * value - key - category name
     * value - converted value
     *
     * @return custom categories converted values map
     */
    public HashMap<Integer, HashMap<String, Float>> getCustomCategoriesConvertedValuesMap() {
        HashMap<Integer, HashMap<String, Float>> resultMap = new HashMap<>();
        /**
         * key - category name
         * value - 0 - value
         *         1 - currency
         *         2 - recursion
         */
        HashMap<String, Float> incomeMap = new HashMap<>();
        HashMap<String, Float> expenseMap = new HashMap<>();
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());

        /**
         * key - category id
         * value - 0 - category name
         *         1 - 0-income/1-expense
         */
        HashMap<String, List<String>> customMap = categoryDocument.getCategoriesMap();

        for (Map.Entry<String, List<String>> entry : customCategoriesMap.entrySet()) {
            //check if custom category id is in category document
            if (customMap.containsKey(entry.getKey())) {
                //check if custom category is income or expense
                if (customMap.get(entry.getKey()).get(1).equals("0")) {
                    incomeMap.put(customMap.get(entry.getKey()).get(0), convertToDefaultCurrency(entry.getValue().get(0), entry.getValue().get(1)));
                } else {
                    expenseMap.put(customMap.get(entry.getKey()).get(0), convertToDefaultCurrency(entry.getValue().get(0), entry.getValue().get(1)));

                }
            }
        }

        resultMap.put(CUSTOM_INCOME, incomeMap);
        resultMap.put(CUSTOM_EXPENSE, expenseMap);

        return resultMap;
    }

    /**
     * Gets custom category value by id
     *
     * @param id of category
     * @return converted category value
     */
    public float getCustomCategoryConvertedValue(String id) {
        List<String> customCategoryValue = customCategoriesMap.get(id);
        if (customCategoryValue != null && !customCategoryValue.isEmpty()) {
            return convertToDefaultCurrency(customCategoryValue.get(0), customCategoryValue.get(1));
        }
        return 0;
    }

    /**
     * Converts category value into default currency
     *
     * @param value    - of category
     * @param currency - currency of category
     * @return converted value
     */
    private float convertToDefaultCurrency(String value, String currency) {
        if (value == null || currency == null) return 0;
        else if (currency.equals(MainActivity.defaultCurrency)) {
            return Float.valueOf(value);
        } else {
            return CurrencyConversion.convertCurrency(Float.valueOf(value), currency, MainActivity.defaultCurrency);
        }

    }

    //comments

    /**
     * Gets document comments
     *
     * @return document comments
     */
    public String getComments() {
        if (comments == null) {
            return "";
        } else {
            return comments;
        }
    }

    /**
     * Sets document comments
     *
     * @param comments of document
     */
    public void setComments(String comments) {
        if (comments != null) {
            this.comments = comments;
        }
    }

    public DocumentRevision getDocumentRevision() {
        return rev;
    }

    /**
     * Creates finaince document from revision
     *
     * @param rev document revision
     * @return finance document
     */
    public static FinanceDocument fromRevision(DocumentRevision rev) {
        FinanceDocument t = new FinanceDocument();
        t.rev = rev;
        // this could also be done by a fancy object mapper
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(FinanceDocument.DOC_TYPE)) {
            t.setDate((String) map.get("date"));
            t.setType((String) map.get("type"));
            t.setUserId((String) map.get("userId"));
            t.setAccount((String) map.get("account"));
            t.setComments((String) map.get("comments"));
            t.setSalary((ArrayList<String>) map.get("salary"));
            t.setRentalIncome((ArrayList<String>) map.get("rentalIncome"));
            t.setInterest((ArrayList<String>) map.get("interest"));
            t.setGifts((ArrayList<String>) map.get("gifts"));
            t.setOtherIncome((ArrayList<String>) map.get("otherIncome"));
            t.setTaxes((ArrayList<String>) map.get("taxes"));
            t.setMortgage((ArrayList<String>) map.get("mortgage"));
            t.setCreditCard((ArrayList<String>) map.get("creditCard"));
            t.setUtilities((ArrayList<String>) map.get("utilities"));
            t.setFood((ArrayList<String>) map.get("food"));
            t.setCarPayment((ArrayList<String>) map.get("carPayment"));
            t.setPersonal((ArrayList<String>) map.get("personal"));
            t.setActivities((ArrayList<String>) map.get("activities"));
            t.setOtherExpenses((ArrayList<String>) map.get("otherExpenses"));

            //reading custom categories data
            CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                    CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
            HashMap<String, List<String>> customMap = categoryDocument.getCategoriesMap();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (customMap.containsKey(entry.getKey())) {
                    t.customCategoriesMap.put(entry.getKey(), (List<String>) entry.getValue());
                }
            }
            return t;
        }
        return null;
    }

    /**
     * Creates hash map of data types and values
     *
     * @return map of data types and values
     */
    public Map<String, Object> asMap() {
        // this could also be done by a fancy object mapper
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("userId", userId);
        map.put("account", account);
        map.put("comments", comments);
        if (salary != null && !salary.get(0).equals("0")) {
            map.put("salary", salary);
        }
        if (rentalIncome != null && !rentalIncome.get(0).equals("0")) {
            map.put("rentalIncome", rentalIncome);
        }
        if (interest != null && !interest.get(0).equals("0")) {
            map.put("interest", interest);
        }
        if (gifts != null && !gifts.get(0).equals("0")) {
            map.put("gifts", gifts);
        }
        if (otherIncome != null && !otherIncome.get(0).equals("0")) {
            map.put("otherIncome", otherIncome);
        }
        if (taxes != null && !taxes.get(0).equals("0")) {
            map.put("taxes", taxes);
        }
        if (mortgage != null && !mortgage.get(0).equals("0")) {
            map.put("mortgage", mortgage);
        }
        if (creditCard != null && !creditCard.get(0).equals("0")) {
            map.put("creditCard", creditCard);
        }
        if (utilities != null && !utilities.get(0).equals("0")) {
            map.put("utilities", utilities);
        }
        if (food != null && !food.get(0).equals("0")) {
            map.put("food", food);
        }
        if (carPayment != null && !carPayment.get(0).equals("0")) {
            map.put("carPayment", carPayment);
        }
        if (personal != null && !personal.get(0).equals("0")) {
            map.put("personal", personal);
        }
        if (activities != null && !activities.get(0).equals("0")) {
            map.put("activities", activities);
        }
        if (otherExpenses != null && !otherExpenses.get(0).equals("0")) {
            map.put("otherExpenses", otherExpenses);
        }

        if (customCategoriesMap != null && !customCategoriesMap.isEmpty()) {
            map.putAll(customCategoriesMap);
        }
        map.put("date", date);

        return map;
    }

}