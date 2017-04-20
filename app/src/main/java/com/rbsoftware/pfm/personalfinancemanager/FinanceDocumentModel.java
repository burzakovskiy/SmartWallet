package com.rbsoftware.pfm.personalfinancemanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cloudant.sync.datastore.Attachment;
import com.cloudant.sync.datastore.ConflictException;
import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentNotFoundException;
import com.cloudant.sync.datastore.DocumentRevision;
import com.cloudant.sync.datastore.UnsavedFileAttachment;
import com.cloudant.sync.query.IndexManager;
import com.cloudant.sync.query.QueryResult;
import com.rbsoftware.pfm.personalfinancemanager.accounts.AccountDocument;
import com.rbsoftware.pfm.personalfinancemanager.banking.BankingCardDocument;
import com.rbsoftware.pfm.personalfinancemanager.banking.DescriptionToCategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.budget.BudgetDocument;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;
import com.rbsoftware.pfm.personalfinancemanager.goals.GoalDocument;
import com.rbsoftware.pfm.personalfinancemanager.utils.DateUtils;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by burzakovskiy on 11/24/2015.
 * Holds methods for CRUD and querying finance documents
 **/
public class FinanceDocumentModel {

    private static final String TAG = "FinanceDocumentModel";
    private static final String DATASTORE_MANGER_DIR = "data";
    private static final String DOCUMENT_DATASTORE = "documents";
    private static final String FINANCE_DOCUMENT_INDEX_LIST = "FinanceDocumentIndexList";


    private Calendar cal;
    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";
    private Datastore mDatastore;
    private IndexManager im;


    public FinanceDocumentModel(Context context) {

        // Set up our tasks datastore within its own folder in the applications
        // data directory.
        File path = context.getApplicationContext().getDir(
                DATASTORE_MANGER_DIR,
                Context.MODE_PRIVATE
        );
        DatastoreManager manager = new DatastoreManager(path.getAbsolutePath());
        try {
            this.mDatastore = manager.openDatastore(DOCUMENT_DATASTORE);
        } catch (DatastoreNotCreatedException dnce) {
            Log.e(TAG, "Unable to open Datastore", dnce);
        }


    }


    public Datastore getDatastore() {
        return mDatastore;
    }

    /**
     * Set index manager
     **/

    public void setIndexManager() {
        im = new IndexManager(mDatastore);
        List<Object> indexList = new ArrayList<>();
        indexList.add("type");
        indexList.add("userId");
        indexList.add("date");
        indexList.add("account");

        im.ensureIndexed(indexList, FINANCE_DOCUMENT_INDEX_LIST);
    }

    /**
     * Gets index manager
     *
     * @return index manager
     */
    public IndexManager getIndexManager() {
        return im;
    }


    // Document query methods

    /**
     * Queries all banking cards
     *
     * @param userId  of current user
     * @param docType of banking card document
     * @return list of banking cards
     */
    public List<BankingCardDocument> queryBankingCardDocuments(String userId, String docType) {
        List<BankingCardDocument> list = new ArrayList<>();
        Map<String, Object> query = new HashMap<>();

        Map<String, Object> eqUserId = new HashMap<>();       //Query by userId
        Map<String, Object> userIdClause = new HashMap<>();               //*
        eqUserId.put("$eq", userId);                                       //*
        userIdClause.put("userId", eqUserId);                              //**********************

        Map<String, Object> eqType = new HashMap<>();       //Query by type
        Map<String, Object> typeClause = new HashMap<>();               //*
        eqType.put("$eq", docType);                                       //*
        typeClause.put("type", eqType);                              //**********************


        query.put("$and", Arrays.<Object>asList(userIdClause, typeClause));

        QueryResult result = im.find(query);
        if (result != null) {
            for (DocumentRevision rev : result) {
                list.add(getBankingCardDocument(rev.getId()));

            }

        }
        return list;

    }

    /**
     * Queries finance documents by time period
     *
     * @param timeFrame "thisWeek"
     *                  "lastWeek"
     *                  "threeWeeks"
     *                  "thisMonth"
     *                  "lastMonth"
     *                  "threeMonths"
     *                  "Jan" - "Dec"
     *                  "thisYear"
     * @param userId    id of current user
     * @param docType   document type
     * @param account   current active account
     * @return list of the documents
     **/


