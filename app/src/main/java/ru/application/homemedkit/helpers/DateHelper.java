package ru.application.homemedkit.helpers;

import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;
import static ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DateHelper {
    public static final ZoneOffset ZONE = ZoneId.systemDefault().getRules().getOffset(Instant.now());
    public static final DateTimeFormatter FORMAT_L = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    public static final DateTimeFormatter FORMAT_S = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter FORMAT_D_H = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm");
    public static final DateTimeFormatter FORMAT_D_M_Y = DateTimeFormatter.ofPattern("d MMMM yyyy");
    public static final DateTimeFormatter FORMAT_D_M = DateTimeFormatter.ofPattern("d MMMM, E");
    public static final DateTimeFormatter FORMAT_H = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter FORMAT_M_Y = DateTimeFormatter.ofPattern("MM/yyyy");

    public static String toExpDate(long milli) {
        return milli > 0 ? getDateTime(milli).format(FORMAT_L) : BLANK;
    }

    public static String toExpDate(int month, int year) {
        return LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()).format(FORMAT_L);
    }

    public static long toTimestamp(int month, int year) {
        return LocalDateTime.of(year, month, YearMonth.of(year, month).lengthOfMonth(),
                LocalTime.MAX.getHour(), LocalTime.MAX.getMinute()).toInstant(ZONE).toEpochMilli();
    }

    public static String inCard(long milli) {
        return milli == -1L ? BLANK : getDateTime(milli).format(FORMAT_M_Y);
    }

    public static String formatIntake(long milli) {
        return getDateTime(milli).format(FORMAT_S);
    }

    public static String clockIntake(MaterialTimePicker picker) {
        return LocalTime.of(picker.getHour(), picker.getMinute()).format(FORMAT_H);
    }

    public static void setCalendarDates(TextInputEditText editS, TextInputEditText editF, int days) {
        long today = System.currentTimeMillis();

        String textS = formatIntake(today);
        String textF = getDateTime(today).toLocalDate().plusDays(days - 1).format(FORMAT_S);

        editS.setText(textS);
        editF.setText(textF);
    }

    public static String setCalendarDates(long start, int weeks) {
        return getDateTime(start).toLocalDate().plusWeeks(weeks - 1).format(FORMAT_S);
    }

    public static long longSecond(String textD, String textT) {
        var date = LocalDate.parse(textD, FORMAT_S);
        var time = LocalTime.parse(textT, FORMAT_H);
        var unix = LocalDateTime.of(date, time);

        if (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
            unix = unix.plusDays(1);
        }

        return unix.toInstant(ZONE).toEpochMilli();
    }

    public static long[] longSeconds(String start, String time) {
        List<String> times = Arrays.asList(sortTimes(time));
        ArrayList<Long> triggers = new ArrayList<>(times.size());

        for (int i = 0; i < times.size(); i++) triggers.add(longSecond(start, times.get(i)));

        return triggers.stream().mapToLong(Long::longValue).toArray();
    }

    public static long lastDay(String finish) {
        LocalDate date = LocalDate.parse(finish, FORMAT_S);

        return LocalDateTime.of(date, LocalTime.now()).toInstant(ZONE).toEpochMilli();
    }

    public static long expirationCheckTime() {
        LocalDateTime unix = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));

        if (unix.toInstant(ZONE).toEpochMilli() < System.currentTimeMillis()) {
            unix = unix.plusDays(1);
        }

        return unix.toInstant(ZONE).toEpochMilli();
    }

    public static String[] sortTimes(String time) {
        List<String> times = Arrays.asList(time.split(SEMICOLON));
        times.sort(Comparator.comparing(item -> LocalTime.parse(item, FORMAT_H)));
        return times.toArray(new String[]{});
    }

    private static ZonedDateTime getDateTime(long milli) {
        return Instant.ofEpochMilli(milli).atZone(ZONE);
    }
}
