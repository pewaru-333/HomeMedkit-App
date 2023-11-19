package ru.application.homemedkit.graphics;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.google.android.material.textfield.TextInputLayout;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;

public class ExpandAnimation implements View.OnClickListener {
    private final MedicineActivity activity;
    private final TextInputLayout layout;

    public ExpandAnimation(MedicineActivity activity, TextInputLayout layout) {
        this.activity = activity;
        this.layout = layout;
    }

    @Override
    public void onClick(View v) {
        TransitionManager.beginDelayedTransition(layout, new Slide());
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int height = layout.getLayoutParams().height;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        int dip = TypedValue.COMPLEX_UNIT_DIP;
        int value = 100;

        if (height == wrapContent) {
            layout.getLayoutParams().height = (int) TypedValue.applyDimension(dip, value, metrics);
            layout.setEndIconDrawable(R.drawable.vector_arrow_down);
        } else {
            layout.getLayoutParams().height = wrapContent;
            layout.setEndIconDrawable(R.drawable.vector_arrow_up);
        }

        layout.requestLayout();
    }
}
