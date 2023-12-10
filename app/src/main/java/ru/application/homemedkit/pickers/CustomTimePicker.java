package ru.application.homemedkit.pickers;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static java.lang.String.format;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;

import ru.application.homemedkit.R;

public class CustomTimePicker extends Chip {
    private final int one = 1;

    public CustomTimePicker(Context context) {
        super(context);

        setAttributes((AppCompatActivity) context);

        setText(format(context.getString(R.string.intake_time_placeholder), one));
    }

    public CustomTimePicker(Context context, int index) {
        super(context);

        setAttributes((AppCompatActivity) context);

        setText(format(context.getString(R.string.intake_time_placeholder), index + one));
    }

    public CustomTimePicker(Context context, String time) {
        super(context);

        setAttributes((AppCompatActivity) context);

        setText(time);
        setOnClickListener(null);
    }

    private void setAttributes(AppCompatActivity context) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int height = (int) applyDimension(COMPLEX_UNIT_DIP, 64, metrics);
        float iconSize = applyDimension(COMPLEX_UNIT_DIP, 32, metrics);
        float stroke = applyDimension(COMPLEX_UNIT_DIP, 2, metrics);

        setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, height));
        setGravity(Gravity.CENTER);
        setCheckable(false);
        setTextAppearance(android.R.style.TextAppearance_Material_Large);
        setChipIconResource(R.drawable.vector_time);
        setChipIconSize(iconSize);
        setChipStrokeWidth(stroke);
        setOnClickListener(new ClockPicker(context, this));
    }
}
