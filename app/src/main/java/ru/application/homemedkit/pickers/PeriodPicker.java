package ru.application.homemedkit.pickers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ru.application.homemedkit.helpers.ConstantsHelper.PERIOD;
import static ru.application.homemedkit.helpers.DateHelper.setCalendarDates;

import android.view.View;
import android.widget.AdapterView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;
import ru.application.homemedkit.dialogs.SpinnerDialog;
import ru.application.homemedkit.helpers.SettingsHelper;

public class PeriodPicker implements AdapterView.OnItemClickListener {

    private static final int WEEK = 7;
    private static final int MONTH = 30;
    private static final int INDEFINITE = 50000;
    private final IntakeActivity activity;
    private final FlexboxLayout datesLayout;
    private final TextInputEditText startDate, finalDate;
    private final String[] periods;

    public PeriodPicker(IntakeActivity activity) {
        this.activity = activity;

        startDate = activity.findViewById(R.id.intake_calendar_start);
        finalDate = activity.findViewById(R.id.intake_calendar_finish);
        datesLayout = activity.findViewById(R.id.intake_layout_dates);

        periods = activity.getResources().getStringArray(R.array.period_types);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Objects.requireNonNull(startDate.getText()).clear();
        Objects.requireNonNull(finalDate.getText()).clear();

        boolean isLight = new SettingsHelper(activity).getLightPeriod();
        if (!isLight) {
            startDate.setOnClickListener(new CustomRangePicker(activity));
            finalDate.setOnClickListener(new CustomRangePicker(activity));
        } else {
            startDate.setOnClickListener(null);
            finalDate.setOnClickListener(null);
        }

        switch (position) {
            case 0 -> {
                IntakeActivity.periodType = periods[0];
                setCalendarDates(startDate, finalDate, WEEK);
                datesLayout.setVisibility(VISIBLE);
            }
            case 1 -> {
                IntakeActivity.periodType = periods[1];
                setCalendarDates(startDate, finalDate, MONTH);
                datesLayout.setVisibility(VISIBLE);
            }
            case 2 -> {
                IntakeActivity.periodType = periods[2];
                startDate.setOnClickListener(null);
                finalDate.setOnClickListener(null);
                new SpinnerDialog(activity, PERIOD);
            }
            case 3 -> {
                IntakeActivity.periodType = periods[3];
                setCalendarDates(startDate, finalDate, INDEFINITE);
                datesLayout.setVisibility(GONE);
            }
        }
    }
}
