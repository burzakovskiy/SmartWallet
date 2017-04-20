package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.Context;
import android.database.Cursor;

import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds methods for parsing Ukrsibbank sms
 *
 * Created by Roman Burzakovskiy on 7/26/2016.
 */
public class UkrsibbankSMSParser extends BasicSMSParser {

    private final static String TAG = "UkrsibbankSMSParser";
    public final static String PHONE_NUMBER = "729";
    private final static String SMS_KEY_CANCEL = "Vidmina operatsii";
    private final static String SMS_KEY_AMOUNT = "na sumu";
    private final static String SMS_KEY_BALANCE = "Dostupnyi zalyshok";

    /**
     * Public parser constructor
     *
     * @param context app context
     * @param limit   max number of sms to process -1  -infinite sms
     */
    public UkrsibbankSMSParser(Context context, int limit) {
        super(context, PHONE_NUMBER, limit);
    }

    /**
     * Public parser constructor
     *
     * @param context       app context
     * @param limit         max number of sms to process -1  -infinite sms
     * @param unixTimeStamp date to start parsing from
     */
    public UkrsibbankSMSParser(Context context, int limit, String unixTimeStamp) {
        super(context, PHONE_NUMBER, limit, unixTimeStamp);
    }

    @Override
    public boolean initValidation() {
        Cursor cursor = getCursor();


        if (cursor != null && !cursor.isClosed() && (cursor.getCount() > 0)) {

            if (cursor.moveToFirst()) {
                do {

                    String smsBody = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BODY));
                    if (hasCardNumber(smsBody)) {
                        return true;
                    }
                } while (cursor.moveToNext());
            }
        }
        return false;
    }


    /**
     * Reads cursor data and creates PreFinanceDocument list
     *
     * @return PreFinanceDocument list
     */
    public List<PreFinanceDocument> getParsedSMSList() {
        List<PreFinanceDocument> preFinanceDocumentList = new ArrayList<>();
        Cursor cursor = getCursor();
        if (cursor.moveToFirst()) {
            do {
                String smsBody = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BODY));

                if (hasCardNumber(smsBody) || hasAccountNumber(smsBody)) {
                    String[] parsedSMSBody = parseSMSBody(smsBody);

                    if (!parsedSMSBody[0].isEmpty()) {
                        PreFinanceDocument preFinanceDocument = new PreFinanceDocument();
                        preFinanceDocument.setDate(
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(KEY_DATE)));

                        preFinanceDocument.setValue(parsedSMSBody[0]);
                        preFinanceDocument.setCurrency(parsedSMSBody[1]);
                        preFinanceDocument.setDescription(parsedSMSBody[2]);
                        preFinanceDocumentList.add(preFinanceDocument);
                    }
                }
            } while (cursor.moveToNext());
        }

        closeCursor();

        return preFinanceDocumentList;
    }

    /**
     * Parses sms body to extract pre finance document data
     *
     * @param smsBody of message
     * @return 0 - value
     * 1 - currency
     * 2 - description
     */
    private String[] parseSMSBody(String smsBody) {
        if (!smsBody.contains(SMS_KEY_CANCEL)) {
            return extractCardData(smsBody);
        }
        return new String[]{"", "", ""};
    }

    /**
     * Extracts prefinance document data from card sms
     *
     * @param smsBody body of sms
     * @return prefinance document data
     */
    private String[] extractCardData(String smsBody) {
        String[] cardData = new String[]{"", "", ""};
        if (smsBody.contains(SMS_KEY_AMOUNT)) {
            String result0 = smsBody.split(SMS_KEY_AMOUNT)[1];
            if (result0.contains(SMS_KEY_BALANCE)) {
                String result1 = result0.split(SMS_KEY_BALANCE)[0].trim();
                if (result1.endsWith(".")) {
                    result1 = result1.substring(0, result1.length() - 1);
                }
                String value = result1.replaceAll("[A-Za-z]", "");
                String currency = result1.replaceAll("[^A-Za-z]", "");

                String[] currencyArray = getContext().getResources().getStringArray(R.array.report_activity_currency_spinner);
                if (!value.isEmpty() && !currency.isEmpty() && Utils.isNumber(value) && Arrays.asList(currencyArray).contains(currency)) {
                    cardData[0] = String.valueOf(Math.abs(Float.valueOf(value)));
                    cardData[1] = currency;
                    cardData[2] = smsBody.split(",")[0].trim();
                }
            }
        }
        return cardData;
    }

    /**
     * Checks if SMS contains card number
     *
     * @param smsBody tex message
     * @return true if SMS contains card number
     */
    private boolean hasCardNumber(String smsBody) {
        return smsBody.contains(getCardNumber());
    }

    /**
     * Checks if SMS contains account number
     *
     * @param smsBody tex message
     * @return true if SMS contains account number
     */
    private boolean hasAccountNumber(String smsBody) {
        return smsBody.contains(getAccountNumber());
    }

    /**
     * Converts card number into bank's format
     *
     * @return trimmed card number
     */
    public static String convertCardNumber(String part1, String part4) {

        return part1 + "***" + part4;
    }


}
