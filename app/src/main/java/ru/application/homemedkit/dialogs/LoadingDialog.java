package ru.application.homemedkit.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import ru.application.homemedkit.R;

public class LoadingDialog {

    private final Activity activity;
    private AlertDialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    @SuppressLint("InflateParams")
    public void showDialog() {
        LayoutInflater inflater = activity.getLayoutInflater();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);

        builder.setView(inflater.inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);

        dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}
