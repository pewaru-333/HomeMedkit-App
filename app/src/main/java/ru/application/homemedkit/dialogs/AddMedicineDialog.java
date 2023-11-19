package ru.application.homemedkit.dialogs;

import static ru.application.homemedkit.helpers.ConstantsHelper.CIS;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;

public class AddMedicineDialog {

    private final Activity activity;
    private final Intent intent;
    private AlertDialog dialog;

    public AddMedicineDialog(Activity activity, String cis) {
        this.activity = activity;

        intent = new Intent(activity, MedicineActivity.class);
        intent.putExtra(CIS, cis);
    }

    public void showDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle(R.string.text_connection_error)
                .setMessage(R.string.text_manual_adding)
                .setOnCancelListener(dialogInterface -> negative())
                .setNegativeButton(R.string.text_no, (dialog, id) -> negative())
                .setPositiveButton(R.string.text_yes, (dialog, id) -> positive());

        dialog = builder.create();
        dialog.show();

        TextView textView = dialog.findViewById(android.R.id.message);
        if (textView != null) textView.setTextSize(16);
    }

    public void negative() {
        dialog.dismiss();
        activity.recreate();
    }

    private void positive() {
        dialog.dismiss();
        activity.startActivity(intent);
    }
}
