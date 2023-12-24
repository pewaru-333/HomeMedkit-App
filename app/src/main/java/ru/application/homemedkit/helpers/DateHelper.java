package ru.application.homemedkit.helpers;

import static android.icu.text.DateFormat.LONG;
import static android.icu.text.DateFormat.SHORT;
import static android.icu.text.DateFormat.getDateInstance;
import static android.icu.text.DateFormat.getInstanceForSkeleton;
import static android.icu.text.DateFormat.getTimeInstance;
import static android.icu.util.Calendar.DATE;
import static android.icu.util.Calendar.DAY_OF_MONTH;
import static android.icu.util.Calendar.HOUR_OF_DAY;
import static android.icu.util.Calendar.MILLISECOND;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.SECOND;
import static android.icu.util.Calendar.WEEK_OF_YEAR;
import static android.icu.util.Calendar.YEAR;
import static android.icu.util.Calendar.getInstance;
import static android.icu.util.ULocale.US;
import static android.icu.util.ULocale.getDefault;
import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;
import static ru.application.homemedkit.helpers.ConstantsHelper.PATTERN;
import static ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DateHelper {
    public static final Locale RUSSIAN = Locale.forLanguageTag("ru");
    private static final DateFormat formatLong = getDateInstance(LONG, getDefault());
    private static final DateFormat formatShort = getDateInstance(SHORT, RUSSIAN);

    public static String toExpDate(long timestamp) {
        Calendar calendar = getInstance();
        calendar.setTimeInMillis(timestamp);

        return timestamp > 0 ? formatLong.format(calendar) : BLANK;
    }

    public static String toExpDate(int month, int year) {
        Calendar calendar = getInstance();

        calendar.set(YEAR, year);
        calendar.set(MONTH, month);
        calendar.set(DAY_OF_MONTH, calendar.getActualMaximum(DAY_OF_MONTH));

        return formatLong.format(calendar.getTime());
    }

    public static long toTimestamp(int month, int year) {
        Calendar calendar = getInstance();

        calendar.set(YEAR, year);
        calendar.set(MONTH, month);
        calendar.set(DAY_OF_MONTH, calendar.getActualMaximum(DAY_OF_MONTH));

        return calendar.getTimeInMillis();
    }

    public static String inCard(long timestamp) {
        Calendar calendar = getInstance();
        calendar.setTimeInMillis(timestamp);

        String skeleton = "MM/yyyy";
        DateFormat dateFormat = getInstanceForSkeleton(skeleton, US);

        return timestamp == -1L ? BLANK : dateFormat.format(calendar.getTime());
    }

    public static String formatIntake(long date) {
        Calendar calendar = getInstance();
        calendar.setTimeInMillis(date);

        return formatShort.format(calendar.getTime());
    }

    public static String clockIntake(MaterialTimePicker picker) {
        DateFormat formatTime = getTimeInstance(SHORT, RUSSIAN);

        Calendar calendar = getInstance();
        calendar.set(HOUR_OF_DAY, picker.getHour());
        calendar.set(MINUTE, picker.getMinute());
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);

        return formatTime.format(calendar.getTimeInMillis());
    }

    public static void setCalendarDates(TextInputEditText start, TextInputEditText finish, int days) {
        Calendar calendar = Calendar.getInstance();
        String textStart = formatIntake(calendar.getTimeInMillis());
        start.setText(textStart);

        calendar.add(DAY_OF_MONTH, days - 1);
        String textFinish = formatShort.format(calendar.getTime());
        finish.setText(textFinish);
    }

    public static String setCalendarDates(long start, int weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        calendar.add(WEEK_OF_YEAR, weeks);

        return formatShort.format(calendar.getTime());
    }

    public static long longSecond(String start, String time) {
        String pattern = "H:mm";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(PATTERN, RUSSIAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(pattern, RUSSIAN);
        LocalDate date = LocalDate.parse(start, dateFormatter);
        LocalTime lt = LocalTime.parse(time, timeFormatter);

        Calendar calendar = getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(YEAR, date.getYear());
        calendar.set(MONTH, date.getMonthValue() - 1);
        calendar.set(DATE, date.getDayOfMonth());
        calendar.set(HOUR_OF_DAY, lt.getHour());
        calendar.set(MINUTE, lt.getMinute());
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(DATE, 1);
        }

        return calendar.getTimeInMillis();
    }

    public static long[] longSeconds(String start, String time) {
        List<String> times = Arrays.asList(sortTimes(time));
        ArrayList<Long> triggers = new ArrayList<>(times.size());

        for (int i = 0; i < times.size(); i++) triggers.add(longSecond(start, times.get(i)));

        return triggers.stream().mapToLong(Long::longValue).toArray();
    }

    public static long lastDay(String finish) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN, RUSSIAN);
        LocalDate date = LocalDate.parse(finish, formatter);

        Calendar calendar = getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(DATE, date.getDayOfMonth());
        calendar.set(MONTH, date.getMonthValue() - 1);
        calendar.set(YEAR, date.getYear());

        return calendar.getTimeInMillis();
    }

    public static long expirationCheckTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(HOUR_OF_DAY, 12);
        calendar.set(MINUTE, 0);
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(DATE, 1);
        }

        return calendar.getTimeInMillis();
    }

    public static String[] sortTimes(String time) {
        List<String> times = Arrays.asList(time.split(SEMICOLON));
        Collections.sort(times);
        return times.toArray(new String[]{});
    }
}
