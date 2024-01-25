package ru.application.homemedkit.helpers;

import static ru.application.homemedkit.helpers.ConstantsHelper.BLANK;
import static ru.application.homemedkit.helpers.ConstantsHelper.DOWN_DASH;
import static ru.application.homemedkit.helpers.ConstantsHelper.SEMICOLON;
import static ru.application.homemedkit.helpers.ConstantsHelper.WHITESPACE_R;
import static ru.application.homemedkit.helpers.DateHelper.sortTimes;

import android.content.Context;

import androidx.core.text.HtmlCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import ru.application.homemedkit.R;

public class StringHelper {
    public static String formName(String name) {
        int i = name.indexOf(' ');
        return i > 0 ? name.substring(0, i) : name;
    }

    public static String shortName(String name) {
        int i = name.indexOf(',');
        if (i > 0) return name.substring(0, i);
        else return name;
    }

    public static String fromHTML(String text) {
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString();
    }

    public static String timesString(FlexboxLayout layout) {
        int childCount = layout.getChildCount();
        StringBuilder times = new StringBuilder(childCount * 7);
        for (int i = 0; i < childCount; i++)
            times.append(((Chip) layout.getChildAt(i)).getText()).append(SEMICOLON);
        times.deleteCharAt(times.length() - 1);

        String sorted = Arrays.toString(sortTimes(times.toString().strip()));

        return sorted.substring(1, sorted.length() - 1).replaceAll(WHITESPACE_R, BLANK);
    }

    public static int daysInterval(String interval) {
        String amount = interval.split(DOWN_DASH)[1];
        return Integer.parseInt(amount);
    }

    public static String intervalName(Context context, String interval) {
        String[] array = context.getResources().getStringArray(R.array.interval_types);
        String[] intervals = context.getResources().getStringArray(R.array.interval_types_name);

        if (interval.equals(array[0])) return intervals[0];
        else if (interval.equals(array[1])) return intervals[1];
        else if (interval.equals(array[2])) return intervals[2];
        else return intervals[3];
    }

    public static double parseAmount(String input) {
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        try {
            return Objects.requireNonNull(format.parse(input)).doubleValue();
        } catch (ParseException | NullPointerException e) {
            return 0;
        }
    }

    public static String decimalFormat(double amount) {
        return new DecimalFormat("0.###").format(amount);
    }
}
