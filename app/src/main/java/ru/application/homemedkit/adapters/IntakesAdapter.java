package ru.application.homemedkit.adapters;

import static ru.application.homemedkit.helpers.ImageHelper.getIconType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import ru.application.homemedkit.R;
import ru.application.homemedkit.databaseController.Intake;
import ru.application.homemedkit.databaseController.MedicineDatabase;
import ru.application.homemedkit.helpers.StringHelper;

public class IntakesAdapter extends RecyclerView.Adapter<IntakesAdapter.ItemsViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private MedicineDatabase database;
    private List<Intake> intakes;

    public IntakesAdapter(RecyclerViewInterface recyclerViewInterface) {
        intakes = new ArrayList<>();
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addData(Intake intake) {
        intakes.add(intake);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addData(List<Intake> list) {
        intakes = list;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        intakes.clear();
        notifyDataSetChanged();
    }

    public Intake getItem(int position) {
        return intakes.get(position);
    }

    @NonNull
    @Override
    public ItemsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_intake, parent, false);
        database = MedicineDatabase.getInstance(view.getContext());
        return new ItemsViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsViewHolder holder, int position) {
        Intake intake = intakes.get(position);
        String productName = database.medicineDAO().getProductName(intake.medicineId);
        String form = database.medicineDAO().getByPK(intake.medicineId).prodFormNormName;
        Context context = holder.itemView.getContext();

        Drawable image = getIconType(context, form);
        String shortName = StringHelper.shortName(productName);
        String intervalName = StringHelper.intervalName(context, intake.interval);
        String startDate = context.getResources().getString(R.string.text_from_date_card_intake, intake.startDate);

        holder.image.setImageDrawable(image);
        holder.name.setText(shortName);
        holder.interval.setText(intervalName);
        holder.from.setText(startDate);
        holder.time.setText(intake.time);
    }

    @Override
    public int getItemCount() {
        return intakes.size();
    }

    public static class ItemsViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final MaterialTextView name, interval, time, from;

        public ItemsViewHolder(@NonNull View view, RecyclerViewInterface recyclerView) {
            super(view);

            image = view.findViewById(R.id.intake_card_image);
            name = view.findViewById(R.id.intake_card_name);
            interval = view.findViewById(R.id.intake_card_interval);
            time = view.findViewById(R.id.intake_card_time);
            from = view.findViewById(R.id.intake_card_start_date);

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
