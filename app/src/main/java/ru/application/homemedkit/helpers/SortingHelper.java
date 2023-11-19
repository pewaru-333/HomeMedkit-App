package ru.application.homemedkit.helpers;

import static android.view.View.OnClickListener;
import static com.google.android.material.button.MaterialButtonToggleGroup.OnButtonCheckedListener;
import static ru.application.homemedkit.helpers.FiltersHelper.sortInverseDate;
import static ru.application.homemedkit.helpers.FiltersHelper.sortInverseTitle;
import static ru.application.homemedkit.helpers.FiltersHelper.sortReverseDate;
import static ru.application.homemedkit.helpers.FiltersHelper.sortReverseTitle;

import android.app.Activity;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;

import ru.application.homemedkit.R;
import ru.application.homemedkit.adapters.MedicinesAdapter;

public class SortingHelper implements OnClickListener, OnButtonCheckedListener {
    private final Activity activity;
    private final MedicinesAdapter adapter;
    private final SettingsHelper preferences;
    private final String[] types;

    public SortingHelper(Activity activity, MedicinesAdapter adapter) {
        this.activity = activity;
        this.adapter = adapter;

        preferences = new SettingsHelper(activity);
        types = activity.getResources().getStringArray(R.array.sorting_types);
    }

    @Override
    public void onClick(View v) {
        View view = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_sorting_types, null);
        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_button_sorting);
        toggleGroup.addOnButtonCheckedListener(this);

        final String sortOrder = preferences.getSortingOrder();

        if (sortOrder.equals(types[0])) toggleGroup.check(R.id.button_inverse_title);
        else if (sortOrder.equals(types[1])) toggleGroup.check(R.id.button_reverse_title);
        else if (sortOrder.equals(types[2])) toggleGroup.check(R.id.button_inverse_date);
        else if (sortOrder.equals(types[3])) toggleGroup.check(R.id.button_reverse_date);

        BottomSheetDialog bottomSheet = new BottomSheetDialog(activity);
        bottomSheet.getBehavior().setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheet.getBehavior().setDraggable(true);
        bottomSheet.setContentView(view);
        bottomSheet.show();
    }

    @Override
    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        if (isChecked) {
            if (checkedId == R.id.button_inverse_title) {
                preferences.setSortingOrder(types[0]);
                adapter.sortData(sortInverseTitle);
            }
            if (checkedId == R.id.button_reverse_title) {
                preferences.setSortingOrder(types[1]);
                adapter.sortData(sortReverseTitle);
            }
            if (checkedId == R.id.button_inverse_date) {
                preferences.setSortingOrder(types[2]);
                adapter.sortData(sortInverseDate);
            }
            if (checkedId == R.id.button_reverse_date) {
                preferences.setSortingOrder(types[3]);
                adapter.sortData(sortReverseDate);
            }
        }
    }

    public void setSorting() {
        String value = preferences.getSortingOrder();

        if (value.equals(types[0])) adapter.sortData(sortInverseTitle);
        else if (value.equals(types[1])) adapter.sortData(sortReverseTitle);
        else if (value.equals(types[2])) adapter.sortData(sortInverseDate);
        else adapter.sortData(sortReverseDate);
    }
}
