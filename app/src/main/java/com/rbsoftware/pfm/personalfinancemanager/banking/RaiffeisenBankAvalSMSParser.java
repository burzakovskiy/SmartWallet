package com.rbsoftware.pfm.personalfinancemanager.banking;

import android.content.Context;
import android.database.Cursor;

import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.utils.Utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds methods for Raiffeisen Bank Aval SMS parsing
 *
 * @author Roman Burzakovskiy
 */
public class RaiffeisenBankAvalSMSParser extends BasicSMSParser {
    private final static String TAG = "RaiffeisenBankSMSParser";
    public final static String PHONE_NUMBER = "10901";

    private final static String SMS_KEY_EXPENSE_UAH = "Vasha operatsija:";
    private final static String SMS_KEY_EXPENSE_FOREIGN = "Suma operacii";
    private final static String SMS_KEY_INCOME_ATM = "Popovnennja sumy cherez ATM:";
    private final static String SMS_KEY_INCOME_CASHLESS = "Bezgotivkove zarakhuvannya:";
    private final static String SMS_KEY_INCOME_ACCOUNT = "na Vash rakhunok kartkovyi";
    private final static String SMS_KEY_INCOME_RETURN = "Povernennja sumy:";
    private final static String SMS_KEY_CANCELLED = "vidkhylena:";


    public RaiffeisenBankAvalSMSParser(Context context, int limit) {

        super(context, PHONE_NUMBER, limit);
    }

    public RaiffeisenBankAvalSMSParser(Context context, int limit, String unixTimeStamp) {
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
    public static String convertCardNumber(String part3, String part4) {
        String part1 = part3.substring(part3.length() / 2);
        return part1 + part4;
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
        if (smsBody.contains(SMS_KEY_EXPENSE_UAH)) {

            return extractCardDataUAH(smsBody);
        }
        if (smsBody.contains(SMS_KEY_EXPENSE_FOREIGN)) {
            return extractCardDataForeign(smsBody);
        }
        if (smsBody.contains(SMS_KEY_INCOME_ATM)) {

            return extractCardDataUAH(smsBody);
        }
        if (smsBody.contains(SMS_KEY_INCOME_CASHLESS)) {


            return extractCardDataUAH(smsBody);

        }
        if (smsBody.contains(SMS_KEY_INCOME_ACCOUNT)) {
            return extractAccountData(smsBody);
        }
        if (smsBody.contains(SMS_KEY_INCOME_RETURN)) {


            return extractCardDataUAH(smsBody);
        }

        return new String[]{"", "", ""};
    }

    /**
     * Extracts data from foreign currency operations
     *
     * @param smsbody of sms
     * @return 0 - vaule
     * 1 - currency
     * 2 - description
     */
    private String[] extractCardDataForeign(String smsbody) {
        String[] result = new String[]{"", "", ""};
        if (smsbody.contains(SMS_KEY_CANCELLED)) {
            return result;
        } else {
            String[] resultData1 = smsbody.split(SMS_KEY_EXPENSE_FOREIGN);
            String[] resultData2 = resultData1[1].split(", ");
            String value = resultData2[0].trim().split(" ")[0];
            String currency = resultData2[0].trim().split(" ")[1];
            String[] currencyArray = getContext().getResources().getStringArray(R.array.report_activity_currency_spinner);
            if (Utils.isNumber(value) && Arrays.asList(currencyArray).contains(currency)) {
                result[0] = value;
                result[1] = currency;
                result[2] = resultData1[0].replaceAll("[^A-Za-z ]", "").trim();

                return result;
            } else {
                return result;
            }
        }
    }

    /**
     * Extracts data from local currency operations
     *
     * @param smsbody of sms
     * @return 0 - vaule
     * 1 - currency
     * 2 - description
     */
    private String[] extractCardDataUAH(String smsbody) {
        String[] result = new String[]{"", "", ""};
        if (smsbody.contains(SMS_KEY_CANCELLED)) {
            return result;
        } else {
            String[] resultData1 = smsbody.split(getCardNumber());
            String[] resultData2 = resultData1[1].split("dostupna suma");
            String value = resultData2[0].split("UAH")[0].trim();
            if (Utils.isNumber(value)) {
                result[0] = value;
                result[1] = "UAH";
                result[2] = resultData2[0].split("UAH")[1].trim();

                return result;
            } else {
                return result;
            }


        }

    }

    /**
     * Extracts data from account operations
     *
     * @param smsbody of sms
     * @return 0 - vaule
     * 1 - currency
     * 2 - description
     */
    private String[] extractAccountData(String smsbody) {
        String[] result = new String[]{"", "", ""};
        String[] resultData1 = smsbody.split("bulo zarakhovano sumu ");
        String[] resultData2 = resultData1[1].split(" ");
        String value = resultData2[0];
        if (Utils.isNumber(value)) {
            result[0] = resultData2[0];
            result[1] = resultData2[1];
            result[2] = getContext().getString(R.string.refill);
            return result;
        } else {
            return result;
        }
    }
}
