package com.rbsoftware.pfm.personalfinancemanager.goals;

import com.cloudant.sync.datastore.DocumentRevision;
import com.rbsoftware.pfm.personalfinancemanager.CurrencyConversion;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds mthod for goal document
 *
 * @author Roman Burzakovskiy
 */
public class GoalDocument {
    public static final String DOC_TYPE = "Goal Document";
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_HIGH = 2;
    /**
     * Document revision
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
     * Goal name
     */
    private String name;
    /**
     * Goal value 0 -value, 1- currency
     */
    private ArrayList<String> value = new ArrayList<>();
    /**
     * goal priority
     */
    private int priority;
    /**
     * Document date
     */
    private String date;
    /**
     * users account
     */
    private String account;
    /**
     * Goal image name
     */
    private String imageName;

    private GoalDocument() {

    }

    /**
     * public goal document constructor
     *
     * @param userId    of current user
     * @param name      of goal
     * @param value     of goal 0-value, 1- currency
     * @param priority  of goal
     * @param imageName name of goal image
     */
    public GoalDocument(String userId, String name, ArrayList<String> value, int priority, String imageName) {
        this.userId = userId;
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);
        this.type = DOC_TYPE;
        this.name = name;
        this.value = value;
        this.priority = priority;
        this.setAccount(MainActivity.getActiveAccountId());
        this.imageName = imageName;
    }

    /**
     * Gets goal name
     *
     * @return goal name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets goal name
     *
     * @param name of goal
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets goal converted into default currency value value
     *
     * @return converted goal value
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
     * Gets unconverted goal value
     *
     * @return unconverted goal value
     */
    public float getValue() {
        if (value == null) {
            return 0;
        } else return Float.valueOf(value.get(0));
    }

    /**
     * Gets goal currency
     *
     * @return goal currency
     */
    public String getCurrency() {
        if (value != null) {
            return value.get(1);
        } else {
            return "USD";
        }
    }

    /**
     * Sets goal value
     *
     * @param value of goal
     */
    public void setValue(ArrayList<String> value) {
        this.value = value;
    }

    /**
     * Sets goal date
     *
     * @param date of goal creation or update
     */
    private void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets goal date
     *
     * @return goal date in unix format
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets document type of goal document
     *
     * @param type of document
     */
    private void setType(String type) {
        this.type = type;
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

    public String getuserId() {
        return userId;
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
     * Sets priority level to goal document
     *
     * @param priority level of priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets level of priority
     *
     * @return level of priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets attached image name
     *
     * @return image name
     */
    public String getImageName() {

        return imageName;

    }

    /**
     * Sets image nmae to to attachment
     *
     * @param imageName image name
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Builds map of goal document values
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
        map.put("priority", priority);
        map.put("imageName", imageName);

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
     * @return goal document
     */
    public static GoalDocument fromRevision(DocumentRevision rev) {
        GoalDocument t = new GoalDocument();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(DOC_TYPE)) {
            t.setUserId((String) map.get("userId"));
            t.setDate((String) map.get("date"));
            t.setType((String) map.get("type"));
            t.setAccount((String) map.get("account"));
            t.setName((String) map.get("name"));
            t.setPriority((int) map.get("priority"));
            t.setImageName((String) map.get("imageName"));
            //noinspection unchecked
            t.setValue((ArrayList<String>) map.get("value"));
            return t;
        }
        return null;
    }
}
