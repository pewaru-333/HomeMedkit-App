package ru.application.homemedkit.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.application.homemedkit.R;
import ru.application.homemedkit.databaseController.Medicine;
import ru.application.homemedkit.helpers.DateHelper;
import ru.application.homemedkit.helpers.StringHelper;

public class MedicinesAdapter extends RecyclerView.Adapter<MedicinesAdapter.ItemsViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private List<Medicine> medicines;

    public MedicinesAdapter(RecyclerViewInterface recyclerViewInterface) {
        medicines = new ArrayList<>();
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addData(Medicine medicine) {
        medicines.add(medicine);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addData(List<Medicine> list) {
        medicines = list;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        medicines.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortData(Comparator<Medicine> comparator) {
        medicines.sort(comparator);
        notifyDataSetChanged();
    }

    public Medicine getItem(int position) {
        return medicines.get(position);
    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_medicine, parent, false);
        return new ItemsViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);

        holder.productName.setText(StringHelper.shortName(medicine.productName));
        holder.prodFormNormName.setText(StringHelper.formName(medicine.prodFormNormName));
        holder.expDate.setText(DateHelper.inCard(medicine.expDate));
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName, prodFormNormName, expDate;

        public ItemsViewHolder(@NonNull View view, RecyclerViewInterface recyclerView) {
            super(view);

            productName = view.findViewById(R.id.card_medicine_product_name);
            prodFormNormName = view.findViewById(R.id.card_medicine_prod_form_norm_name);
            expDate = view.findViewById(R.id.card_medicine_exp_date);

            view.setOnClickListener(v -> {
                if (recyclerView != null) {
                    int pos = getLayoutPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerView.onItemClick(pos);
                    }
                }
            });
        }
    }
}
