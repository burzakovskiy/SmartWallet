package com.rbsoftware.pfm.personalfinancemanager.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Holds utils static method to work with dates
 *
 * @author Roman Burzakovskiy
 */
public class DateUtils {

    public static final int DATE_FORMAT_SHORT = 0;
    public static final int DATE_FORMAT_MEDIUM = 1;
    public static final int DATE_FORMAT_LONG = 2;
    public static final int DATE_FORMAT_FULL = 3;

    //Query time frames
    public static final String THIS_WEEK = "thisWeek";
    public static final String LAST_WEEK = "lastWeek";
    public static final String THIS_MONTH = "thisMonth";
    public static final String LAST_MONTH = "lastMonth";
    public static final String THIS_YEAR = "thisYear";
    public static final String THREE_WEEKS = "threeWeeks";
    public static final String THREE_MONTHS = "threeMonths";
    public static final String FROM_START = "fromStart";

    /**
     * Calculates the first day of current month
     *
     * @return the first day of current month
     */
    public static Long getFirstDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());

        cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the first day of current week
     *
     * @return the first day of current week
     */
    public static Long getFirstDateOfCurrentWeek() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the first day of current year
     *
     * @return the first day of current year
     */
    public static Long getFirstDateOfCurrentYear() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeZone(cal.getTimeZone());
        cal.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the first day of last month
     *
     * @return the first day of last month
     */
    public static Long getFirstDateOfPreviousMonth() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the first day of 3 month ago
     *
     * @return the first day of 3 month ago
     */
    public static Long getFirstDateOfTwoMonthsAgo() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.MONTH, -2);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the last day of last month
     *
     * @return the last day of last month
     */
    public static Long getLastDateOfPreviousMonth() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the first day of last week
     *
     * @return the first day of last week
     */
    public static Long getFirstDateOfPreviousWeek() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the first day of three weeks ago
     *
     * @return the first day of three weeks ago
     */
    public static Long getFirstDateOfTwoWeeksAgo() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.add(Calendar.WEEK_OF_YEAR, -2);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the last day of last week
     *
     * @return the last day of last week
     */
    public static Long getLastDateOfPreviousWeek() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Calculates the start date
     *
     * @return the Jan 1st 2016
     */
    public static Long getStartDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    /**
     * Converts unix date into human readable
     *
     * @param format date of the date
     * @return human readable date
     */

    public static String getNormalDate(int format, String date) {
        Date formatDate = new Date(Long.valueOf(date) * 1000L); // *1000 is to convert seconds to milliseconds
        DateFormat sdf;
        switch (format) {
            case DATE_FORMAT_SHORT: //short
                if (!Locale.getDefault().equals(Locale.US)) {
                    sdf = new SimpleDateFormat("dd.MM", Locale.getDefault()); // the format of your date
                } else {
                    sdf = new SimpleDateFormat("MM.dd", Locale.getDefault()); // the format of your date
                }
                break;
            case DATE_FORMAT_MEDIUM: //medium
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // the format of your date
                break;
            case DATE_FORMAT_LONG: // long
                sdf = DateFormat.getDateInstance(DATE_FORMAT_LONG, Locale.getDefault());
                break;
            case DATE_FORMAT_FULL: //full
                sdf = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
                break;
            default:
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // the format of your date
                break;
        }
        sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
        return sdf.format(formatDate);
    }
}
