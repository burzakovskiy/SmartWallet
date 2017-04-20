package com.rbsoftware.pfm.personalfinancemanager.budget;

import com.cloudant.sync.datastore.DocumentRevision;
import com.rbsoftware.pfm.personalfinancemanager.CurrencyConversion;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds method for managing budget document
 *
 * @author Roman Burzakovskiy
 */
@SuppressWarnings("unchecked")
public class BudgetDocument {
    public static final String DOC_TYPE = "Budget Document";
    public static final String PERIOD_WEEKLY = "weekly";
    public static final String PERIOD_MONTHLY = "monthly";
    /**
     * Revision of document
     */
    private DocumentRevision rev;

    /**
     * Current user id
     */
    private String userId;
    /**
     * Document type
     */
    private String type;
    /**
     * Budget name
     */
    private String name;
    /**
     * Budget value 0- value, 1-currency
     */
    private ArrayList<String> value = new ArrayList<>();
    /**
     * is budget active
     */
    private boolean isActive;
    /**
     * document date
     */
    private String date;
    /**
     * document account
     */
    private String account;
    /**
     * Budget period weekly/monthly
     */
    private String period;

    private BudgetDocument() {

    }

    /**
     * Constructor of budget document
     *
     * @param userId   id of current user
     * @param period   of budget
     * @param name     of budget
     * @param value    of budget 0-value, 1- currency
     * @param isActive status of budget
     */
    public BudgetDocument(String userId, String period, String name, ArrayList<String> value, boolean isActive) {
        this.userId = userId;
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);
        this.type = DOC_TYPE;
        this.name = name;
        this.value = value;
        this.isActive = isActive;
        this.setAccount(MainActivity.getActiveAccountId());
        this.period = period;

    }

    /**
     * Gets budget name
     *
     * @return budget name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets budget name
     *
     * @param name of budget
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets budget period
     *
     * @return budget period
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Sets budget period
     *
     * @param period of budget
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * Gets converted into default currency budget value
     *
     * @return budget value
     */
    public float getConvertedValue() {

        if (value == null) {
            return 0;
        } else if (value.get(1).equals(MainActivity.defaultCurrency)) {

            return Float.valueOf(value.get(0));
        } else {

            return CurrencyConversion.convertCurrency(Float.valueOf(value.get(0)), value.get(1), MainActivity.defaultCurrency);
        }
    }

    /**
     * Gets unconverted budget value
     *
     * @return unconverted budget value
     */
    public float getValue() {
        if (value == null) {
            return 0;
        } else return Float.valueOf(value.get(0));
    }

    /**
     * Gets budget currency
     *
     * @return budget currency
     */
    public String getCurrency() {
        if (value != null) {
            return value.get(1);
        } else {
            return "USD";
        }
    }

    /**
     * Sets budget value
     *
     * @param value of budget
     */
    public void setValue(ArrayList<String> value) {
        this.value = value;
    }


    /**
     * Gets budget status
     *
     * @return true if budget is active
     */
    public boolean getActive() {
        return isActive;
    }

    /**
     * Sets budget status
     *
     * @param isActive budget status
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Sets budget date
     *
     * @param date of budget creation or update
     */
    private void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets budget date
     *
     * @return budget date in unix format
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets document type of budget document
     *
     * @param type of document
     */
    private void setType(String type) {
        this.type = type;
    }

    public String getuserId() {
        return userId;
    }

    /**
     * Sets users account to document
     *
     * @param account of user
     */
    private void setAccount(String account) {
        this.account = account;
    }

    private String getAccount() {
        return this.account;
    }

    /**
     * Sets userid to document
     *
     * @param data of user
     */
    private void setUserId(String data) {
        this.userId = data;
    }

    /**
     * Builds map of budget document values
     *
     * @return map of values
     */
    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("userId", userId);
        map.put("account", account);
        map.put("date", date);
        map.put("name", name);
        map.put("value", value);
        map.put("isActive", isActive);
        map.put("period", period);

        return map;
    }

    /**
     * Gets document revision
     *
     * @return revision of document
     */
    public DocumentRevision getDocumentRevision() {
        return rev;
    }

    /**
     * Creates document from revision
     *
     * @param rev document revision
     * @return budget document
     */
    public static BudgetDocument fromRevision(DocumentRevision rev) {
        BudgetDocument t = new BudgetDocument();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(DOC_TYPE)) {
            t.setUserId((String) map.get("userId"));
            t.setDate((String) map.get("date"));
            t.setType((String) map.get("type"));
            t.setAccount((String) map.get("account"));
            t.setName((String) map.get("name"));
            t.setPeriod((String) map.get("period"));
            t.setValue((ArrayList<String>) map.get("value"));
            t.setActive((boolean) map.get("isActive"));
            return t;
        }
        return null;
    }


}
