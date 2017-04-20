package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Holds generic methods of SMS parsing
 *
 * @author Roman Burzakovskiy
 */
public abstract class BasicSMSParser {
    private final static String TAG = "BasicSMSParser";
    protected final static String KEY_ID = "_id";
    protected final static String KEY_ADDRESS = "address";
    protected final static String KEY_DATE = "date";
    protected final static String KEY_BODY = "body";
    private String mCardNumber;
    private String mAccountNumber;

    private final Context mContext;
    private final Uri inboxURI;
    private final Cursor mCursor;

    /**
     * Constructor of parser
     *
     * @param context     of application
     * @param phoneNumber to monitor sms
     * @param limit       max entries, -1 if no limit needed
     */
    public BasicSMSParser(Context context, String phoneNumber, int limit) {
        mContext = context;
        inboxURI = Uri.parse("content://sms/inbox");
        String[] reqCols = new String[]{KEY_ID, KEY_ADDRESS, KEY_DATE, KEY_BODY};

        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = mContext.getContentResolver();
        String limitBuilder = "";
        if (limit != -1) limitBuilder = " limit " + limit;
        // Fetch Inbox SMS Message from Built-in Content Provider
        mCursor = cr.query(inboxURI, reqCols, KEY_ADDRESS + " like ?", new String[]{phoneNumber + "%"}, KEY_DATE + " desc" + limitBuilder);

    }

    /**
     * Constructor of parser
     *
     * @param context       of application
     * @param phoneNumber   to monitor sms
     * @param limit         max entries, -1 if no limit needed
     * @param unixTimeStamp of sms
     */
    public BasicSMSParser(Context context, String phoneNumber, int limit, String unixTimeStamp) {
        mContext = context;
        inboxURI = Uri.parse("content://sms/inbox");
        String[] reqCols = new String[]{KEY_ID, KEY_ADDRESS, KEY_DATE, KEY_BODY};

        // Get Content Resolver object, which will deal with Content Provider
        ContentResolver cr = mContext.getContentResolver();
        String limitBuilder = "";

        long date = Long.valueOf(unixTimeStamp) * 1000;

        if (limit != -1) limitBuilder = " limit " + limit;
        // Fetch Inbox SMS Message from Built-in Content Provider
        mCursor = cr.query(inboxURI, reqCols, KEY_ADDRESS + " like ? and " + KEY_DATE + " >= ?", new String[]{phoneNumber + "%", String.valueOf(date)}, KEY_DATE + " desc" + limitBuilder);

    }

    /**
     * Validates that SMS banking is enabled and sms in parsable
     *
     * @return true if card is validated
     */
    public abstract boolean initValidation();

    /**
     * Gets cursor
     *
     * @return cursor
     */
    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Closes cursor
     */
    public void closeCursor() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    /**
     * Sets card number
     *
     * @param cardNumber of card
     */
    public void setCardNumber(String cardNumber) {
        this.mCardNumber = cardNumber;
    }

    /**
     * Gets card number
     *
     * @return card number
     */
    protected String getCardNumber() {
        return mCardNumber;
    }

    /**
     * Sets account number
     *
     * @param accountNumber of card
     */
    public void setAccountNumber(String accountNumber) {
        this.mAccountNumber = accountNumber;
    }

    /**
     * Gets account number
     *
     * @return account number
     */
    protected String getAccountNumber() {
        if (mAccountNumber == null || mAccountNumber.isEmpty())
            return "no_account";
        else {
            return mAccountNumber;
        }
    }

    protected Context getContext(){return mContext;}

}
