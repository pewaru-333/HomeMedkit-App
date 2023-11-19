package ru.application.homemedkit.helpers;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import ru.application.homemedkit.R;
import ru.application.homemedkit.adapters.IntakesAdapter;
import ru.application.homemedkit.adapters.MedicinesAdapter;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.graphics.Toasts;

public class DataHelper {

    private final Activity activity;

    public DataHelper(Activity activity) {
        this.activity = activity;
    }

    public static void fetchData(MedicinesAdapter adapter, List<Medicine> medicines) {
        adapter.clearData();

        for (int i = 0; i < medicines.size(); i++) {
            adapter.addData(medicines.get(i));
        }
    }

    public static void fetchData(IntakesAdapter adapter, List<Intake> intakes) {
        adapter.clearData();

        for (int i = 0; i < intakes.size(); i++) {
            adapter.addData(intakes.get(i));
        }
    }

    public void filterData(MedicinesAdapter adapter, List<Medicine> medicines, CharSequence text) {
        adapter.clearData();

        List<Medicine> filtered = new ArrayList<>(medicines.size());

        for (Medicine medicine : medicines) {
            if (medicine.productName.toLowerCase().contains(text.toString().toLowerCase())) {
                filtered.add(medicine);
            }
        }

        if (!filtered.isEmpty()) adapter.addData(filtered);
        else new Toasts(activity, R.string.text_no_data_found);
    }

    public void filterData(IntakesAdapter adapter, List<Intake> intakes, CharSequence text) {
        adapter.clearData();

        MedicineDatabase database = MedicineDatabase.getInstance(activity);
        List<Intake> filtered = new ArrayList<>(intakes.size());

        for (Intake intake : intakes) {
            String name = database.medicineDAO().getProductName(intake.medicineId);
            if (name.toLowerCase().contains(text.toString().toLowerCase()))
                filtered.add(intake);
        }

        if (!filtered.isEmpty()) adapter.addData(filtered);
        else new Toasts(activity, R.string.text_no_data_found);
    }
}