    public List<FinanceDocument> queryFinanceDocumentsByDate(String timeFrame, String userId, String docType, String account) {
        List<FinanceDocument> list = new ArrayList<>();
        cal = Calendar.getInstance();
        long currDate = cal.getTimeInMillis() / 1000;
        Map<String, Object> query = new HashMap<>();

        Map<String, Object> gteDate = new HashMap<>();                    // Start of the period
        Map<String, Object> startClause = new HashMap<>();                //*
        gteDate.put("$gte", startDateBuilder(currDate, timeFrame));        //*
        startClause.put("date", gteDate);
        //*********
        Map<String, Object> lteDate = new HashMap<>();      // End of t/he period
        Map<String, Object> endClause = new HashMap<>();                 // *
        lteDate.put("$lte", endDateBuilder(currDate, timeFrame));         //*
        endClause.put("date", lteDate);                                   //*********

        Map<String, Object> eqUserId = new HashMap<>();       //Query by userId
        Map<String, Object> userIdClause = new HashMap<>();               //*
        eqUserId.put("$eq", userId);                                       //*
        userIdClause.put("userId", eqUserId);                              //**********************

        Map<String, Object> eqType = new HashMap<>();       //Query by type
        Map<String, Object> typeClause = new HashMap<>();               //*
        eqType.put("$eq", docType);                                       //*
        typeClause.put("type", eqType);                              //**********************

        Map<String, Object> eqAccount = new HashMap<>();       //Query by account
        Map<String, Object> accountClause = new HashMap<>();               //*
        eqAccount.put("$eq", account);                                       //*
        accountClause.put("account", eqAccount);                              //**********************


        query.put("$and", Arrays.<Object>asList(startClause, endClause, userIdClause, typeClause, accountClause)); //query


        QueryResult result = im.find(query);
        if (result != null) {
            for (DocumentRevision rev : result) {
                list.add(getFinanceDocument(rev.getId()));

                // The returned revision object contains all fields for
                // the object. You cannot project certain fields in the
                // current implementation.
            }

        }
        return list;
    }

    /**
     * Queries finance documents by time period and sort
     *
     * @param timeFrame "thisWeek"
     *                  "lastWeek"
     *                  "threeWeeks"
     *                  "thisMonth"
     *                  "lastMonth"
     *                  "threeMonths"
     *                  "Jan" - "Dec"
     *                  "thisYear"
     * @param userId    id of current user
     * @param docType   document type
     * @param order     asc or desc
     * @param account   current active account
     * @return list of the documents
     **/

    public List<FinanceDocument> queryFinanceDocumentsByDate(String timeFrame, String userId, String docType, String order, String account) {
        List<FinanceDocument> list = new ArrayList<>();
        cal = Calendar.getInstance();
        long currDate = cal.getTimeInMillis() / 1000;
        Map<String, Object> query = new HashMap<>();

        Map<String, Object> gteDate = new HashMap<>();                    // Start of the period
        Map<String, Object> startClause = new HashMap<>();                //*
        gteDate.put("$gte", startDateBuilder(currDate, timeFrame));        //*
        startClause.put("date", gteDate);
        //*********
        Map<String, Object> lteDate = new HashMap<>();      // End of t/he period
        Map<String, Object> endClause = new HashMap<>();                 // *
        lteDate.put("$lte", endDateBuilder(currDate, timeFrame));         //*
        endClause.put("date", lteDate);                                   //*********
        Map<String, Object> eqUserId = new HashMap<>();       //Query by userId
        Map<String, Object> userIdClause = new HashMap<>();               //*
        eqUserId.put("$eq", userId);                                       //*
        userIdClause.put("userId", eqUserId);                              //**********************


        Map<String, Object> eqType = new HashMap<>();       //Query by type
        Map<String, Object> typeClause = new HashMap<>();               //*
        eqType.put("$eq", docType);                                       //*
        typeClause.put("type", eqType);                              //**********************

        Map<String, Object> eqAccount = new HashMap<>();       //Query by account
        Map<String, Object> accountClause = new HashMap<>();               //*
        eqAccount.put("$eq", account);                                       //*
        accountClause.put("account", eqAccount);                              //**********************


        query.put("$and", Arrays.<Object>asList(startClause, endClause, userIdClause, typeClause, accountClause)); //query

        //Sorting documents
        List<Map<String, String>> sortDocument = new ArrayList<>();
        Map<String, String> sortByDate = new HashMap<>();
        if (order.equals(ORDER_ASC)) {

            sortByDate.put("date", "asc");  //sorting by date

        } else {
            sortByDate.put("date", "desc");  //sorting by date
        }
        sortDocument.add(sortByDate);
        QueryResult result = im.find(query, 0, 0, null, sortDocument);
        if (result != null) {
            for (DocumentRevision rev : result) {
                list.add(getFinanceDocument(rev.getId()));

                // The returned revision object contains all fields for
                // the object. You cannot project certain fields in the
                // current implementation.
            }

        }
        return list;
    }

