package ru.application.homemedkit.pickers;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;

public class EditClick implements View.OnClickListener {

    private final IntakeActivity activity;

    private final FloatingActionButton buttonEdit, buttonSave;

    public EditClick(IntakeActivity activity) {
        this.activity = activity;

        buttonSave = activity.findViewById(R.id.intake_button_save);
        buttonEdit = activity.findViewById(R.id.intake_button_edit);
    }

    @Override
    public void onClick(View v) {
        buttonEdit.setVisibility(INVISIBLE);
        buttonSave.setVisibility(VISIBLE);

        IntakeActivity.edit = true;
        activity.setEditLayout();
    }
}
