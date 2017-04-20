package com.rbsoftware.pfm.personalfinancemanager.accounts;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds methods for account document management.
 *
 * @author Roman Burzakovskiy
 */
public class AccountDocument {

    public static final String DOC_TYPE = "AccountDocument";
    private static final String TAG = "AccountDocument";
    public static final String ACCOUNT_DOCUMENT_ID = "AccountDocumentID";
    private DocumentRevision rev;
    private String date;
    private String type;
    private String userId;
    private HashMap<String, List<String>> mAccounts = new HashMap<>();

    private AccountDocument() {

    }

    /**
     * Public account document constructor
     *
     * @param userId current user id
     * @param values list of account properties
     *               0- account name
     */
    public AccountDocument(String userId, HashMap<String, List<String>> values) {

        this.type = DOC_TYPE;
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);
        this.userId = userId;
        this.mAccounts = values;
    }

    /**
     * Get accounts map
     *
     * @return accounts hash map
     */
    public HashMap<String, List<String>> getAccountsMap() {
        return mAccounts;
    }

    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.putAll(mAccounts);
        map.put("type", type);
        map.put("userId", userId);
        map.put("date", date);


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
     * @return Description To Category Document
     */
    public static AccountDocument fromRevision(DocumentRevision rev) {
        AccountDocument t = new AccountDocument();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(DOC_TYPE)) {
            t.userId = (String) map.get("userId");
            t.date = (String) map.get("date");
            t.type = (String) map.get("type");

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!entry.getKey().equals("_id") && !entry.getKey().equals("_rev") && !entry.getKey().equals("userId") && !entry.getKey().equals("date") && !entry.getKey().equals("type"))
                    //noinspection unchecked
                    t.mAccounts.put(entry.getKey(), (ArrayList<String>) entry.getValue());
            }

            return t;
        }
        return null;
    }

}
