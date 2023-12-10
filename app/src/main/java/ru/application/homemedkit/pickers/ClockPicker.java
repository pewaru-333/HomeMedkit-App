package ru.application.homemedkit.pickers;

import static ru.application.homemedkit.helpers.ConstantsHelper.TIME;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import ru.application.homemedkit.R;
import ru.application.homemedkit.helpers.DateHelper;

public class ClockPicker implements View.OnClickListener {
    private final AppCompatActivity activity;
    private final Chip chip;

    public ClockPicker(AppCompatActivity activity, Chip chip) {
        this.activity = activity;
        this.chip = chip;
    }

    private static MaterialTimePicker createPicker() {
        return new MaterialTimePicker.Builder()
                .setTitleText(R.string.text_choose_time)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();
    }

    @Override
    public void onClick(View v) {
        MaterialTimePicker picker = createPicker();

        picker.show(activity.getSupportFragmentManager(), TIME);
        picker.addOnPositiveButtonClickListener(time -> chip.setText(DateHelper.clockIntake(picker)));
    }
}
