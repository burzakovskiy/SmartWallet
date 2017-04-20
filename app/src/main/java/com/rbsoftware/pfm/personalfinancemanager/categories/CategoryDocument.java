package com.rbsoftware.pfm.personalfinancemanager.categories;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds methods for custom categories management
 *
 * @author Roman Burzakovskiy
 */
public class CategoryDocument {
    public static final String DOC_TYPE = "CategoryDocument";
    private static final String TAG = "CategoryDocument";
    public static final String CATEGORY_DOCUMENT_ID = "CategoryDocumentID";
    private DocumentRevision rev;
    private String type;
    private String date;
    private String userId;
    private HashMap<String, List<String>> mCategories = new HashMap<>();


    private CategoryDocument() {

    }

    /**
     * Public constructor of custom categories document
     *
     * @param userId of current user
     * @param values hashmap of custom categories
     *               key- random characters
     *               value - 0 category name
     *               1 income/expense
     */
    public CategoryDocument(String userId, HashMap<String, List<String>> values) {
        this.type = DOC_TYPE;
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);
        this.userId = userId;
        this.mCategories = values;
    }

    /**
     * Gets hashmap of custom categories
     *
     * @return map of custom categories
     */
    public HashMap<String, List<String>> getCategoriesMap() {
        return mCategories;
    }

    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.putAll(mCategories);
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
     * @return Custom category Document
     */
    public static CategoryDocument fromRevision(DocumentRevision rev) {
        CategoryDocument t = new CategoryDocument();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(DOC_TYPE)) {
            t.userId = (String) map.get("userId");
            t.date = (String) map.get("date");
            t.type = (String) map.get("type");

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!entry.getKey().equals("_id") && !entry.getKey().equals("_rev") && !entry.getKey().equals("userId") && !entry.getKey().equals("date") && !entry.getKey().equals("type"))
                    //noinspection unchecked
                    t.mCategories.put(entry.getKey(), (ArrayList<String>) entry.getValue());
            }

            return t;
        }
        return null;
    }


}
