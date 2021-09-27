package com.atguigu.gmall.common.util;

import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date operation tools
 *
 */
public class DateUtil {

    private static final String dateFormat = "yyyy-MM-dd";

    /**
     * Get two time difference unit: second
     * @param date1
     * @param date2
     * @return
     */
    public Long getTimeSubtract(Date date1, Date date2) {
        return (date1.getTime()-date2.getTime()) / 1000;
    }

    /**
     * Format date
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);

    }

    /**
     * Intercept and compare the value at the field of the two date objects.
     * If the first date is less than, equal to, or greater than the second, it will return a negative integer, 0, and a positive integer accordingly
     *
     * @param date1 The first date object, not null
     * @param date2 The second date object, not null
     * @param field Calendar threshold
     * <p>
     * date1> date2 returns: 1
     * date1 = date2 returns: 0
     * date1 <date2 returns: -1
     */
    public static int truncatedCompareTo(final Date date1, final Date date2, final int field) {
        return DateUtils.truncatedCompareTo(date1, date2, field);
    }

    /**
     * Compare the time size
     * @param beginDate
     * @param endDate
     * @return
     */
    public static boolean dateCompare(Date beginDate, Date endDate) {
        // endDate> beginDate
        if (DateUtil.truncatedCompareTo(beginDate, endDate, Calendar.SECOND) == 1) {
            return false;
        }
        // beginDate <= endDate
        return true;
    }
}