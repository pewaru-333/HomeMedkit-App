package ru.application.homemedkit.dialogs;

import static ru.application.homemedkit.helpers.ConstantsHelper.CIS;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;

public class AddMedicineDialog extends MaterialAlertDialogBuilder {

    private final Activity activity;
    private final Intent intent;

    public AddMedicineDialog(Context context, String cis) {
        super(context);

        activity = (Activity) context;

        intent = new Intent(context, MedicineActivity.class);
        intent.putExtra(CIS, cis);
    }

    public void showDialog() {
        setTitle(R.string.text_connection_error)
                .setMessage(R.string.manual_add)
                .setOnCancelListener(this::negative)
                .setNegativeButton(R.string.text_no, (dialog, id) -> negative(dialog))
                .setPositiveButton(R.string.text_yes, (dialog, id) -> positive(dialog));

        create().show();

        MaterialTextView textView = activity.findViewById(android.R.id.message);
        if (textView != null) textView.setTextSize(16);
    }

    private void negative(DialogInterface dialog) {
        dialog.dismiss();
        activity.recreate();
    }

    private void positive(DialogInterface dialog) {
        dialog.dismiss();
        activity.startActivity(intent);
    }
}
