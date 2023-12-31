package ru.application.homemedkit.pickers;

import static com.google.android.material.datepicker.MaterialDatePicker.Builder;
import static com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR;
import static com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds;
import static ru.application.homemedkit.helpers.ConstantsHelper.RUS;
import static ru.application.homemedkit.helpers.ConstantsHelper.START_DATE;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import ru.application.homemedkit.R;
import ru.application.homemedkit.dialogs.SpinnerDialog;
import ru.application.homemedkit.helpers.DateHelper;

public class CustomDatePicker extends DialogFragment {
    private final AppCompatActivity activity;
    private final MaterialDatePicker<Long> picker;
    private final MaterialAutoCompleteTextView interval;

    public CustomDatePicker(AppCompatActivity activity) {
        this.activity = activity;

        interval = activity.findViewById(R.id.intake_edit_text_interval);

        long now = todayInUtcMilliseconds();
        long year = now + 365 * AlarmManager.INTERVAL_DAY;

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .setStart(now)
                .setEnd(year)
                .build();

        picker = Builder.datePicker()
                .setTitleText(R.string.intake_weekly_period_choose_day)
                .setInputMode(INPUT_MODE_CALENDAR)
                .setTextInputFormat(RUS)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnDismissListener(this::dismissAll);
        picker.addOnCancelListener(this::dismissAll);

        show(activity.getSupportFragmentManager(), START_DATE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        picker.show(activity.getSupportFragmentManager(), START_DATE);
        picker.addOnPositiveButtonClickListener(this::onPositiveButtonClick);

        return super.onCreateDialog(savedInstanceState);
    }

    private void onPositiveButtonClick(Long selection) {
        TextInputEditText startDate = activity.findViewById(R.id.intake_calendar_start);
        String interval = activity.getResources().getStringArray(R.array.interval_types)[2];

        String text = DateHelper.formatIntake(selection);
        startDate.setText(text);

        dismiss();
        SpinnerDialog.SELECTION = selection;
        new SpinnerDialog(activity, interval);
    }

    private void dismissAll(DialogInterface dialogInterface) {
        interval.setText(null);
        dialogInterface.dismiss();
        dismiss();
    }
}
