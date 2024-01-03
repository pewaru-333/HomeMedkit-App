package ru.application.homemedkit.graphics;

import android.app.Activity;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.ScannerActivity;

public class Snackbars {

    private static final int ONE = 1000;
    private static final int TWO = 2000;
    private final Activity activity;
    private final int ERROR_COLOR;

    public Snackbars(Activity activity) {
        this.activity = activity;

        ERROR_COLOR = com.google.android.material.R.color.design_default_color_error;
    }

    public void wrongCode() {
        int id = activity.getClass().equals(ScannerActivity.class) ?
                R.id.scanner_view : R.id.scanned_medicine_top_app_bar_toolbar;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_wrong_code, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    public void codeNotFound() {
        int id = activity.getClass().equals(ScannerActivity.class) ?
                R.id.scanner_view : R.id.scanned_medicine_top_app_bar_toolbar;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_code_not_found, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    public void wrongCodeCategory() {
        int id = activity.getClass().equals(ScannerActivity.class) ?
                R.id.scanner_view : R.id.scanned_medicine_top_app_bar_toolbar;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_is_not_medicine_code, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.addCallback(new Recreate());
        snackbar.show();
    }

    public void error() {
        int id = activity.getClass().equals(ScannerActivity.class) ?
                R.id.scanner_view : R.id.scanned_medicine_top_app_bar_toolbar;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id), R.string.text_unknown_error, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.show();
    }

    public void noNetwork() {
        int id = R.id.button_fetch_data;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id),
                R.string.text_connection_error, TWO);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.show();
    }

    public void fetchError() {
        int id = R.id.button_fetch_data;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id),
                R.string.text_unknown_error, TWO);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.show();
    }

    public void duplicateMedicine() {
        int id = R.id.medicine_scanned_layout_ph_kinetics;

        Snackbar snackbar = Snackbar.make(activity.findViewById(id),
                R.string.text_medicine_duplicate, ONE);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, ERROR_COLOR));
        snackbar.show();
    }

    private class Recreate extends BaseTransientBottomBar.BaseCallback<Snackbar> {
        @Override
        public void onDismissed(Snackbar transientBottomBar, int event) {
            super.onDismissed(transientBottomBar, event);
            if (activity.getClass().equals(ScannerActivity.class))
                activity.recreate();
        }
    }
}
