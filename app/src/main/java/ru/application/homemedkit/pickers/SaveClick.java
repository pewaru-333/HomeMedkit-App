package ru.application.homemedkit.pickers;

import static java.lang.String.valueOf;
import static ru.application.homemedkit.activities.IntakeActivity.edit;
import static ru.application.homemedkit.activities.IntakeActivity.intervalType;
import static ru.application.homemedkit.activities.IntakeActivity.periodType;
import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_INTAKE;
import static ru.application.homemedkit.helpers.DateHelper.longSecond;
import static ru.application.homemedkit.helpers.DateHelper.longSeconds;
import static ru.application.homemedkit.helpers.StringHelper.parseAmount;
import static ru.application.homemedkit.helpers.StringHelper.timesString;

import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.stream.Stream;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MainActivity;
import ru.application.homemedkit.alarms.AlarmSetter;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.graphics.Toasts;

public class SaveClick implements View.OnClickListener {

    private final AppCompatActivity activity;
    private final MedicineDatabase database;
    private final Intake intake;
    private final FlexboxLayout timesGroup;
    private final TextInputEditText productName, amount, startDate, finalDate;
    private final MaterialAutoCompleteTextView interval, period;
    private final long medicineId;

    public SaveClick(AppCompatActivity activity, long intakeId, long medicineId) {
        this.activity = activity;
        this.medicineId = medicineId;

        database = MedicineDatabase.getInstance(activity);
        intake = database.intakeDAO().getByPK(intakeId);

        timesGroup = activity.findViewById(R.id.linear_times_activity_intake);
        productName = activity.findViewById(R.id.intake_edit_text_product_name);
        amount = activity.findViewById(R.id.intake_edit_text_amount);
        interval = activity.findViewById(R.id.intake_edit_text_interval);
        period = activity.findViewById(R.id.intake_edit_text_period);
        startDate = activity.findViewById(R.id.intake_calendar_start);
        finalDate = activity.findViewById(R.id.intake_calendar_finish);
    }

    @Override
    public void onClick(View v) {
        String[] intervals = activity.getResources().getStringArray(R.array.interval_types);

        boolean typed = Stream.of(productName, amount, interval, period).allMatch(item ->
                item.getText().length() > 0);

        if (typed) {
            double prodAmount = parseAmount(valueOf(amount.getText()));
            String time = timesString(timesGroup);
            String start = valueOf(startDate.getText());
            String finish = valueOf(finalDate.getText());

            Intake newIntake = new Intake(medicineId, prodAmount, intervalType, time, periodType, start, finish);

            if (edit) {
                newIntake.intakeId = intake.intakeId;
                newIntake.medicineId = intake.medicineId;
                database.intakeDAO().update(newIntake);

                edit = false;
                activity.finish();
                activity.startActivity(activity.getIntent());
            } else {
                long intakeId = database.intakeDAO().add(newIntake);

                AlarmSetter setter = new AlarmSetter(activity);

                if (intervalType.equals(intervals[0])) {
                    long[] triggers = longSeconds(time);
                    setter.setAlarm(intakeId, triggers, intervalType, finish);
                } else {
                    long trigger = longSecond(time);
                    setter.setAlarm(intakeId, trigger, intervalType, finish);
                }

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra(NEW_INTAKE, true);
                activity.startActivity(intent);
            }
        } else new Toasts(activity, R.string.text_fill_all_fields);
    }
}