    /**
     * Queries finance documents by time period and sort
     *
     * @param startDate start date of range
     * @param endDate   end date of range
     * @param userId    id of current user
     * @param docType   document type
     * @param order     asc or desc
     * @param account   current active account
     * @return list of the documents
     **/

    public List<FinanceDocument> queryFinanceDocumentsByDateRange(long startDate, long endDate, String userId, String docType, String order, String account) {
        List<FinanceDocument> list = new ArrayList<>();

        Map<String, Object> query = new HashMap<>();

        Map<String, Object> gteDate = new HashMap<>();                    // Start of the period
        Map<String, Object> startClause = new HashMap<>();                //*
        gteDate.put("$gte", startDate);        //*
        startClause.put("date", gteDate);
        //*********
        Map<String, Object> lteDate = new HashMap<>();      // End of t/he period
        Map<String, Object> endClause = new HashMap<>();                 // *
        lteDate.put("$lte", endDate);         //*
        endClause.put("date", lteDate);                                   //*********
        Map<String, Object> eqUserId = new HashMap<>();       //Query by userId
        Map<String, Object> userIdClause = new HashMap<>();               //*
        eqUserId.put("$eq", userId);                                       //*
        userIdClause.put("userId", eqUserId);                              //**********************


        Map<String, Object> eqType = new HashMap<>();       //Query by type
        Map<String, Object> typeClause = new HashMap<>();               //*
        eqType.put("$eq", docType);                                       //*
        typeClause.put("type", eqType);                              //**********************

        Map<String, Object> eqAccount = new HashMap<>();       //Query by account
        Map<String, Object> accountClause = new HashMap<>();               //*
        eqAccount.put("$eq", account);                                       //*
        accountClause.put("account", eqAccount);                              //**********************


        query.put("$and", Arrays.<Object>asList(startClause, endClause, userIdClause, typeClause, accountClause)); //query

        //Sorting documents
        List<Map<String, String>> sortDocument = new ArrayList<>();
        Map<String, String> sortByDate = new HashMap<>();
        if (order.equals(ORDER_ASC)) {

            sortByDate.put("date", "asc");  //sorting by date

        } else {
            sortByDate.put("date", "desc");  //sorting by date
        }
        sortDocument.add(sortByDate);
        QueryResult result = im.find(query, 0, 0, null, sortDocument);
        if (result != null) {
            for (DocumentRevision rev : result) {
                list.add(getFinanceDocument(rev.getId()));

                // The returned revision object contains all fields for
                // the object. You cannot project certain fields in the
                // current implementation.
            }

        }
        return list;
    }

    /**
     * Queries budget documents by time period and sort
     *
     * @param timeFrame "thisWeek"
     *                  "lastWeek"
     *                  "threeWeeks"
     *                  "thisMonth"
     *                  "lastMonth"
     *                  "threeMonths"
     *                  "Jan" - "Dec"
     *                  "thisYear"
     * @param userId    id of current user
     * @param docType   document type
     * @param order     asc or desc
     * @param account   current active account
     * @return list of the documents
     **/

