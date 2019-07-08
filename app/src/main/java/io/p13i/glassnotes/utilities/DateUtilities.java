package io.p13i.glassnotes.utilities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtilities {
    public static final String TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ssZ";

    @SuppressLint("SimpleDateFormat")
    public static String formatDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    public static String nowAs(String pattern) {
        return formatDate(now(), pattern);
    }

    public static String timestamp() {
        return nowAs(TIMESTAMP);
    }
}
