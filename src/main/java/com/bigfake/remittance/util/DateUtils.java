package com.bigfake.remittance.util;
import java.text.*; import java.util.*;
public final class DateUtils {
    private DateUtils() {}
    public static Date parseLegacy(String value) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd"); format.setLenient(false); return format.parse(value);
    }
    public static Date todayPlusDays(int days) { Calendar calendar=Calendar.getInstance(); calendar.add(Calendar.DAY_OF_MONTH, days); return calendar.getTime(); }
}
