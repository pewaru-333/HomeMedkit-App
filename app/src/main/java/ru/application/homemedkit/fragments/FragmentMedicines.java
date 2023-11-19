package ru.application.homemedkit.fragments;

import static ru.application.homemedkit.helpers.ConstantsHelper.ID;
import static ru.application.homemedkit.helpers.DataHelper.fetchData;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;
import ru.application.homemedkit.adapters.MedicinesAdapter;
import ru.application.homemedkit.adapters.RecyclerViewInterface;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.DataHelper;
import ru.application.homemedkit.helpers.SortingHelper;

public class FragmentMedicines extends Fragment implements RecyclerViewInterface, TextWatcher {

    private MedicinesAdapter adapter;
    private List<Medicine> medicines;

    public FragmentMedicines() {
        super(R.layout.fragment_medicines);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MedicineDatabase database = MedicineDatabase.getInstance(getContext());
        medicines = database.medicineDAO().getAll();
        adapter = new MedicinesAdapter(this);

        fetchData(adapter, medicines);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText searchField = view.findViewById(R.id.medicine_search_field);
        ActionMenuItemView buttonSort = view.findViewById(R.id.top_app_bar_sort);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_fragment_medicines);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        SortingHelper sorting = new SortingHelper(getActivity(), adapter);
        sorting.setSorting();

        buttonSort.setOnClickListener(sorting);

        searchField.addTextChangedListener(this);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this.getContext(), MedicineActivity.class);
        intent.putExtra(ID, adapter.getItem(position).id);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        new DataHelper(getActivity()).filterData(adapter, medicines, s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}