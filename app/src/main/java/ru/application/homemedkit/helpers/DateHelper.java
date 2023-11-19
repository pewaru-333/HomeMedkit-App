package ru.application.homemedkit.helpers;

import static android.icu.util.Calendar.DATE;
import static android.icu.util.Calendar.DAY_OF_MONTH;
import static android.icu.util.Calendar.HOUR_OF_DAY;
import static android.icu.util.Calendar.MILLISECOND;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.SECOND;
import static android.icu.util.Calendar.YEAR;
import static android.icu.util.Calendar.getInstance;
import static java.lang.String.valueOf;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DateHelper {
    public static final SimpleDateFormat DAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final Locale RUSSIAN = Locale.forLanguageTag("ru");
    public static final SimpleDateFormat RUS = new SimpleDateFormat("MMMM yyyy года", RUSSIAN);
    public static final SimpleDateFormat FMT = new SimpleDateFormat("dd.MM.yyyy", RUSSIAN);
    public static final DateTimeFormatter FMT_D = DateTimeFormatter.ofPattern("dd.MM.yyyy", RUSSIAN);
    public static final SimpleDateFormat TIME = new SimpleDateFormat("H:mm", RUSSIAN);
    private static final DateTimeFormatter TIME_F = DateTimeFormatter.ofPattern("H:mm", RUSSIAN);


    public static String toExpDate(long timestamp) {
        Calendar calendar = getInstance();
        calendar.setTimeInMillis(timestamp);

        return timestamp > 0 ? RUS.format(calendar) : "";
    }

    public static String toExpDate(int month, int year) {
        Calendar calendar = getInstance();

        calendar.set(YEAR, year);
        calendar.set(MONTH, month);
        calendar.set(DAY_OF_MONTH, calendar.getActualMaximum(DAY_OF_MONTH));

        return RUS.format(calendar.getTime());
    }

    public static long toTimestamp(String date) {
        Calendar calendar = getInstance();
        if (date.length() > 0)
            try {
                long time = RUS.parse(date.substring(3)).getTime();
                calendar.setTimeInMillis(time);
                calendar.set(DAY_OF_MONTH, calendar.getActualMaximum(DAY_OF_MONTH));
                return calendar.getTimeInMillis();
            } catch (ParseException e) {
                return -1L;
            }
        else
            return -1L;
    }

    public static String inCard(long timestamp) {
        Calendar calendar = getInstance();
        calendar.setTimeInMillis(timestamp);

        var RUS = new SimpleDateFormat("MM/yyyy", RUSSIAN);

        return RUS.format(calendar.getTime());
    }

    public static String formatIntake(long date) {
        try {
            long dayMillis = 1000 * 60 * 60 * 24;
            return FMT.format(DAT.parse(LocalDate.ofEpochDay(date / dayMillis).toString()));
        } catch (ParseException e) {
            return valueOf(date);
        }
    }

    public static String clockIntake(@NonNull MaterialTimePicker picker) {
        String hour = String.valueOf(picker.getHour());
        String minute = String.valueOf(picker.getMinute());

        try {
            return TIME.format(TIME.parse(hour + ":" + minute));
        } catch (ParseException e) {
            return hour + ":" + minute;
        }
    }

    public static void setCalendarDates(TextInputEditText startDate, TextInputEditText finalDate,
                                        int days, boolean countToday) {
        LocalDate today = LocalDate.now();

        if (!countToday) startDate.setText(formatIntake(today));
        else days += 1;
        finalDate.setText(formatIntake(today.plusDays(days)));
    }

    public static String formatIntake(@NonNull LocalDate date) {
        try {
            return FMT.format(DAT.parse(date.toString()));
        } catch (ParseException e) {
            return valueOf(date);
        }
    }

    public static long longSecond(String time) {
        LocalTime lt = LocalTime.parse(time, TIME_F);

        Calendar calendar = getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(HOUR_OF_DAY, lt.getHour());
        calendar.set(MINUTE, lt.getMinute());
        calendar.set(SECOND, 0);
        calendar.set(MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(DATE, 1);
        }

        return calendar.getTimeInMillis();
    }

    public static long[] longSeconds(String time) {
        List<String> times = Arrays.asList(time.split(","));
        ArrayList<Long> triggers = new ArrayList<>(times.size());

        for (int i = 0; i < times.size(); i++) {
            LocalTime lt = LocalTime.parse(times.get(i), TIME_F);

            Calendar calendar = getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(HOUR_OF_DAY, lt.getHour());
            calendar.set(MINUTE, lt.getMinute());
            calendar.set(SECOND, 0);
            calendar.set(MILLISECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(DATE, 1);
            }

            triggers.add(calendar.getTimeInMillis());
        }

        return triggers.stream().mapToLong(Long::longValue).toArray();
    }

    public static long lastDay(String finish) {
        LocalDate date = LocalDate.parse(finish, FMT_D);

        Calendar calendar = getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(DATE, date.getDayOfMonth());
        calendar.set(MONTH, date.getMonthValue() - 1);
        calendar.set(YEAR, date.getYear());

        return calendar.getTimeInMillis();
    }
}
