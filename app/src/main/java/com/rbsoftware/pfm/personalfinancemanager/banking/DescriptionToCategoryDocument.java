package com.rbsoftware.pfm.personalfinancemanager.banking;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds methods for manageing links between descriptions and categories
 *
 * @author Roman Burzakovskiy
 */
public class DescriptionToCategoryDocument {
    public static final String DOC_TYPE = "DescriptionToCategoryDocument";
    public final static String DESCRIPTION_TO_CATEGORY_ID = "DescriptionToCategoryID";
    private DocumentRevision rev;
    private String userId;
    private String type;
    private String date;
    private HashMap<String, Integer> mDescriptionToCategoryMap = new HashMap<>();

    private DescriptionToCategoryDocument() {

    }

    /**
     * Creates DescriptionToCategoryDocument
     *
     * @param userId of current user
     * @param values hashmap of links description-category
     */
    public DescriptionToCategoryDocument(String userId, HashMap<String, Integer> values) {
        this.type = DOC_TYPE;
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);
        this.userId = userId;
        this.mDescriptionToCategoryMap = values;
    }

    /**
     * Gets hashmap of links
     *
     * @return hashmap of links
     */
    public HashMap<String, Integer> getDescriptionToCategoryMap() {
        return mDescriptionToCategoryMap;
    }

    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.putAll(mDescriptionToCategoryMap);
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
    public static DescriptionToCategoryDocument fromRevision(DocumentRevision rev) {
        DescriptionToCategoryDocument t = new DescriptionToCategoryDocument();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(DOC_TYPE)) {
            t.userId = (String) map.get("userId");
            t.date = (String) map.get("date");
            t.type = (String) map.get("type");

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!entry.getKey().equals("_id") && !entry.getKey().equals("_rev") && !entry.getKey().equals("userId") && !entry.getKey().equals("date") && !entry.getKey().equals("type"))
                    t.mDescriptionToCategoryMap.put(entry.getKey(), (Integer) entry.getValue());
            }

            return t;
        }
        return null;
    }

}