    public List<BudgetDocument> queryBudgetDocumentsByDate(String timeFrame, String userId, String docType, String order, String account) {
        List<BudgetDocument> list = new ArrayList<>();
        cal = Calendar.getInstance();
        long currDate = cal.getTimeInMillis() / 1000;
        Map<String, Object> query = new HashMap<>();

        Map<String, Object> gteDate = new HashMap<>();                    // Start of the period
        Map<String, Object> startClause = new HashMap<>();                //*
        gteDate.put("$gte", startDateBuilder(currDate, timeFrame));        //*
        startClause.put("date", gteDate);
        //*********
        Map<String, Object> lteDate = new HashMap<>();      // End of t/he period
        Map<String, Object> endClause = new HashMap<>();                 // *
        lteDate.put("$lte", endDateBuilder(currDate, timeFrame));         //*
        endClause.put("date", lteDate);                                   //*********
        Map<String, Object> eqUserId = new HashMap<>();       //Query by userId
        Map<String, Object> userIdClause = new HashMap<>();               //*
        eqUserId.put("$eq", userId);                                       //*
        userIdClause.put("userId", eqUserId);                              //**********************


        Map<String, Object> eqType = new HashMap<>();       //Query by type
        Map<String, Object> typeClause = new HashMap<>();               //*
        eqType.put("$eq", docType);                                       //*
        typeClause.put("type", eqType);                              //**********************


        Map<String, Object> eqAccount = new HashMap<>();       //Query by account
        Map<String, Object> accountClause = new HashMap<>();               //*
        eqAccount.put("$eq", account);                                       //*
        accountClause.put("account", eqAccount);                              //**********************

        query.put("$and", Arrays.<Object>asList(startClause, endClause, userIdClause, typeClause, accountClause)); //query

        //Sorting documents
        List<Map<String, String>> sortDocument = new ArrayList<>();
        Map<String, String> sortByDate = new HashMap<>();
        if (order.equals(ORDER_ASC)) {

            sortByDate.put("date", "asc");  //sorting by date

        } else {
            sortByDate.put("date", "desc");  //sorting by date
        }
        sortDocument.add(sortByDate);
        QueryResult result = im.find(query, 0, 0, null, sortDocument);
        if (result != null) {
            for (DocumentRevision rev : result) {
                list.add(getBudgetDocument(rev.getId()));

                // The returned revision object contains all fields for
                // the object. You cannot project certain fields in the
                // current implementation.
            }

        }
        return list;
    }


    /**
     * Queries goal documents by time period and sort
     *
     * @param timeFrame "thisWeek"
     *                  "lastWeek"
     *                  "threeWeeks"
     *                  "thisMonth"
     *                  "lastMonth"
     *                  "threeMonths"
     *                  "Jan" - "Dec"
     *                  "thisYear"
     * @param userId    id of current user
     * @param docType   document type
     * @param order     asc or desc
     * @param account   current active account
     * @return list of the documents
     **/

    public List<GoalDocument> queryGoalDocumentsByDate(String timeFrame, String userId, String docType, String order, String account) {
        List<GoalDocument> list = new ArrayList<>();
        cal = Calendar.getInstance();
        long currDate = cal.getTimeInMillis() / 1000;
        Map<String, Object> query = new HashMap<>();

        Map<String, Object> gteDate = new HashMap<>();                    // Start of the period
        Map<String, Object> startClause = new HashMap<>();                //*
        gteDate.put("$gte", startDateBuilder(currDate, timeFrame));        //*
        startClause.put("date", gteDate);
        //*********
        Map<String, Object> lteDate = new HashMap<>();      // End of t/he period
        Map<String, Object> endClause = new HashMap<>();                 // *
        lteDate.put("$lte", endDateBuilder(currDate, timeFrame));         //*
        endClause.put("date", lteDate);                                   //*********
        Map<String, Object> eqUserId = new HashMap<>();       //Query by userId
        Map<String, Object> userIdClause = new HashMap<>();               //*
        eqUserId.put("$eq", userId);                                       //*
        userIdClause.put("userId", eqUserId);                              //**********************


        Map<String, Object> eqType = new HashMap<>();       //Query by type
        Map<String, Object> typeClause = new HashMap<>();               //*
        eqType.put("$eq", docType);                                       //*
        typeClause.put("type", eqType);                              //**********************

        Map<String, Object> eqAccount = new HashMap<>();       //Query by account
        Map<String, Object> accountClause = new HashMap<>();               //*
        eqAccount.put("$eq", account);                                       //*
        accountClause.put("account", eqAccount);                              //**********************


        query.put("$and", Arrays.<Object>asList(startClause, endClause, userIdClause, typeClause, accountClause)); //query

        //Sorting documents
        List<Map<String, String>> sortDocument = new ArrayList<>();
        Map<String, String> sortByDate = new HashMap<>();
        if (order.equals(ORDER_ASC)) {

            sortByDate.put("date", "asc");  //sorting by date

        } else {
            sortByDate.put("date", "desc");  //sorting by date
        }
        sortDocument.add(sortByDate);
        QueryResult result = im.find(query, 0, 0, null, sortDocument);
        if (result != null) {
            for (DocumentRevision rev : result) {
                list.add(getGoalDocument(rev.getId()));

                // The returned revision object contains all fields for
                // the object. You cannot project certain fields in the
                // current implementation.
            }

        }
        return list;
    }

