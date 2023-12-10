package ru.application.homemedkit.pickers;

import static android.view.View.VISIBLE;

import android.view.View;
import android.widget.AdapterView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;
import ru.application.homemedkit.dialogs.SpinnerDialog;

public class IntervalPicker implements AdapterView.OnItemClickListener {
    private final IntakeActivity activity;
    private final FlexboxLayout timesGroup;
    private final TextInputLayout periodLayout;
    private final MaterialAutoCompleteTextView period;
    private final String[] intervals, periods, periodsL;

    public IntervalPicker(IntakeActivity activity) {
        this.activity = activity;

        timesGroup = activity.findViewById(R.id.linear_times_activity_intake);
        periodLayout = activity.findViewById(R.id.intake_input_layout_period);
        period = activity.findViewById(R.id.intake_edit_text_period);

        intervals = activity.getResources().getStringArray(R.array.interval_types);
        periods = activity.getResources().getStringArray(R.array.period_types);
        periodsL = activity.getResources().getStringArray(R.array.period_types_name);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        timesGroup.removeAllViews();
        switch (position) {
            case 0 -> {
                IntakeActivity.intervalType = intervals[0];
                new SpinnerDialog(activity, IntakeActivity.intervalType);
            }
            case 1 -> {
                IntakeActivity.intervalType = intervals[1];
                periodLayout.setVisibility(VISIBLE);
                timesGroup.addView(new CustomTimePicker(activity));
            }
            case 2 -> {
                IntakeActivity.intervalType = intervals[2];
                IntakeActivity.periodType = periods[2];
                periodLayout.setVisibility(View.GONE);
                period.setText(periodsL[2], false);
                new CustomDatePicker(activity);
            }
            case 3 -> {
                IntakeActivity.intervalType = intervals[3];
                new SpinnerDialog(activity, IntakeActivity.intervalType);
            }
        }
    }
}
