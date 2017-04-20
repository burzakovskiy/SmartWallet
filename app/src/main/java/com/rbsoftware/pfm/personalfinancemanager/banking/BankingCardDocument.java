package com.rbsoftware.pfm.personalfinancemanager.banking;

import com.cloudant.sync.datastore.DocumentRevision;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds methods for managing banking card document
 *
 * @author Roman Burzakovskiy
 */
public class BankingCardDocument {
    public static final String DOC_TYPE = "BankingCard Document";

    private DocumentRevision rev;

    private String userId;
    private String type;
    private String date;
    private String country;
    private String bank;
    private String cardNumber;
    private String accountNumber;

    private BankingCardDocument() {

    }

    /**
     * Constructor of banking card document
     *
     * @param userId        of current user
     * @param country       of bank
     * @param bank          where card is registered
     * @param cardNumber    last 8 digits of card number
     * @param accountNumber 10 digits of account number
     */
    public BankingCardDocument(String userId, String country, String bank, String cardNumber, String accountNumber) {
        this.type = DOC_TYPE;
        Date currDate = new Date();
        this.date = Long.toString(currDate.getTime() / 1000);
        this.userId = userId;
        this.country = country;
        this.bank = bank;
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
    }


    /**
     * Sets budget date
     *
     * @param date of budget creation or update
     */
    private void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets budget date
     *
     * @return budget date in unix format
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets document type of budget document
     *
     * @param type of document
     */
    private void setType(String type) {
        this.type = type;
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
     * Sets country to document
     *
     * @param country of bank
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets country of bank
     *
     * @return country of bank
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets bak where card is registered
     *
     * @param bank of card
     */
    public void setBank(String bank) {
        this.bank = bank;
    }

    /**
     * Gets bank where card is registered
     *
     * @return bank of card
     */
    public String getBank() {
        return bank;
    }

    /**
     * Sets card number to document
     *
     * @param cardNumber last 8 digits of card number
     */
    private void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /**
     * Gets card number
     *
     * @return last 8 digits of card number
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Set account number
     *
     * @param accountNumber of user's account
     */
    private void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Gets account number
     *
     * @return 10 digits account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }


    public Map<String, Object> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("userId", userId);
        map.put("date", date);
        map.put("country", country);
        map.put("bank", bank);
        map.put("cardNumber", cardNumber);
        map.put("accountNumber", accountNumber);

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
     * @return banking card document
     */
    public static BankingCardDocument fromRevision(DocumentRevision rev) {
        BankingCardDocument t = new BankingCardDocument();
        t.rev = rev;
        Map<String, Object> map = rev.asMap();
        if (map.containsKey("type") && map.get("type").equals(DOC_TYPE)) {
            t.setUserId((String) map.get("userId"));
            t.setDate((String) map.get("date"));
            t.setType((String) map.get("type"));
            t.setCountry((String) map.get("country"));
            t.setBank((String) map.get("bank"));
            t.setCardNumber((String) map.get("cardNumber"));
            t.setAccountNumber((String) map.get("accountNumber"));
            return t;
        }
        return null;
    }
}
