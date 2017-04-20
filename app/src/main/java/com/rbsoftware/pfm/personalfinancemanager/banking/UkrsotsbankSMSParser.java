package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.Context;
import android.database.Cursor;

import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds methods for Ukrsotsbank SMS parsing
 *
 * @author Roman Burzakovskiy
 */
public class UkrsotsbankSMSParser extends BasicSMSParser {

    private final static String TAG = "UkrsotsbankSMSParser";
    public final static String PHONE_NUMBER = "UniCredit";
    private final static String SMS_KEY_OPERATION_SUM = "Operatsia na sumu";
    private final static String SMS_KEY_AMOUNT_LEFT = "Zalyshok";

    public UkrsotsbankSMSParser(Context context, int limit) {
        super(context, PHONE_NUMBER, limit);
    }

    public UkrsotsbankSMSParser(Context context, int limit, String unixTimeStamp) {
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

        return part1 + "*" + part4;
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
        if (smsBody.contains(SMS_KEY_OPERATION_SUM)) {
            String[] resultData1 = smsBody.split(" po ");
            String part01 = resultData1[0].split(SMS_KEY_OPERATION_SUM)[1].trim().replace(" ", "");
            String value = part01.replaceAll("[A-Za-z]", "");
            String currency = part01.replaceAll("[^A-Za-z]", "");
            String description = resultData1[1].split(SMS_KEY_AMOUNT_LEFT)[0];
            String cardPattern = getCardNumber();
            String accountPattern = getAccountNumber();
            String descriptionResult;
            if (description.contains(cardPattern)) {
                descriptionResult = description.split(cardPattern)[1].trim();
            } else {
                descriptionResult = description.split(accountPattern)[1].trim();
            }


            String[] currencyArray = getContext().getResources().getStringArray(R.array.report_activity_currency_spinner);
            if (!value.isEmpty() && !currency.isEmpty() && Utils.isNumber(value) && Arrays.asList(currencyArray).contains(currency)) {

                return new String[]{value, currency, descriptionResult};
            }


        }
        return new String[]{"", "", ""};
    }

    @Override
    protected String getAccountNumber() {
        return super.getAccountNumber().substring(10);
    }
}
