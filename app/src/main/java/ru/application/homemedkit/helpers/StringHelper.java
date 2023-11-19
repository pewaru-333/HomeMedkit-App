package ru.application.homemedkit.helpers;

import android.content.Context;

import androidx.core.text.HtmlCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

import ru.application.homemedkit.R;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.Technical;

public class StringHelper {

    public static Medicine parseJSON(String data) {
        try {
            JSONObject parsedJSON = new JSONObject(data);
            JSONObject drugsData = parsedJSON.getJSONObject("drugsData");
            JSONObject foiv = drugsData.getJSONObject("foiv");
            JSONObject vidalData = drugsData.getJSONObject("vidalData");
            return new Medicine(
                    parsedJSON.getString("cis"),
                    parsedJSON.getString("productName"),
                    drugsData.getLong("expireDate"),
                    foiv.getString("prodFormNormName"),
                    foiv.getString("prodDNormName"),
                    vidalData.getString("phKinetics"),
                    new Technical(Boolean.TRUE, Boolean.TRUE));
        } catch (JSONException e) {
            return new Medicine("no", -1L);
        }
    }

    public static String formatCode(String code) {
        StringBuilder cis = new StringBuilder(code);
        cis.deleteCharAt(0);

        cis.replace(31, 32, "\u001D");
        cis.replace(38, 39, "\u001D");

        return cis.toString();
    }

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
            times.append(((TextInputEditText) layout.getChildAt(i)).getText()).append(",");
        times.deleteCharAt(times.length() - 1);

        return times.toString();
    }

    public static int daysInterval(String interval) {
        String amount = interval.split("_")[1];
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
        DecimalFormat decimal = new DecimalFormat("0.###");
        return decimal.format(amount);
    }
}
