package ru.application.homemedkit.pickers;

import static java.lang.String.valueOf;
import static ru.application.homemedkit.activities.IntakeActivity.edit;
import static ru.application.homemedkit.activities.IntakeActivity.intervalType;
import static ru.application.homemedkit.activities.IntakeActivity.periodType;
import static ru.application.homemedkit.helpers.ConstantsHelper.HASHTAG;
import static ru.application.homemedkit.helpers.ConstantsHelper.NEW_INTAKE;
import static ru.application.homemedkit.helpers.DateHelper.longSecond;
import static ru.application.homemedkit.helpers.DateHelper.longSeconds;
import static ru.application.homemedkit.helpers.StringHelper.parseAmount;
import static ru.application.homemedkit.helpers.StringHelper.timesString;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MainActivity;
import ru.application.homemedkit.alarms.AlarmReceiver;
import ru.application.homemedkit.alarms.AlarmSetter;
import ru.application.homemedkit.databaseController.Alarm;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.graphics.Toasts;

public class SaveClick implements View.OnClickListener, TextWatcher {
    private final AppCompatActivity activity;
    private final MedicineDatabase database;
    private final Intake intake;
    private final FlexboxLayout timesGroup;
    private final MaterialAutoCompleteTextView interval, period;
    private final TextInputEditText productName, amount, startDate, finalDate;
    private final long medicineId;
    private List<? extends EditText> fields;
    private List<Chip> chips;

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

    private static void createAlarm(AlarmSetter setter, long newIntake, String[] intervals, String time, String finish) {
        if (intervalType.equals(intervals[0])) {
            long[] triggers = longSeconds(time);
            setter.setAlarm(newIntake, triggers, intervalType, finish);
        } else {
            long trigger = longSecond(time);
            setter.setAlarm(newIntake, trigger, intervalType, finish);
        }
    }

    @Override
    public void onClick(View v) {
        String[] intervals = activity.getResources().getStringArray(R.array.interval_types);
        String error = activity.getString(R.string.text_fill_this_field);

        fields = Arrays.asList(productName, amount, interval, period);
        chips = getChips();

        for (EditText item : fields) item.addTextChangedListener(this);
        for (Chip item : chips) item.addTextChangedListener(this);

        boolean typed = fields.stream().noneMatch(this::isEmpty) && chips.stream().noneMatch(this::isTime);

        if (fields.stream().anyMatch(this::isEmpty)) for (EditText item : fields)
            if (isEmpty(item)) ((TextInputLayout) item.getParent().getParent()).setError(error);

        if (chips.stream().anyMatch(this::isTime)) for (Chip chip : chips)
            if (isTime(chip)) chip.setError(error);

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

                List<Alarm> alarms = database.alarmDAO().getByIntakeId(newIntake.intakeId);

                AlarmSetter setter = new AlarmSetter(activity);
                removeAlarm(setter, alarms);
                createAlarm(setter, newIntake.intakeId, intervals, time, finish);

                edit = false;
                activity.finish();
                activity.startActivity(activity.getIntent());
            } else {
                long intakeId = database.intakeDAO().add(newIntake);

                AlarmSetter setter = new AlarmSetter(activity);
                createAlarm(setter, intakeId, intervals, time, finish);

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra(NEW_INTAKE, true);
                activity.startActivity(intent);
            }
        } else new Toasts(activity, R.string.text_fill_all_fields);
    }

    private void removeAlarm(AlarmSetter setter, List<Alarm> alarms) {
        for (int i = 0; i < alarms.size(); i++) {
            Context context = activity.getApplicationContext();
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            long alarmId = alarms.get(i).alarmId;

            setter.removeAlarm(context, alarmId, intent);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        for (EditText item : fields)
            if (!isEmpty(item)) ((TextInputLayout) item.getParent().getParent()).setError(null);

        for (Chip item : chips) if (!isTime(item)) item.setError(null, null);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean isEmpty(EditText field) {
        return field.getText().length() == 0;
    }

    private boolean isTime(Chip chip) {
        return chip.getText().toString().contains(HASHTAG);
    }

    private List<Chip> getChips() {
        List<Chip> chips = new ArrayList<>(timesGroup.getChildCount());
        for (int i = 0; i < timesGroup.getChildCount(); i++) {
            chips.add((Chip) timesGroup.getChildAt(i));
        }

        return chips;
    }
}
