package ru.application.homemedkit.dialogs;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static ru.application.homemedkit.helpers.DateHelper.setCalendarDates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
    private FlexboxLayout timesGroup, datesLayout;
    private TextInputLayout periodLayout;
    private TextInputEditText startDate, finalDate, spinner;
    private View inflate;
    private int counter = 2;

    public SpinnerDialog(Context context, String type) {
        super(context);

        Activity activity = (Activity) context;
        MaterialAutoCompleteTextView pickerInterval = activity.findViewById(R.id.intake_edit_text_interval);
        MaterialAutoCompleteTextView pickerPeriod = activity.findViewById(R.id.intake_edit_text_period);

        setLayoutViews(activity);
        setDialogViews(activity);


        final String[] intervals = context.getResources().getStringArray(R.array.interval_types);
        final String textSave = context.getString(R.string.text_save);
        final String textBack = context.getString(R.string.text_cancel);

        setView(inflate)
                .setNegativeButton(textBack, (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(dialog -> pickerInterval.setText(null));

        if (type.equals(intervals[0])) hourlyInterval(context, textSave);
        else if (type.equals(intervals[2])) weeklyInterval(context, textSave);
        else if (type.equals(intervals[3])) flexDailyInterval(context, textSave);
        else setDaysPeriod(context, pickerPeriod, textSave);

        create().show();
    }

    private void setDaysPeriod(Context context, MaterialAutoCompleteTextView pickerPeriod, String textSave) {
        datesLayout.setVisibility(INVISIBLE);
        setTitle(R.string.intake_daily_period)
                .setOnCancelListener(dialog -> pickerPeriod.setText(null))
                .setPositiveButton(textSave, (dialog, which) -> {
                    counter = Integer.parseInt(String.valueOf(spinner.getText()));
                    if (counter > 0) {
                        setCalendarDates(startDate, finalDate, counter);
                        datesLayout.setVisibility(VISIBLE);
                    } else new Toasts(context, R.string.text_wrong_period);
                });
    }

    private void flexDailyInterval(Context context, String textSave) {
        periodLayout.setVisibility(INVISIBLE);
        datesLayout.setVisibility(INVISIBLE);
        setTitle(R.string.intake_daily_interval)
                .setPositiveButton(textSave, (dialog, which) -> {
                    IntakeActivity.intervalType += "_" + counter;
                    timesGroup.addView(new CustomTimePicker(context));
                    periodLayout.setVisibility(VISIBLE);
                });
    }

    private void weeklyInterval(Context context, String textSave) {
        setTitle(R.string.intake_weekly_interval)
                .setPositiveButton(textSave, (dialog, which) -> {
                    setCalendarDates(startDate, finalDate, 7 * counter);
                    datesLayout.setVisibility(VISIBLE);
                });
        timesGroup.addView(new CustomTimePicker(context));
    }

    private void hourlyInterval(Context context, String textSave) {
        periodLayout.setVisibility(INVISIBLE);
        datesLayout.setVisibility(INVISIBLE);
        setTitle(R.string.intake_hourly_interval)
                .setPositiveButton(textSave, (dialog, which) -> {
                    counter = Integer.parseInt(String.valueOf(spinner.getText()));
                    if (counter > 1) {
                        for (int i = 0; i < counter; i++) {
                            timesGroup.addView(new CustomTimePicker(context, i));
                        }
                        periodLayout.setVisibility(VISIBLE);
                    } else new Toasts(context, R.string.text_wrong_interval);
                });
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
}
