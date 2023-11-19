package ru.application.homemedkit.fragments;

import static ru.application.homemedkit.helpers.ConstantsHelper.ADDING;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ru.application.homemedkit.R;
import ru.application.homemedkit.activities.MedicineActivity;
import ru.application.homemedkit.activities.ScannerActivity;

public class FragmentHome extends Fragment {
    private FragmentActivity activity;

    public FragmentHome() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = requireActivity();
        activity.getOnBackPressedDispatcher().addCallback(new BackClick());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialCardView cardScan = view.findViewById(R.id.card_scan_code);
        MaterialCardView cardSelf = view.findViewById(R.id.card_add_self);

        cardScan.setOnClickListener(v ->
                startActivity(new Intent(activity, ScannerActivity.class)));
        cardSelf.setOnClickListener(v ->
                startActivity(new Intent(activity, MedicineActivity.class)
                        .putExtra(ADDING, true)));
    }

    private class BackClick extends OnBackPressedCallback {
        public BackClick() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.text_confirm_exit)
                    .setMessage(R.string.text_sure_to_exit)
                    .setPositiveButton(R.string.text_yes, (dialog, which) -> activity.finishAndRemoveTask())
                    .setNegativeButton(R.string.text_no, (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}