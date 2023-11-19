package ru.application.homemedkit.pickers;

import static ru.application.homemedkit.helpers.ConstantsHelper.TIME;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import ru.application.homemedkit.R;
import ru.application.homemedkit.helpers.DateHelper;

public class ClockPicker implements View.OnClickListener {
    private final AppCompatActivity activity;
    private final TextInputEditText input;

    public ClockPicker(AppCompatActivity activity, TextInputEditText input) {
        this.activity = activity;
        this.input = input;
    }

    @Override
    public void onClick(View v) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTitleText(R.string.text_choose_time)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        picker.show(activity.getSupportFragmentManager(), TIME);
        picker.addOnPositiveButtonClickListener(time -> input.setText(DateHelper.clockIntake(picker)));
    }
}
