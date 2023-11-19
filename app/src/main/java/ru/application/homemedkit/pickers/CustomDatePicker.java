package ru.application.homemedkit.pickers;

import static ru.application.homemedkit.helpers.ConstantsHelper.START_DATE;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZonedDateTime;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;
import ru.application.homemedkit.dialogs.SpinnerDialog;
import ru.application.homemedkit.helpers.DateHelper;

public class CustomDatePicker extends DialogFragment {
    private static final SimpleDateFormat RUS = new SimpleDateFormat("dd.MM.yyyy", DateHelper.RUSSIAN);
    private static final long MILLI = ZonedDateTime.now(Clock.systemDefaultZone()).toInstant().toEpochMilli();
    private static final CalendarConstraints constraints = new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now()).setStart(MILLI).build();

    public CustomDatePicker(@NonNull IntakeActivity activity) {
        TextInputEditText startDate = activity.findViewById(R.id.intake_calendar_start);

        String interval = activity.getResources().getStringArray(R.array.interval_types)[2];

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(R.string.intake_weekly_period_choose_day)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setTextInputFormat(RUS)
                .setCalendarConstraints(constraints)
                .build();

        picker.show(activity.getSupportFragmentManager(), START_DATE);
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate.setText(DateHelper.formatIntake(selection));
            new SpinnerDialog(activity, interval);
        });
    }
}
