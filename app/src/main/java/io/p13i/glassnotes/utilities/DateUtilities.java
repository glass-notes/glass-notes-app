package io.p13i.glassnotes.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtilities {
    public static String formatDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    public static String nowAs(String pattern) {
        return formatDate(now(), pattern);
    }
}
