package ru.application.homemedkit.dialogs;

import static ru.application.homemedkit.helpers.DateHelper.toExpDate;
import static ru.application.homemedkit.helpers.DateHelper.toTimestamp;

import android.app.Activity;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;
import android.view.View;
import android.widget.NumberPicker;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;

public class ExpDateDialog extends MaterialAlertDialogBuilder {

    private final Activity activity;

    public ExpDateDialog(Context context) {
        super(context);

        activity = (Activity) context;

        View view = activity.getLayoutInflater().inflate(R.layout.dialog_exp_date, null);
        TextInputEditText expDate = activity.findViewById(R.id.medicine_scanned_exp_date);
        NumberPicker month = view.findViewById(R.id.dialog_exp_date_spinner_month);
        NumberPicker year = view.findViewById(R.id.dialog_exp_date_picker_year);

        setMonth(month);
        setYear(year);

        setView(view)
                .setTitle(R.string.text_enter_exp_date)
                .setPositiveButton(R.string.text_save, (dialog, which) -> setDate(month, year, expDate));

        create().show();
    }

    private void setMonth(NumberPicker month) {
        String[] months = activity.getResources().getStringArray(R.array.months_name);

        month.setDisplayedValues(months);
        month.setWrapSelectorWheel(false);
        month.setMinValue(0);
        month.setMaxValue(11);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            month.setTextSize(52);
        }
    }

    private void setYear(NumberPicker year) {
        year.setWrapSelectorWheel(false);
        year.setMinValue(2000);
        year.setMaxValue(2099);
        year.setValue(Calendar.getInstance().get(Calendar.YEAR));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            year.setTextSize(52);
        }
    }

    private void setDate(NumberPicker month, NumberPicker year, TextInputEditText expDate) {
        String until = activity.getString(R.string.exp_date_until);
        int intMonth = month.getValue();
        int intYear = year.getValue();

        MedicineActivity.timestamp = toTimestamp(intMonth, intYear);
        expDate.setText(String.format(until, toExpDate(intMonth, intYear)));
    }
}
