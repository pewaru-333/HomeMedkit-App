package ru.application.homemedkit.fragments;

import static ru.application.homemedkit.helpers.ConstantsHelper.INTAKE_ID;
import static ru.application.homemedkit.helpers.DataHelper.fetchData;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.IntakeActivity;
import ru.application.homemedkit.adapters.IntakesAdapter;
import ru.application.homemedkit.adapters.RecyclerViewInterface;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.DataHelper;

public class FragmentIntakes extends Fragment implements RecyclerViewInterface, TextWatcher {

    private IntakesAdapter adapter;
    private List<Intake> intakes;

    public FragmentIntakes() {
        super(R.layout.fragment_intakes);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MedicineDatabase database = MedicineDatabase.getInstance(getContext());
        intakes = database.intakeDAO().getAll();
        adapter = new IntakesAdapter(this);

        fetchData(adapter, intakes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText searchField = view.findViewById(R.id.medicine_search_field);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_fragment_table);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchField.addTextChangedListener(this);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this.getContext(), IntakeActivity.class);
        intent.putExtra(INTAKE_ID, adapter.getItem(position).intakeId);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        new DataHelper(getActivity()).filterData(adapter, intakes, s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}