    /**
     * Calculates start date for query
     *
     * @param currDate  current date in milliseconds
     * @param timeFrame time frame
     * @return start date for query
     */
    private long startDateBuilder(long currDate, String timeFrame) {
        switch (timeFrame) {
            case DateUtils.THIS_WEEK:
                return DateUtils.getFirstDateOfCurrentWeek();

            case DateUtils.THIS_MONTH:
                return DateUtils.getFirstDateOfCurrentMonth();

            case DateUtils.THIS_YEAR:
                return DateUtils.getFirstDateOfCurrentYear();

            case DateUtils.LAST_WEEK:
                return DateUtils.getFirstDateOfPreviousWeek();

            case DateUtils.THREE_WEEKS:
                return DateUtils.getFirstDateOfTwoWeeksAgo();

            case DateUtils.LAST_MONTH:
                return DateUtils.getFirstDateOfPreviousMonth();

            case DateUtils.THREE_MONTHS:
                return DateUtils.getFirstDateOfTwoMonthsAgo();

            case DateUtils.FROM_START:
                return DateUtils.getStartDate();

            default:
                return 0;

        }
    }

    /**
     * Calculates end date for query
     *
     * @param currDate  current date in milliseconds
     * @param timeFrame time frame
     * @return end date for query
     */
    private long endDateBuilder(long currDate, String timeFrame) {


        switch (timeFrame) {
            case DateUtils.THIS_WEEK:
                return currDate;
            case DateUtils.THIS_MONTH:
                return currDate;
            case DateUtils.THIS_YEAR:
                return currDate;
            case DateUtils.LAST_WEEK:
                return DateUtils.getLastDateOfPreviousWeek();
            case DateUtils.THREE_WEEKS:
                return currDate;
            case DateUtils.LAST_MONTH:
                return DateUtils.getLastDateOfPreviousMonth();
            case DateUtils.THREE_MONTHS:
                return currDate;
            case DateUtils.FROM_START:
                return currDate;
            default:
                return currDate;

        }


    }


    //
    // DOCUMENT CRUD
    //

