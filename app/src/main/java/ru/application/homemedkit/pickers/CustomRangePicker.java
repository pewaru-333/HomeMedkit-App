package ru.application.homemedkit.pickers;

import static com.google.android.material.datepicker.MaterialDatePicker.Builder;
import static com.google.android.material.datepicker.MaterialDatePicker.INPUT_MODE_CALENDAR;
import static com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds;
import static ru.application.homemedkit.helpers.ConstantsHelper.RUS;
import static ru.application.homemedkit.helpers.ConstantsHelper.START_DATE;
import static ru.application.homemedkit.helpers.DateHelper.formatIntake;

import android.app.AlarmManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;

public class CustomRangePicker implements View.OnClickListener {
    private final AppCompatActivity activity;
    private final MaterialDatePicker<Pair<Long, Long>> picker;
    private final MaterialAutoCompleteTextView period;
    private final TextInputEditText start, finish;

    public CustomRangePicker(AppCompatActivity activity) {
        this.activity = activity;

        period = activity.findViewById(R.id.intake_edit_text_period);
        start = activity.findViewById(R.id.intake_calendar_start);
        finish = activity.findViewById(R.id.intake_calendar_finish);

        long now = todayInUtcMilliseconds();
        long week = now + 7 * AlarmManager.INTERVAL_DAY;
        long year = now + 365 * AlarmManager.INTERVAL_DAY;

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .setStart(now)
                .setEnd(year)
                .build();

        picker = Builder.dateRangePicker()
                .setTitleText(R.string.text_pick_period)
                .setInputMode(INPUT_MODE_CALENDAR)
                .setTextInputFormat(RUS)
                .setCalendarConstraints(constraints)
                .setSelection(new Pair<>(now, week))
                .build();
    }

    @Override
    public void onClick(View v) {
        picker.show(activity.getSupportFragmentManager(), START_DATE);
        picker.addOnPositiveButtonClickListener(this::onPositiveButtonClick);
    }

    private void onPositiveButtonClick(Pair<Long, Long> selection) {
        String periodL = activity.getResources().getStringArray(R.array.period_types_name)[2];
        String first = formatIntake(selection.first);
        String second = formatIntake(selection.second);

        period.setText(periodL);
        start.setText(first);
        finish.setText(second);

        IntakeActivity.periodType = activity.getResources().getStringArray(R.array.period_types)[2];
    }
}
