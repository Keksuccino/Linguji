package de.keksuccino.polyglot.polyglot.backend.util;

import java.text.DateFormat;
import java.util.Date;

public class DateUtils {

    public static String getShortDateTimeString() {
        Date d = new Date();
        DateFormat format = DateFormat.getInstance();
        return format.format(d);
    }

    public static String getDateTimeString() {
        Date d = new Date();
        DateFormat format = DateFormat.getDateTimeInstance();
        return format.format(d);
    }

}
