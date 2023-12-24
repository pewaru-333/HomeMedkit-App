package ru.application.homemedkit.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.application.homemedkit.R;

public class LoadingDialog extends MaterialAlertDialogBuilder {

    private final Activity activity;
    private AlertDialog dialog;

    public LoadingDialog(Context context) {
        super(context);

        activity = (Activity) context;
    }

    @SuppressLint("InflateParams")
    public void showDialog() {
        LayoutInflater inflater = activity.getLayoutInflater();

        setView(inflater.inflate(R.layout.dialog_loading, null));
        setBackground(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        dialog = create();
        show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}
