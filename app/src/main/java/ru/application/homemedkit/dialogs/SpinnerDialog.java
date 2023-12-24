package ru.application.homemedkit.dialogs;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static ru.application.homemedkit.helpers.DateHelper.setCalendarDates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;
import ru.application.homemedkit.graphics.Toasts;
import ru.application.homemedkit.pickers.CustomTimePicker;

public class SpinnerDialog extends MaterialAlertDialogBuilder {
    public static long SELECTION = -1;
    private final Context context;
    private final String textSave;
    private FlexboxLayout timesGroup, datesLayout;
    private TextInputLayout periodLayout;
    private final MaterialAutoCompleteTextView pickerInterval, pickerPeriod;
    private TextInputEditText startDate, finalDate, spinner;
    private View inflate;
    private int counter = 2;

    public SpinnerDialog(Context context, String type) {
        super(context);

        this.context = context;

        Activity activity = (Activity) context;
        pickerInterval = activity.findViewById(R.id.intake_edit_text_interval);
        pickerPeriod = activity.findViewById(R.id.intake_edit_text_period);

        setLayoutViews(activity);
        setDialogViews(activity);

        final String[] intervals = context.getResources().getStringArray(R.array.interval_types);
        final String textBack = context.getString(R.string.text_cancel);
        textSave = context.getString(R.string.text_save);

        setView(inflate)
                .setNegativeButton(textBack, (dialog, which) -> dismissClear(dialog))
                .setOnCancelListener(this::dismissClear);

        if (type.equals(intervals[0])) hourlyInterval();
        else if (type.equals(intervals[2])) weeklyInterval();
        else if (type.equals(intervals[3])) flexInterval();
        else setDaysPeriod();

        create().show();
    }

    private void setDaysPeriod() {
        datesLayout.setVisibility(INVISIBLE);
        setTitle(R.string.intake_daily_period)
                .setOnCancelListener(dialog -> pickerPeriod.setText(null))
                .setPositiveButton(textSave, this::clickDaysPeriod);
    }

    private void flexInterval() {
        datesLayout.setVisibility(INVISIBLE);
        setTitle(R.string.intake_daily_interval)
                .setPositiveButton(textSave, this::clickFlexInterval);
    }

    public void weeklyInterval() {
        setTitle(R.string.intake_weekly_interval)
                .setPositiveButton(textSave, this::clickWeeklyInterval);
        timesGroup.addView(new CustomTimePicker(context));
    }

    private void hourlyInterval() {
        datesLayout.setVisibility(INVISIBLE);
        setTitle(R.string.intake_hourly_interval)
                .setPositiveButton(textSave, this::clickHourlyInterval);
    }

    private void setLayoutViews(Activity activity) {
        timesGroup = activity.findViewById(R.id.linear_times_activity_intake);
        periodLayout = activity.findViewById(R.id.intake_input_layout_period);
        datesLayout = activity.findViewById(R.id.intake_layout_dates);
        startDate = activity.findViewById(R.id.intake_calendar_start);
        finalDate = activity.findViewById(R.id.intake_calendar_finish);
    }

    @SuppressLint("InflateParams")
    private void setDialogViews(Activity activity) {
        inflate = activity.getLayoutInflater().inflate(R.layout.dialog_interval, null);

        ExtendedFloatingActionButton minus = inflate.findViewById(R.id.number_picker_spinner_minus);
        ExtendedFloatingActionButton plus = inflate.findViewById(R.id.number_picker_spinner_plus);
        spinner = inflate.findViewById(R.id.number_picker_spinner);

        spinner.setText(String.valueOf(counter));
        minus.setOnClickListener(v -> changeNumber(false));
        plus.setOnClickListener(v -> changeNumber(true));
    }

    private void changeNumber(boolean inc) {
        if (inc) counter++;
        else if (counter > 2)
            counter--;
        spinner.setText(String.valueOf(counter));
    }

    private void clickFlexInterval(DialogInterface dialog, int which) {
        IntakeActivity.intervalType += "_" + counter;
        timesGroup.addView(new CustomTimePicker(context));
        periodLayout.setVisibility(VISIBLE);
    }

    private void clickDaysPeriod(DialogInterface dialog, int which) {
        String value = String.valueOf(spinner.getText());
        counter = Integer.parseInt(value);
        if (counter > 0) {
            setCalendarDates(startDate, finalDate, counter);
            datesLayout.setVisibility(VISIBLE);
        } else new Toasts(context, R.string.text_wrong_period);
    }

    private void clickHourlyInterval(DialogInterface dialog, int which) {
        String value = String.valueOf(spinner.getText());
        counter = Integer.parseInt(value);
        if (counter > 1) {
            for (int i = 0; i < counter; i++) {
                timesGroup.addView(new CustomTimePicker(context, i));
            }
            periodLayout.setVisibility(VISIBLE);
        } else new Toasts(context, R.string.text_wrong_interval);
    }

    private void clickWeeklyInterval(DialogInterface dialog, int which) {
        String finish = setCalendarDates(SELECTION, counter);
        String interval = context.getResources().getStringArray(R.array.interval_types_name)[2];

        finalDate.setText(finish);
        pickerInterval.setText(interval);
        datesLayout.setVisibility(VISIBLE);
    }

    private void dismissClear(DialogInterface dialog) {
        pickerInterval.setText(null);
        dialog.dismiss();
    }
}
