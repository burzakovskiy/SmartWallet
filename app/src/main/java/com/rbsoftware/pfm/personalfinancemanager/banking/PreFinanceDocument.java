package com.rbsoftware.pfm.personalfinancemanager.banking;

/**
 * Holds methods for managing loaded banking data
 *
 * @author Roman Burzakovskiy
 */
public class PreFinanceDocument {

    private String mDate;
    private String mValue;
    private String mCurrency;
    private String mDescription;

    public PreFinanceDocument() {

    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        long unixTimeStamp = Long.valueOf(date)/1000;
        this.mDate = String.valueOf(unixTimeStamp);
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String mValue) {
        this.mValue = mValue;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String mCurrency) {
        this.mCurrency = mCurrency;
    }
}
