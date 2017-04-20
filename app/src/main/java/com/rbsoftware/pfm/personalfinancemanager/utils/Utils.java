package com.rbsoftware.pfm.personalfinancemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.rbsoftware.pfm.personalfinancemanager.FinanceDocument;
import com.rbsoftware.pfm.personalfinancemanager.MainActivity;
import com.rbsoftware.pfm.personalfinancemanager.R;
import com.rbsoftware.pfm.personalfinancemanager.billing.Purchase;
import com.rbsoftware.pfm.personalfinancemanager.categories.CategoryDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Holds various helper methods
 *
 * @author Roman Burzakovskiy
 */
public class Utils {


    private static final String TAG = "Utils";
    public static final String PREF_FILE = "PrefFile";

    /**
     * Detects if application runs on tablet
     *
     * @param context app context
     * @return true if app runs on tablet, false if on phone
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Converts int key to human readable string
     *
     * @param mContext application context
     * @param key      value range 1-FinanceDocument.NUMBER_OF_CATEGORIES
     * @return string value
     */
    public static String keyToString(Context mContext, String key) {
        CategoryDocument categoryDocument = MainActivity.financeDocumentModel.getCategoryDocument(
                CategoryDocument.CATEGORY_DOCUMENT_ID + MainActivity.getUserId());
        HashMap<String, List<String>> customCategoriesMap = categoryDocument.getCategoriesMap();

        if (isNumber(key)) {
            switch (Integer.valueOf(key)) {
                case 1:
                    return mContext.getResources().getString(R.string.salary);
                case 2:
                    return mContext.getResources().getString(R.string.rental_income);
                case 3:
                    return mContext.getResources().getString(R.string.interest);
                case 4:
                    return mContext.getResources().getString(R.string.gifts);
                case 5:
                    return mContext.getResources().getString(R.string.other_income);
                case 6:
                    return mContext.getResources().getString(R.string.taxes);
                case 7:
                    return mContext.getResources().getString(R.string.mortgage);
                case 8:
                    return mContext.getResources().getString(R.string.credit_card);
                case 9:
                    return mContext.getResources().getString(R.string.utilities);
                case 10:
                    return mContext.getResources().getString(R.string.food);
                case 11:
                    return mContext.getResources().getString(R.string.car_payment);
                case 12:
                    return mContext.getResources().getString(R.string.personal);
                case 13:
                    return mContext.getResources().getString(R.string.activities);
                case 14:
                    return mContext.getResources().getString(R.string.other_expense);
            }
        } else {
            if (customCategoriesMap.containsKey(key)) {
                return customCategoriesMap.get(key).get(0);
            }

        }
        return "";
    }

