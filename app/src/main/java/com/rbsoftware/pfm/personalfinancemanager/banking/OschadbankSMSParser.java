package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.Context;
import android.database.Cursor;

import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds methods for parsing oschadbank sms
 *
 * @author Roman Burzakovskiy
 */
public class OschadbankSMSParser extends BasicSMSParser {

    private final static String TAG = "OschadbankSMSParser";
    public final static String PHONE_NUMBER = "Oschadbank";
    private final static String SMS_KEY_CARD = "Card";
    private final static String SMS_KEY_ACCOUNT = "Acnt";
    private final static String SMS_KEY_SUM = "Sum";

    public OschadbankSMSParser(Context context, int limit) {
        super(context, PHONE_NUMBER, limit);
    }

    public OschadbankSMSParser(Context context, int limit, String unixTimeStamp) {
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
        if (smsBody.contains(SMS_KEY_CARD)) {
            return extractCardData(smsBody);
        }
        if (smsBody.contains(SMS_KEY_ACCOUNT)) {

            return extractAccountData(smsBody);
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

        String amountLine = "";
        String place = "";
        BufferedReader bufReader = new BufferedReader(new StringReader(smsBody));
        String line;
        try {
            while ((line = bufReader.readLine()) != null) {
                if (line.contains(SMS_KEY_SUM)) {
                    amountLine = line;
                }
                if (line.contains("M=")) {
                    place = line;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!amountLine.isEmpty() && !place.isEmpty()) {
            String result = amountLine.replaceAll("[Sum: ]", "");
            String value = result.replaceAll("[A-Za-z]", "");
            String currency = result.replaceAll("[^A-Za-z]", "");
            String[] currencyArray = getContext().getResources().getStringArray(R.array.report_activity_currency_spinner);
            if (!value.isEmpty() && !currency.isEmpty() && Utils.isNumber(value) && Arrays.asList(currencyArray).contains(currency)) {
                String[] cardData = new String[]{"", "", ""};
                cardData[0] = String.valueOf(Math.abs(Float.valueOf(value)));
                cardData[1] = currency;
                cardData[2] = place.replaceAll("M=", "").trim();
                return cardData;
            }


        }
        return new String[]{"", "", ""};
    }

    /**
     * Extracts prefinance document data from account sms
     *
     * @param smsBody body of sms
     * @return prefinance document data
     */
    private String[] extractAccountData(String smsBody) {
        String amountLine = "";
        String place = "";
        BufferedReader bufReader = new BufferedReader(new StringReader(smsBody));
        String line;
        try {

            while ((line = bufReader.readLine()) != null) {
                if (line.contains(SMS_KEY_SUM)) {
                    amountLine = line;
                    if ((line = bufReader.readLine()) != null) {
                        place = line;
                        break;
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!amountLine.isEmpty() && !place.isEmpty()) {
            String result = amountLine.replaceAll("[Sum: ]", "");
            String value = result.replaceAll("[A-Za-z]", "");
            String currency = result.replaceAll("[^A-Za-z]", "");
            String[] currencyArray = getContext().getResources().getStringArray(R.array.report_activity_currency_spinner);
            if (!value.isEmpty() && !currency.isEmpty() && Utils.isNumber(value) && Arrays.asList(currencyArray).contains(currency)) {
                String[] cardData = new String[]{"", "", ""};
                cardData[0] = String.valueOf(Math.abs(Float.valueOf(value)));
                cardData[1] = currency;
                cardData[2] = place.trim();
                return cardData;
            }
        }
        return new String[]{"", "", ""};
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

        return part1 + "-" + part4;
    }

    /**
     * Converts account number into bank's format
     *
     * @return trimmed card number
     */
    private static String convertAccountNumber(String account) {
        StringBuilder buffer = new StringBuilder(account);
        buffer.insert(3, "-");
        buffer.insert(10, "-");
        return buffer.toString();
    }

    @Override
    protected String getAccountNumber() {
        return convertAccountNumber(super.getAccountNumber());
    }
}