    /**
     * Creates a finance document, assigning an ID.
     *
     * @param document task to create
     * @return new revision of the document
     **/
    public FinanceDocument createDocument(FinanceDocument document) {
        DocumentRevision rev = new DocumentRevision();

        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return FinanceDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a currency document, assigning an ID.
     *
     * @param document task to create
     * @return new revision of the document
     */
    public Currency createDocument(Currency document) {
        DocumentRevision rev = new DocumentRevision(Currency.CURRENCY_ID);
        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return Currency.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a budget document, assigning an ID.
     *
     * @param document budget to create
     * @return new revision of the document
     */
    public BudgetDocument createDocument(BudgetDocument document) {
        DocumentRevision rev = new DocumentRevision();

        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return BudgetDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a goal document, assigning an ID.
     *
     * @param document goal to create
     * @return new revision of the document
     */
    public GoalDocument createDocument(GoalDocument document, String imageUri) {
        DocumentRevision rev = new DocumentRevision();

        rev.setBody(DocumentBodyFactory.create(document.asMap()));

        if (imageUri != null) {
            // create an UnsavedFileAttachment: the constructor takes
            // a File object on disk and a MIME type
            UnsavedFileAttachment att1 = new UnsavedFileAttachment(
                    new File(imageUri), "image/jpeg");

            // As with the document body, you can replace the attachments
            rev.setAttachments(new HashMap<String, Attachment>());

            // Or just add or update a single one:
            // (because the getter will always return the underlying map and not a copy)
            rev.getAttachments().put(att1.name, att1);
        }
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return GoalDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a banking card document, assigning an ID.
     *
     * @param document budget to create
     * @return new revision of the document
     */
    public BankingCardDocument createDocument(BankingCardDocument document) {
        DocumentRevision rev = new DocumentRevision();

        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return BankingCardDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a description to category document, assigning an ID.
     *
     * @param document description to category to create
     * @return new revision of the document
     */
    public DescriptionToCategoryDocument createDocument(DescriptionToCategoryDocument document) {
        DocumentRevision rev = new DocumentRevision(DescriptionToCategoryDocument.DESCRIPTION_TO_CATEGORY_ID + MainActivity.getUserId());

        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return DescriptionToCategoryDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a account document, assigning an ID.
     *
     * @param document account to create
     * @return new revision of the document
     */
    public AccountDocument createDocument(AccountDocument document) {
        DocumentRevision rev = new DocumentRevision(AccountDocument.ACCOUNT_DOCUMENT_ID + MainActivity.getUserId());

        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return AccountDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    /**
     * Creates a category document, assigning an ID.
     *
     * @param document category to create
     * @return new revision of the document
     */
    public CategoryDocument createDocument(CategoryDocument document) {
        DocumentRevision rev = new DocumentRevision(CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());

        rev.setBody(DocumentBodyFactory.create(document.asMap()));
        try {
            DocumentRevision created = this.mDatastore.createDocumentFromRevision(rev);

            return CategoryDocument.fromRevision(created);

        } catch (DocumentException de) {
            Log.e("Doc", "document was not created");
            return null;
        }
    }

    public Bitmap getGoalImage(String docId, String name) {
        try {
            DocumentRevision retrieved = this.mDatastore.getDocument(docId);
            Attachment att = retrieved.getAttachments().get(name);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            return BitmapFactory.decodeStream(att.getInputStream(), null, options);

        } catch (DocumentNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves document by id.
     *
     * @param docId task to create
     * @return revision of the document
     */
    public FinanceDocument getFinanceDocument(String docId) {

        DocumentRevision retrieved = null;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            e.printStackTrace();
            Log.e("Doc", "document was not found");
        }
        return FinanceDocument.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId task to create
     * @return revision of the document
     */
    public Currency getCurrencyDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return Currency.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId task to create
     * @return revision of the document
     */
    public BudgetDocument getBudgetDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return BudgetDocument.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId task to create
     * @return revision of the document
     */
    public BankingCardDocument getBankingCardDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return BankingCardDocument.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId task to create
     * @return revision of the document
     */
    public GoalDocument getGoalDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return GoalDocument.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId id of document
     * @return revision of the document
     */
    public DescriptionToCategoryDocument getDescriptionToCategoryDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return DescriptionToCategoryDocument.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId id of document
     * @return revision of the document
     */
    public AccountDocument getAccountDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return AccountDocument.fromRevision(retrieved);
    }

    /**
     * Retrieves document by id.
     *
     * @param docId id of document
     * @return revision of the document
     */
    public CategoryDocument getCategoryDocument(String docId) {

        DocumentRevision retrieved;
        try {
            retrieved = mDatastore.getDocument(docId);
        } catch (DocumentNotFoundException e) {
            //e.printStackTrace();
            Log.e("Doc", "document was not found");
            return null;
        }
        return CategoryDocument.fromRevision(retrieved);
    }

    /**
     * Updates a Document document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public FinanceDocument updateFinanceDocument(FinanceDocument oldDocument, FinanceDocument newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return FinanceDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a Document document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public Currency updateCurrencyDocument(Currency oldDocument, Currency newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return Currency.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a Document document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public BudgetDocument updateBudgetDocument(BudgetDocument oldDocument, BudgetDocument newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return BudgetDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a Banking card document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public BankingCardDocument updateBankingDocument(BankingCardDocument oldDocument, BankingCardDocument newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return BankingCardDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a Document document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public GoalDocument updateGoalDocument(GoalDocument oldDocument, GoalDocument newDocument, String imageUri) throws ConflictException {

        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {

            if (imageUri != null) {
                UnsavedFileAttachment att1 = new UnsavedFileAttachment(
                        new File(imageUri), "image/jpeg");
                if (oldDocument.getDocumentRevision().getAttachments() != null) {
                    rev.getAttachments().clear();
                    rev.getAttachments().put(att1.name, att1);
                } else {
                    // As with the document body, you can replace the attachments
                    rev.setAttachments(new HashMap<String, Attachment>());

                    // Or just add or update a single one:
                    // (because the getter will always return the underlying map and not a copy)
                    rev.getAttachments().put(att1.name, att1);
                }
            } else {
                if (oldDocument.getDocumentRevision().getAttachments() != null) {
                    rev.getAttachments().putAll(oldDocument.getDocumentRevision().getAttachments());
                } else {
                    rev.getAttachments().clear();
                }
            }
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return GoalDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }


    /**
     * Updates a Banking card document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public DescriptionToCategoryDocument updateDescriptionToCategoryDocument(DescriptionToCategoryDocument oldDocument, DescriptionToCategoryDocument newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return DescriptionToCategoryDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a account document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public AccountDocument updateAccountDocument(AccountDocument oldDocument, AccountDocument newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return AccountDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a category document within the datastore.
     *
     * @param oldDocument document to update
     * @param newDocument new document
     * @return the updated revision of the Task
     * @throws ConflictException if the document passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public CategoryDocument updateCategoryDocument(CategoryDocument oldDocument, CategoryDocument newDocument) throws ConflictException {
        DocumentRevision rev = oldDocument.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(newDocument.asMap()));
        try {
            DocumentRevision updated = this.mDatastore.updateDocumentFromRevision(rev);

            return CategoryDocument.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Deletes a Finance document within the datastore.
     *
     * @param doc task to delete
     * @throws ConflictException if the task passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public void deleteDocument(FinanceDocument doc) throws ConflictException {
        this.mDatastore.deleteDocumentFromRevision(doc.getDocumentRevision());

    }

    /**
     * Deletes a Budget document within the datastore.
     *
     * @param doc task to delete
     * @throws ConflictException if the task passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public void deleteDocument(BudgetDocument doc) throws ConflictException {
        this.mDatastore.deleteDocumentFromRevision(doc.getDocumentRevision());

    }

    /**
     * Deletes a banking card document within the datastore.
     *
     * @param doc task to delete
     * @throws ConflictException if the task passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public void deleteDocument(BankingCardDocument doc) throws ConflictException {
        this.mDatastore.deleteDocumentFromRevision(doc.getDocumentRevision());

    }

    /**
     * Deletes a account document within the datastore.
     *
     * @param doc task to delete
     * @throws ConflictException if the task passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public void deleteDocument(AccountDocument doc) throws ConflictException {
        this.mDatastore.deleteDocumentFromRevision(doc.getDocumentRevision());

    }

    /**
     * Deletes a  document within the datastore.
     *
     * @param docId to delete
     * @throws ConflictException if the task passed in has a rev which doesn't
     *                           match the current rev in the datastore.
     */
    public void deleteDocument(String docId) throws DocumentException {
        this.mDatastore.deleteDocument(docId);

    }


    /**
     * <p>Returns all {@code Task} documents in the datastore.</p>
     */
    public List<FinanceDocument> allTasks() {
        int nDocs = this.mDatastore.getDocumentCount();
        List<DocumentRevision> all = this.mDatastore.getAllDocuments(0, nDocs, true);
        List<FinanceDocument> tasks = new ArrayList<>();

        // Filter all documents down to those of type Task.
        for (DocumentRevision rev : all) {
            FinanceDocument t = FinanceDocument.fromRevision(rev);
            if (t != null) {
                tasks.add(t);
            }
        }

        return tasks;
    }


}
