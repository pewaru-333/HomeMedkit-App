package ru.application.homemedkit.graphics;

import android.app.Activity;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.ScannerActivity;

public class Snackbars {
    private static final int ONE = 1000;
    private final Activity activity;
    private final int ERROR_COLOR, ERROR_TEXT;
    private final int id = R.id.scanner_view;

    public Snackbars(Activity activity) {
        this.activity = activity;

        ERROR_COLOR = com.google.android.material.R.color.design_default_color_error;
        ERROR_TEXT = com.google.android.material.R.color.design_default_color_on_error;
    }

    public void encodeError() {
        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_encode_code_error, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.setTextColor(ContextCompat.getColor(activity, ERROR_TEXT));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    public void codeNotFound() {
        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_code_not_found, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    public void wrongCodeCategory() {
        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_not_medicine_code, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.setTextColor(ContextCompat.getColor(activity, ERROR_TEXT));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    public void error() {
        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_unknown_error, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.setTextColor(ContextCompat.getColor(activity, ERROR_TEXT));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    private class Recreate extends BaseTransientBottomBar.BaseCallback<Snackbar> {
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            super.onDismissed(transientBottomBar, event);
            if (activity.getClass().equals(ScannerActivity.class)) activity.recreate();
        }
    }
}