    /**
     * Converts dp to px
     *
     * @param mContext application context
     * @param dp       density pixels
     * @return pixels
     */
    public static int dpToPx(Context mContext, int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics));
    }

    /**
     * Gets currency position in array
     *
     * @param currency currency string
     * @return currency position in array
     */
    public static int getCurrencyPosition(String currency) {
        switch (currency) {
            case "USD":
                return 0;
            case "EUR":
                return 1;
            case "GBP":
                return 2;
            case "UAH":
                return 3;
            case "RUB":
                return 4;
            case "BTC":
                return 5;
            case "PLN":
                return 6;
            case "KRW":
                return 7;
            default:
                return 0;
        }
    }


    /**
     * Parsing string to retrieve document data
     */
    public static ArrayList<String> getItem(ArrayList<String> reportResult, int i) {
        ArrayList<String> item = new ArrayList<>();
        item.add(0, "0");
        item.add(1, MainActivity.defaultCurrency);
        item.add(2, "Never");
        for (String listItem : reportResult) {
            String[] parts = listItem.split("-");
            int position = Integer.valueOf(parts[0]);
            if (i == position) {
                item.clear();
                item.add(0, parts[2]);
                item.add(1, parts[3]);
                /* Recursion disabled in version 1.0
                    TODO enable recursion in future versions
                item.add(2,parts[4]);
                */
                item.add(2, "Never");
            }


        }

        return item;
    }


    /**
     * Gets progress bar color
     *
     * @param mContext  application context
     * @param max       progressbar max value
     * @param threshold progressbar threshold
     * @param progress  progressbar current progress
     * @return progressbar color
     */
    public static int getProgressColor(Context mContext, int max, int threshold, int progress) {
        int color = ContextCompat.getColor(mContext, R.color.income);
        if (progress < threshold) color = ContextCompat.getColor(mContext, R.color.income);
        if ((threshold < max) && (progress >= threshold) && (progress <= (max - max * 0.05)))
            color = ContextCompat.getColor(mContext, R.color.colorAccent);
        if ((progress >= (max - max * 0.05)) && (progress <= max))
            color = ContextCompat.getColor(mContext, R.color.expense);
        return color;
    }

    /**
     * Gets color by data type key
     *
     * @param key data type key
     * @return color
     */
    public static int getColorPalette(Context mContext, String key) {
        if (isNumber(key)) {
            switch (Integer.valueOf(key)) {
                case -2:
                    return ContextCompat.getColor(mContext, R.color.balance);
                case -1:
                    return ContextCompat.getColor(mContext, R.color.income);
                case 0:
                    return ContextCompat.getColor(mContext, R.color.expense);
                case 1:
                    return ContextCompat.getColor(mContext, R.color.salary);
                case 2:
                    return ContextCompat.getColor(mContext, R.color.rental_income);
                case 3:
                    return ContextCompat.getColor(mContext, R.color.interest);
                case 4:
                    return ContextCompat.getColor(mContext, R.color.gifts);
                case 5:
                    return ContextCompat.getColor(mContext, R.color.other_income);
                case 6:
                    return ContextCompat.getColor(mContext, R.color.taxes);
                case 7:
                    return ContextCompat.getColor(mContext, R.color.mortgage);
                case 8:
                    return ContextCompat.getColor(mContext, R.color.credit_card);
                case 9:
                    return ContextCompat.getColor(mContext, R.color.utilities);
                case 10:
                    return ContextCompat.getColor(mContext, R.color.food);
                case 11:
                    return ContextCompat.getColor(mContext, R.color.car_payment);
                case 12:
                    return ContextCompat.getColor(mContext, R.color.personal);
                case 13:
                    return ContextCompat.getColor(mContext, R.color.activities);
                case 14:
                    return ContextCompat.getColor(mContext, R.color.other_expense);

            }
        }
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));


    }


    public static String getRealPathFromUri(Context context, Uri contentUri) {
        if (contentUri != null) {
            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    public static String getImageName(String path) {
        if (path != null) {
            return (new File(path)).getName();
        }
        return null;
    }

    /**
     * <p>Checks whether the String a valid Java number.</p>
     * <p/>
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code> or
     * <code>0X</code> qualifier, octal numbers, scientific notation and numbers
     * marked with a type qualifier (e.g. 123L).</p>
     * <p/>
     * <p>Non-hexadecimal strings beginning with a leading zero are
     * treated as octal values. Thus the string <code>09</code> will return
     * <code>false</code>, since <code>9</code> is not a valid octal value.
     * However, numbers beginning with {@code 0.} are treated as decimal.</p>
     * <p/>
     * <p><code>null</code> and empty/blank {@code String} will return
     * <code>false</code>.</p>
     *
     * @param str the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     * @since 3.3 the code supports hex {@code 0Xhhh} and octal {@code 0ddd} validation
     */
    public static boolean isNumber(final String str) {
        if (str.isEmpty()) {
            return false;
        }
        final char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0') { // leading 0
            if (
                    (chars[start + 1] == 'x') ||
                            (chars[start + 1] == 'X')
                    ) { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            } else if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < '0' || chars[i] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                    || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    public static int getDrawableByCategory(int category) {
        switch (category) {
            case FinanceDocument.PARAM_SALARY:
                return R.drawable.ic_salary_black_24dp;
            case FinanceDocument.PARAM_RENTAL_INCOME:
                return R.drawable.ic_rental_income_black_24dp;
            case FinanceDocument.PARAM_INTEREST:
                return R.drawable.ic_interest_black_24dp;
            case FinanceDocument.PARAM_GIFTS:
                return R.drawable.ic_gifts_black_24dp;
            case FinanceDocument.PARAM_OTHER_INCOME:
                return R.drawable.ic_other_income_black_24dp;

            case FinanceDocument.PARAM_FOOD:
                return R.drawable.ic_food_black_24dp;
            case FinanceDocument.PARAM_CAR_PAYMENT:
                return R.drawable.ic_car_payment_black_24dp;
            case FinanceDocument.PARAM_PERSONAL:
                return R.drawable.ic_personal_black_24dp;
            case FinanceDocument.PARAM_ACTIVITIES:
                return R.drawable.ic_activities_black_24dp;
            case FinanceDocument.PARAM_UTILITIES:
                return R.drawable.ic_utilities_black_24dp;
            case FinanceDocument.PARAM_CREDIT_CARD:
                return R.drawable.ic_credit_card_black_24dp;
            case FinanceDocument.PARAM_TAXES:
                return R.drawable.ic_taxes_black_24dp;
            case FinanceDocument.PARAM_MORTGAGE:
                return R.drawable.ic_mortgage_black_24dp;
            case FinanceDocument.PARAM_OTHER_EXPENSE:
                return R.drawable.ic_other_expense_black_24dp;
            default:
                return R.drawable.ic_other_expense_black_24dp;
        }
    }

    /**
     * Generates random alphanumeric string
     *
     * @param length of string
     * @return random string
     */
    public static String generateRandomString(int length) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvmxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }

    /**
     * Static methods for saving to sharedpreferences
     *
     * @param context   application context
     * @param prefName  name variable
     * @param prefValue value
     */
    public static void saveToSharedPreferences(Context context, String prefName, String prefValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefName, prefValue);
        editor.apply();
    }

    /**
     * Static methods for reading from sharedpreferences
     *
     * @param context      application context
     * @param prefName     name variable
     * @param defaultValue default value
     */
    public static String readFromSharedPreferences(Context context, String prefName, String defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(prefName, defaultValue);
    }

    /**
     * Checks if screen orientation has been changed
     *
     * @param context   of application
     * @param prevState previous screen state
     * @return true if orientation has been changed
     */
    public static boolean isOrientationChanged(Context context, int prevState) {
        return prevState != context.getResources().getConfiguration().orientation;
    }

    /**
     * Checks if current device is buggy samsung
     *
     * @return true if current device is buggy samsung
     */
    public static boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung")
                && Build.MODEL.toLowerCase().contains("j5")
                && isBetweenAndroidVersions(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1));
    }

    /**
     * Checks if Android version is in frame
     *
     * @param min version
     * @param max version
     * @return true if Android version is in frame
     */
    private static boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }

    /**
     * Builds license key
     *
     * @return license key
     */
    public static String buildLicenseKey() {
        StringBuilder keyBuilder = new StringBuilder();
        //TODO insert license code

        return keyBuilder.toString();
    }

    /**
     * Checks if purchase payload equls userId
     *
     * @param p purchase
     * @return true if equal
     */
    public static boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return payload.equals(MainActivity.getUserId());

    }

    /**
     * Encrypts string
     *
     * @param input string
     * @return encrypted string
     */
    public static String encrypt(String input) {
        // Simple encryption, not very strong!
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    /**
     * Decrypts string
     *
     * @param input string
     * @return decrypted string
     */
    public static String decrypt(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }
}
