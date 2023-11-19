package ru.application.homemedkit.pickers;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static java.lang.String.format;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.textfield.TextInputEditText;

import ru.application.homemedkit.R;

public class CustomTimePicker extends TextInputEditText {

    private final int one = 1;

    public CustomTimePicker(Context context) {
        super(context);

        setAttributes((AppCompatActivity) context);

        setHint(format(context.getString(R.string.intake_time_placeholder), one));
    }

    public CustomTimePicker(Context context, int index) {
        super(context);

        setAttributes((AppCompatActivity) context);

        setHint(format(context.getString(R.string.intake_time_placeholder), index + one));
    }

    public CustomTimePicker(Context context, String time) {
        super(context);

        setAttributes((AppCompatActivity) context);

        setText(time);
        setOnClickListener(null);
    }

    private void setAttributes(AppCompatActivity context) {
        setFocusable(false);
        setCursorVisible(false);
        setFocusableInTouchMode(false);

        setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        setGravity(Gravity.CENTER);
        setBackground(AppCompatResources.getDrawable(context, R.drawable.shape_edit_text_time));
        setOnClickListener(new ClockPicker(context, this));
    }
}
